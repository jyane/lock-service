package jp.jyane.lock.service

import com.google.protobuf.ByteString
import com.google.protobuf.duration.Duration
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

trait LockServiceImpl extends LockServiceGrpc.LockService with UseChannels with UseExecutionContext {
  def kv: KVGrpc.KVStub = KVGrpc.stub(channel = channels.etcdChannel)
  def lease: LeaseGrpc.LeaseStub = LeaseGrpc.stub(channel = channels.etcdChannel)

  override def tryAcquire(request: TryAcquireRequest): Future[TryAcquireResponse] = {
    for {
      validatedRequest <- ProtoValidator.validateTryAcqureRequest(request) match {
        case Success(s) => Future.successful(s)
        case Failure(s) => Future.failed(InvalidArgumentException(s.toString()))
      }
      key = ByteString.copyFromUtf8(s"/lock/${validatedRequest.owner}/${validatedRequest.key}")
      rangeResponse <- kv.range(RangeRequest(key = key))
      _ <- if (rangeResponse.kvs.nonEmpty) {
        Future.failed(AlreadyExistsException("key already exists"))
      } else {
        Future.successful(())
      }
      leaseGrantResponse <- lease.leaseGrant(LeaseGrantRequest(tTL = validatedRequest.getDuration.seconds))
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
    case NonFatal(_) =>
      throw Status.INTERNAL.asRuntimeException()
  }

  override def release(request: ReleaseRequest): Future[ReleaseResponse] = {
    for {
      validatedRequest <- ProtoValidator.validateRelaseRequest(request) match {
        case Success(s) => Future.successful(s)
        case Failure(s) => Future.failed(InvalidArgumentException(s.toString()))
      }
      key = ByteString.copyFromUtf8(s"/lock/${validatedRequest.owner}/${validatedRequest.key}")
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
    case NonFatal(_) =>
      throw Status.INTERNAL.asRuntimeException()
  }
}

trait MixinLockService {
  val lockService = new LockServiceImpl with MixinChannels with MixinExecutionContext
}
