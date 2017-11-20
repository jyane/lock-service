package jp.jyane.lock

import com.typesafe.scalalogging.StrictLogging
import io.grpc.{ServerBuilder, ServerInterceptor, ServerInterceptors}
import io.grpc.protobuf.services.ProtoReflectionService
import jp.jyane.lock.service.MixinLockService
import jp.jyane.lock.interceptor.LoggingInterceptor
import jyane.lock.LockServiceGrpc

object LockMain extends MixinLockService with MixinChannels with MixinLockConfig with MixinExecutionContext with StrictLogging {
  private[this] lazy val server = ServerBuilder
    .forPort(lockConfig.serverConfig.port)
    .addService(ServerInterceptors.intercept(LockServiceGrpc.bindService(lockService, executionContext), new LoggingInterceptor))
    .addService(ProtoReflectionService.newInstance())
    .build()

  def onStart(): Unit = {
    logger.info(s"server start. port: ${lockConfig.serverConfig.port}")
    server.start()
    server.awaitTermination()
  }

  def onStop(): Unit = {
    logger.info("shutdown.")
    channels.etcdChannel.shutdown()
    server.shutdown()
    ()
  }

  def main(args: Array[String]): Unit = {
    onStart()
    sys.addShutdownHook(onStop())
    ()
  }
}
