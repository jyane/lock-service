package jp.jyane.lock

import scala.concurrent.ExecutionContext

trait UseExecutionContext {
  implicit def executionContext: ExecutionContext
}

trait MixinExecutionContext {
  // currently use global execution context.
  implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
