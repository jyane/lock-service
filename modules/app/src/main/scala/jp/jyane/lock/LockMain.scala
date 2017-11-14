package jp.jyane.lock

import io.grpc.ServerBuilder

object LockMain {
  private[this] lazy val server = ServerBuilder
    .forPort(10080)
    .build

  def onStart(): Unit = {
    server.start()
    server.awaitTermination()
  }

  def onStop(): Unit = {
    server.shutdown()
    ()
  }

  def main(args: Array[String]): Unit = {
    onStart()
    sys.addShutdownHook(onStop())
    ()
  }
}
