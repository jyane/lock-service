package jp.jyane.lock

import jyane.lock.LockConfig
import scalapb.TextFormat

trait UseLockConfig {
  def lockConfig: LockConfig
}

trait MixinLockConfig {
  val lockConfig: LockConfig =
    TextFormat.fromAscii(LockConfig, scala.io.Source.fromFile("conf/lock_config.pb.txt").mkString) match {
      case Right(x) => x
      case Left(x) => throw new RuntimeException(s"failed to load the config: ${x}")
    }
}
