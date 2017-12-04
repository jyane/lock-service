package jp.jyane.lock

import com.google.protobuf.duration.Duration
import jyane.lock.{AcquireRequest, ReleaseRequest, TryAcquireRequest}

import scalaz.ValidationNel
import scalaz.syntax.validation._
import scalaz.syntax.apply._

object ProtoValidator {
  def validateStringLength(name: String, s: String, length: Int): ValidationNel[String, String] = {
    if (s.nonEmpty && s.length < length) {
      s.successNel
    } else {
      s"$name length must be 0 < s.size < $length".failureNel
    }
  }

  def validateDuration(duration: Duration, limit: Long): ValidationNel[String, Duration] = {
    if (0 < duration.seconds && duration.seconds < limit) {
      duration.successNel
    } else {
      s"duration must be 0 < seconds < $limit".failureNel
    }
  }

  def validateAcquireRequest(request: AcquireRequest): ValidationNel[String, AcquireRequest] = {
    (
      ProtoValidator.validateStringLength("owner", request.owner, 256) |@|
        ProtoValidator.validateStringLength("key", request.key, 256) |@|
        ProtoValidator.validateDuration(request.getDuration, 24L * 60L * 60L + 1)
    ) { case (owner, key, duration) =>
      AcquireRequest(owner, key, Some(duration))
    }
  }

  def validateTryAcquireRequest(request: TryAcquireRequest): ValidationNel[String, TryAcquireRequest] = {
    (
      ProtoValidator.validateStringLength("owner", request.owner, 256) |@|
      ProtoValidator.validateStringLength("key", request.key, 256) |@|
      ProtoValidator.validateDuration(request.getDuration, 24L * 60L * 60L + 1)
    ) { case (owner, key, duration) =>
      TryAcquireRequest(owner, key, Some(duration))
    }
  }

  def validateReleaseRequest(request: ReleaseRequest): ValidationNel[String, ReleaseRequest] = {
    (
      ProtoValidator.validateStringLength("owner", request.owner, 256) |@|
      ProtoValidator.validateStringLength("key", request.key, 256)
    ) { case (owner, key) =>
      ReleaseRequest(owner, key)
    }
  }
}
