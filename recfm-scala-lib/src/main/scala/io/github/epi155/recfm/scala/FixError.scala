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

  class NotAsciiException(c: Char, u: Int, deep: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), deep) {
  }

  class NotDigitException(c: Char, u: Int, deep: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), deep) {
  }

  class NotLatinException(c: Int, u: Int, deep: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c, u + 1), deep) {
  }

  class NotValidException(c: Char, u: Int, deep: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), deep) {
  }

  protected class SetterException(s: String, deep: Int) extends RuntimeException(s) {
    fillInStackTrace
    setStackTrace(getStackTrace.drop(deep))
  }

  class NotBlankException(c: Char, u: Int, deep: Int) extends FixError.SetterException(String.format("Offending char: U+%04X @+%d", c.toInt, u + 1), deep) {
  }

  class NotDomainException(value: String, deep: Int) extends FixError.SetterException("Offending value " + value, deep) {
  }

  class NotMatchesException(value: String, deep: Int) extends FixError.SetterException("Offending value " + value, deep) {
  }
}
