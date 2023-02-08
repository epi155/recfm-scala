package io.github.epi155.recfm.scala

object OverflowAction extends Enumeration {
  type OverflowAction = Value
  val TruncR, TruncL, Error = Value
}
