package jp.jyane.lock.service

import com.google.protobuf.ByteString
import com.google.protobuf.duration.Duration
import com.typesafe.scalalogging.StrictLogging
import etcdserverpb._
import io.grpc.Status
import jp.jyane.lock._
import jp.jyane.lock.exception.{AlreadyExistsException, FailedPreconditionException, InvalidArgumentException}
import jyane.lock._

import scala.concurrent.Future
import scala.util.control.NonFatal
import scalaz.{Failure, Success}

trait UseLockService {
  def lockService: LockService
}

trait LockService {
  def tryAcquire(request: TryAcquireRequest): Future[TryAcquireResponse]
  def release(request: ReleaseRequest): Future[ReleaseResponse]
}

trait LockServiceImpl extends LockServiceGrpc.LockService with UseChannels with StrictLogging with UseExecutionContext {
  val kv: KVGrpc.KVStub = KVGrpc.stub(channel = channels.etcdChannel)
  val lease: LeaseGrpc.LeaseStub = LeaseGrpc.stub(channel = channels.etcdChannel)

  def generateKey(owner: String, key: String): ByteString =
    ByteString.copyFromUtf8(s"/lock/$owner/$key")

  override def tryAcquire(request: TryAcquireRequest): Future[TryAcquireResponse] = {
    for {
      _ <- ProtoValidator.validateTryAcquireRequest(request) match {
        case Success(s) => Future.successful(s)
        case Failure(s) => Future.failed(InvalidArgumentException(s.toString()))
      }
      key = generateKey(request.owner, request.key)
      rangeResponse <- kv.range(RangeRequest(key = key))
      _ <- if (rangeResponse.kvs.nonEmpty) {
        Future.failed(AlreadyExistsException("key already exists"))
      } else {
        Future.successful(())
      }
      leaseGrantResponse <- lease.leaseGrant(LeaseGrantRequest(tTL = request.getDuration.seconds))
      _ <- kv.put(
        PutRequest(
          key = key,
          value = ByteString.copyFrom(Array[Byte](1)),
          lease = leaseGrantResponse.iD
        )
      )
    } yield {
      TryAcquireResponse(duration = Some(Duration(seconds = leaseGrantResponse.tTL)))
    }
  }.recover {
    case e: AlreadyExistsException =>
      throw Status.ALREADY_EXISTS.withDescription(e.getMessage).asRuntimeException()
    case e: InvalidArgumentException =>
      throw Status.INVALID_ARGUMENT.withDescription(e.getMessage).asRuntimeException()
    case NonFatal(e) =>
      logger.error("internal error", e)
      throw Status.INTERNAL.asRuntimeException()
  }

  override def release(request: ReleaseRequest): Future[ReleaseResponse] = {
    for {
      _ <- ProtoValidator.validateReleaseRequest(request) match {
        case Success(s) => Future.successful(s)
        case Failure(s) => Future.failed(InvalidArgumentException(s.toString()))
      }
      key = generateKey(request.owner, request.key)
      rangeResponse <- kv.range(RangeRequest(key = key))
      _ <- if (rangeResponse.kvs.isEmpty) {
        Future.failed(FailedPreconditionException("key does not exist"))
      } else {
        Future.successful(())
      }
      _ <- kv.deleteRange(DeleteRangeRequest(key = key))
    } yield {
      ReleaseResponse()
    }
  }.recover {
    case e: FailedPreconditionException =>
      throw Status.FAILED_PRECONDITION.withDescription(e.getMessage).asRuntimeException()
    case e: InvalidArgumentException =>
      throw Status.INVALID_ARGUMENT.withDescription(e.getMessage).asRuntimeException()
    case NonFatal(e) =>
      logger.error("internal error", e)
      throw Status.INTERNAL.asRuntimeException()
  }
}

trait MixinLockService {
  val lockService = new LockServiceImpl with MixinChannels with MixinExecutionContext
}
