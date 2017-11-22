package jp.jyane.lock

import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder

trait UseChannels {
  def channels: Channels
}

trait Channels extends UseLockConfig {
  def etcdChannel: ManagedChannel
}

object DefaultChannels extends Channels with MixinLockConfig {
  lazy val etcdChannel: ManagedChannel = NettyChannelBuilder
    .forAddress(lockConfig.etcdConfig.address, lockConfig.etcdConfig.port)
    .usePlaintext(true)
    .build()
}

trait MixinChannels {
  val channels: Channels = DefaultChannels
}
