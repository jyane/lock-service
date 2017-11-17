package jp.jyane.lock

import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder

trait UseChannels {
  def channels: Channels
}

trait Channels extends UseLockConfig {
  lazy val etcdChannel: ManagedChannel = NettyChannelBuilder
    .forAddress(lockConfig.etcdConfig.address, lockConfig.etcdConfig.port)
    .usePlaintext(true)
    .build()
}

trait MixinChannels {
  val channels = new Channels with MixinLockConfig
}
