package io.github.epi155.recfm.scala

trait FixBasic {
  def validate(handler: FieldValidateHandler): Boolean

  def audit(handler: FieldValidateHandler): Boolean
}
