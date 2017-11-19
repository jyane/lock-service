package jp.jyane.lock.exception

abstract class LockException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)

case class FailedPreconditionException(message: String = null, cause: Throwable = null) extends LockException(message, cause)
case class InvalidArgumentException(message: String = null, cause: Throwable = null) extends LockException(message, cause)
case class AlreadyExistsException(message: String = null, cause: Throwable = null) extends LockException(message, cause)
case class InternalException(message: String = null, cause: Throwable = null) extends LockException(message, cause)
