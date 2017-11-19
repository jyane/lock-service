package jp.jyane.lock.service

import com.google.common.base.Strings
import com.google.protobuf.ByteString
import com.google.protobuf.duration.Duration
import etcdserverpb._
import io.grpc.{ManagedChannel, Status, StatusRuntimeException}
import io.grpc.util.MutableHandlerRegistry
import jp.jyane.lock.testutil.WithServer
import jp.jyane.lock.{Channels, LockConfig}
import jyane.lock.{TryAcquireRequest, TryAcquireResponse}
import mvccpb.KeyValue
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class LockServiceImplTest extends org.scalatest.WordSpec with MockitoSugar {
  class Setup(registry: MutableHandlerRegistry, channel: ManagedChannel) { self =>
    val kvImpl = mock[KVGrpc.KV]
    val leaseImpl = mock[LeaseGrpc.Lease]

    registry.addService(KVGrpc.bindService(kvImpl, ExecutionContext.global))
    registry.addService(LeaseGrpc.bindService(leaseImpl, ExecutionContext.global))

    val lockService = new LockServiceImpl {
      def channels: Channels = new Channels {
        def lockConfig = mock[LockConfig]
        override lazy val etcdChannel: ManagedChannel = channel
      }
      implicit def executionContext: ExecutionContext = ExecutionContext.global
    }
  }

  "tryAcquire" should {
    "returns OK" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        Mockito
          .when(kvImpl.range(RangeRequest(key = ByteString.copyFromUtf8("/lock/jyane/foo"))))
          .thenReturn(Future.successful(RangeResponse()))
        Mockito
          .when(leaseImpl.leaseGrant(LeaseGrantRequest(tTL = 10L)))
          .thenReturn(Future.successful(LeaseGrantResponse(iD = 1L, tTL = 10L)))
        Mockito
          .when(kvImpl.put(PutRequest(key = ByteString.copyFromUtf8("/lock/jyane/foo"), value = ByteString.copyFrom(Array[Byte](1)), lease = 1L)))
          .thenReturn(Future.successful(PutResponse()))

        val request = TryAcquireRequest(owner = "jyane", key = "foo", duration = Some(Duration(seconds = 10L)))
        val response = Await.result(lockService.tryAcquire(request), 1.seconds)
        assert(response === TryAcquireResponse(duration = Some(Duration(seconds = 10L))))
      }
      ()
    })

    "returns ALREADY_EXISTS when key already exists" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        Mockito
          .when(kvImpl.range(RangeRequest(key = ByteString.copyFromUtf8("/lock/jyane/foo"))))
          .thenReturn(Future.successful(RangeResponse(kvs = Seq(KeyValue()))))

        val request = TryAcquireRequest(owner = "jyane", key = "foo", duration = Some(Duration(seconds = 10L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.ALREADY_EXISTS.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when key is empty" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "jyane", key = "", duration = Some(Duration(seconds = 10L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when key length is larger than 255" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "jyane", key = Strings.repeat("a", 256), duration = Some(Duration(seconds = 10L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when owner is empty" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "", key = "foo", duration = Some(Duration(seconds = 10L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when owner length is larger than 255" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = Strings.repeat("a", 256), key = "foo", duration = Some(Duration(seconds = 10L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when duration is None" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "jyane", key = "foo", duration = None)
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when duration is larger than 60 minutes" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "jyane", key = "foo", duration = Some(Duration(seconds = 60L * 60L + 1L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })

    "returns INVALID_ARGUMENT when duration is less than 1 seconds" in new WithServer({ (registry, channel) =>
      new Setup(registry, channel) {
        val request = TryAcquireRequest(owner = "jyane", key = "foo", duration = Some(Duration(seconds = 0L)))
        val response = intercept[StatusRuntimeException] {
          Await.result(lockService.tryAcquire(request), 1.seconds)
        }
        assert(response.getStatus.getCode === Status.INVALID_ARGUMENT.getCode)
      }
      ()
    })
  }
}
