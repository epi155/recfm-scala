package io.github.epi155.recfm.scala

case class FieldValidateError(
                               name: String = null,
                               offset: Int,
                               length: Int,
                               value: String,
                               column: Int = -1,
                               code: ValidateError.ValidateError,
                               wrong: Char = '\ufffd'
                             )
