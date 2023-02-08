package io.github.epi155.recfm.scala

trait FixBasic {
  def validateFails(handler: FieldValidateHandler): Boolean

  def auditFails(handler: FieldValidateHandler): Boolean
}
