package jp.jyane.lock.testutil

import java.util.UUID

import io.grpc.ManagedChannel
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import io.grpc.util.MutableHandlerRegistry

/**
  * In-process gRPC Server
  * @param f f
  */
class WithServer(f: (MutableHandlerRegistry, ManagedChannel) => Unit) {
  val serverName = UUID.randomUUID().toString
  val registry = new MutableHandlerRegistry
  val server = InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(registry).directExecutor().build().start()
  val channel = InProcessChannelBuilder.forName(serverName).directExecutor().build()
  f(registry, channel)
  server.shutdown()
  channel.shutdown()
}
