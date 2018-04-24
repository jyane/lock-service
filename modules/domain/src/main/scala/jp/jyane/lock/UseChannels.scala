package jp.jyane.lock

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

trait UseChannels {
  def channels: Channels
}

trait Channels extends UseLockConfig {
  def etcdChannel: ManagedChannel
}

object DefaultChannels extends Channels with MixinLockConfig {
  lazy val etcdChannel: ManagedChannel = ManagedChannelBuilder
    .forAddress(lockConfig.etcdConfig.address, lockConfig.etcdConfig.port)
    .usePlaintext()
    .build()
}

trait MixinChannels {
  val channels: Channels = DefaultChannels
}
