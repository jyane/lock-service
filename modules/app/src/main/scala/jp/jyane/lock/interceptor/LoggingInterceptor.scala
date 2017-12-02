package jp.jyane.lock.interceptor

import com.typesafe.scalalogging.StrictLogging
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc._

class LoggingServerCall[ReqT, RespT](call: ServerCall[ReqT, RespT]) extends SimpleForwardingServerCall[ReqT, RespT](call) with StrictLogging {
  override def sendMessage(message: RespT): Unit = {
    logger.info(s"${call.getMethodDescriptor.getFullMethodName} send data:\n$message")
    super.sendMessage(message)
  }

  override def close(status: Status, trailers: Metadata): Unit = {
    logger.info(s"call was closed: $status, $trailers")
    super.close(status, trailers)
  }
}

class LoggingServerCallListener[ReqT](val delegate: ServerCall.Listener[ReqT], methodName: String) extends ForwardingServerCallListener[ReqT] with StrictLogging {
  override def onMessage(message: ReqT): Unit = {
    logger.info(s"$methodName receive data:\n$message")
    super.onMessage(message)
  }
}

class LoggingInterceptor extends ServerInterceptor with StrictLogging {
  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    headers: Metadata,
    next: ServerCallHandler[ReqT, RespT]
  ): LoggingServerCallListener[ReqT] = {
    val methodName = call.getMethodDescriptor.getFullMethodName
    new LoggingServerCallListener[ReqT](next.startCall(new LoggingServerCall(call), headers), methodName)
  }
}
