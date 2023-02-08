package io.github.epi155.recfm.scala

trait FieldValidateHandler {
  def error(fieldValidateError: FieldValidateError): Unit
}
