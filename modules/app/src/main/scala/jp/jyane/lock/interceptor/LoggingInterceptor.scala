package jp.jyane.lock.interceptor

import com.typesafe.scalalogging.StrictLogging
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc._

class LoggingInterceptor extends ServerInterceptor with StrictLogging {
  class LoggingServerCallListener[ReqT](val delegate: ServerCall.Listener[ReqT]) extends ForwardingServerCallListener[ReqT] {
    override def onMessage(message: ReqT): Unit = {
      logger.info(s"receive: $message")
      super.onMessage(message)
    }
  }

  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    headers: Metadata,
    next: ServerCallHandler[ReqT, RespT]
  ) = {
    new LoggingServerCallListener[ReqT](
      next.startCall(
        new SimpleForwardingServerCall[ReqT, RespT](call) {
          override def sendMessage(message: RespT): Unit = {
            logger.info(s"send data: $message")
            super.sendMessage(message)
          }
        },
        headers
      )
    )
  }
}
