package jp.jyane.lock.service

import etcdserverpb.KVGrpc
import jp.jyane.lock.{MixinChannels, UseChannels}
import jyane.lock._

import scala.concurrent.Future

trait UseLockService {
  def lockService: LockService
}

trait LockService {
  def tryAcquire(request: TryAcquireRequest): Future[TryAcquireResponse]
  def release(request: ReleaseRequest): Future[ReleaseResponse]
}

trait LockServiceImpl extends LockServiceGrpc.LockService with UseChannels {
  def kv = KVGrpc.stub(channel = channels.etcdChannel)

  override def tryAcquire(request: TryAcquireRequest): Future[TryAcquireResponse] = {
    ???
  }

  override def release(request: ReleaseRequest): Future[ReleaseResponse] = {
    ???
  }
}

trait MixinLockService {
  val lockService = new LockServiceImpl with MixinChannels
}
