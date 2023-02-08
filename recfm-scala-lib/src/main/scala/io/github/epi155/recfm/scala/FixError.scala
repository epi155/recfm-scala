package io.github.epi155.recfm.scala

object FixError {
  class FieldOverFlowException(s: String) extends RuntimeException(s) {
  }

  class FieldUnderFlowException(s: String) extends RuntimeException(s) {
  }

  class RecordOverflowException(s: String) extends RuntimeException(s) {
  }

  class RecordUnderflowException(s: String) extends RuntimeException(s) {
  }

  class NotAsciiException(c: Char, u: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), 3) {
  }

  class NotDigitException(c: Char, u: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), 3) {
  }

  class NotLatinException(c: Int, u: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c, u + 1), 3) {
  }

  class NotValidException(c: Char, u: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), 3) {
  }

  protected class SetterException(s: String, deep: Int) extends RuntimeException(s) {
    fillInStackTrace
    setStackTrace(getStackTrace.drop(deep))
  }

  class NotBlankException(c: Char, u: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), 3) {
  }

  class NotDomainException(value: String) extends FixError.SetterException("Offemding value " + value, 3) {
  }

  class NotMatchesException(value: String) extends FixError.SetterException("Offemding value " + value, 3) {
  }
}
