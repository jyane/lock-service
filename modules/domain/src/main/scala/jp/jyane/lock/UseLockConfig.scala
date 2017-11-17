package jp.jyane.lock

import java.io.File

import com.typesafe.config.ConfigFactory

case class ServerConfig(port: Int)

case class EtcdConfig(address: String, port: Int)

trait UseLockConfig {
  def lockConfig: LockConfig
}

case class LockConfig(serverConfig: ServerConfig, etcdConfig: EtcdConfig)

trait MixinLockConfig {
  private[this] val config = ConfigFactory.parseFile(new File("conf/app.conf"))

  val lockConfig = LockConfig(
    ServerConfig(config.getInt("server.port")),
    EtcdConfig(config.getString("etcd.address"), config.getInt("etcd.port"))
  )
}
