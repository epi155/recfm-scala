package io.github.epi155.recfm.scala

import java.nio.CharBuffer
import java.text.NumberFormat
import java.util
import scala.util.matching.Regex

abstract class FixEngine(
                          length: Int,
                          s: String,
                          r: FixRecord,
                          overflowError: Boolean, underflowError: Boolean
                        ) {
  final protected var rawData: Array[Char] = _

  if (s != null)
    buildFromString(length, s, overflowError, underflowError)
  else if (r != null)
    buildFromRecord(length, r, overflowError, underflowError)
  else
    buildEmpty(length)

  def encode = new String(rawData)

  def validateFails(handler: FieldValidateHandler): Boolean = validateFields(handler)

  def auditFails(handler: FieldValidateHandler): Boolean = auditFields(handler)

  protected def initialize(): Unit

  protected def abc(offset: Int, count: Int) = new String(rawData, offset, count)

  protected def abc(s: String, offset: Int, count: Int): Unit = {
    if (s == null) {
      throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + " null")
    }
    else if (s.length == count) setAsIs(s, offset)
    else if (s.length < count)
        throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    else
        throw new FixError.FieldOverFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
  }

  protected def abc(s: String, offset: Int, count: Int, overflowAction: OverflowAction.OverflowAction, underflowAction: UnderflowAction.UnderflowAction, pad: Char, init: Char): Unit = {
    if (s == null) {
      if (underflowAction eq UnderflowAction.Error) throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + " null")
      fillChar(offset, count, init)
    }
    else if (s.length == count) setAsIs(s, offset)
    else if (s.length < count) underflowAction match {
      case UnderflowAction.PadR =>
        padToRight(s, offset, count, pad)

      case UnderflowAction.PadL =>
        padToLeft(s, offset, count, pad)

      case UnderflowAction.Error =>
        throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
    else overflowAction match {
      case OverflowAction.TruncR =>
        truncRight(s, offset, count)

      case OverflowAction.TruncL =>
        truncLeft(s, offset, count)

      case OverflowAction.Error =>
        throw new FixError.FieldOverFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
  }

  private def truncLeft(s: String, offset: Int, count: Int): Unit = {
    var u = s.length - 1
    var v = offset + count - 1
    while ( {
      v >= offset
    }) {
      rawData(v) = s.charAt(u)
      u -= 1
      v -= 1
    }
  }

  private def truncRight(s: String, offset: Int, count: Int): Unit = {
    var u = 0
    var v = offset
    while ( {
      u < count
    }) {
      rawData(v) = s.charAt(u)
      u += 1
      v += 1
    }
  }

  private def padToLeft(s: String, offset: Int, count: Int, c: Char): Unit = {
    var u = s.length - 1
    var v = offset + count - 1

    while ( {
      u >= 0
    }) {
      rawData(v) = s.charAt(u)

      u -= 1
      v -= 1
    }

    while ( {
      v >= offset
    }) {
      rawData(v) = c

      v -= 1
    }
  }

  private def padToRight(s: String, offset: Int, count: Int, c: Char): Unit = {
    var u = 0
    var v = offset

    while ( {
      u < s.length
    }) {
      rawData(v) = s.charAt(u)

      u += 1
      v += 1
    }

    while ( {
      u < count
    }) {
      rawData(v) = c

      u += 1
      v += 1
    }
  }

  private def setAsIs(s: String, offset: Int): Unit = {
    var u = 0
    var v = offset
    while ( {
      u < s.length
    }) {
      rawData(v) = s.charAt(u)

      u += 1
      v += 1
    }
  }

  protected def num(s: String, offset: Int, count: Int, ovfl: OverflowAction.OverflowAction, unfl: UnderflowAction.UnderflowAction): Unit = {
    if (s == null) {
      if (unfl eq UnderflowAction.Error) throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + " null")
      fillChar(offset, count, '0')
    }
    else if (s.length == count) setAsIs(s, offset)
    else if (s.length < count) unfl match {
      case UnderflowAction.PadR =>
        padToRight(s, offset, count, '0')

      case UnderflowAction.PadL =>
        padToLeft(s, offset, count, '0')

      case UnderflowAction.Error =>
        throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
    else ovfl match {
      case OverflowAction.TruncR =>
        truncRight(s, offset, count)

      case OverflowAction.TruncL =>
        truncLeft(s, offset, count)

      case OverflowAction.Error =>
        throw new FixError.FieldOverFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
  }

  private def fillChar(offset: Int, count: Int, fill: Char): Unit = {
    var u = 0
    var v = offset
    while ( {
      u < count
    }) {
      rawData(v) = fill

      u += 1
      v += 1
    }
  }

  protected def fill(offset: Int, count: Int, c: Char): Unit = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      rawData(u) = c

      u += 1
      v += 1
    }
  }

  protected def pic9(digits: Int): NumberFormat = {
    val nf = NumberFormat.getInstance
    nf.setMinimumIntegerDigits(digits)
    nf.setGroupingUsed(false)
    nf
  }

  protected def validateFields(handler: FieldValidateHandler): Boolean

  protected def auditFields(handler: FieldValidateHandler): Boolean

  protected def checkDigit(name: String, offset: Int, count: Int, handler: FieldValidateHandler): Boolean = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!('0' <= c && c <= '9')) {
        handler.error(
          FieldValidateError(
            name = name,
            offset = offset,
            length = count,
            value = abc(offset, count),
            column = u + 1,
            code = ValidateError.NotNumber,
            wrong = c)
        )
        return true
      }

      u += 1
      v += 1
    }
    false
  }

  protected def checkDigitBlank(name: String, offset: Int, count: Int, handler: FieldValidateHandler): Boolean = {
    var c = rawData(offset)
    if (c == ' ') {
      var u = offset + 1
      var v = 1
      while ( {
        v < count
      }) {
        if (rawData(u) != ' ') {
          handler.error(
            FieldValidateError(
              name = name,
              offset = offset,
              length = count,
              value = abc(offset, count),
              column = u + 1,
              code = ValidateError.NotBlank,
              wrong = rawData(u))
            )
          return true
        }

        u += 1
        v += 1
      }
    }
    else if ('0' <= c && c <= '9') {
      var u = offset + 1
      var v = 1
      while ( {
        v < count
      }) {
        c = rawData(u)
        if (!('0' <= c && c <= '9')) {
          handler.error(
            FieldValidateError(
              name = name,
              offset = offset,
              length = count,
              value = abc(offset, count),
              column = u + 1,
              code = ValidateError.NotNumber,
              wrong = c)
          )
          return true
        }

        u += 1
        v += 1
      }
    }
    else return true
    false
  }

  protected def checkAscii(name: String, offset: Int, count: Int, handler: FieldValidateHandler): Boolean = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!(32 <= c && c < 127)) {
        handler.error(
          FieldValidateError(
            name = name,
            offset = offset,
            value = abc(offset, count),
            length = count,
            column = u + 1,
            code = ValidateError.NotAscii,
            wrong = c)
        )
        return true
      }

      u += 1
      v += 1
    }
    false
  }

  protected def testAscii(offset: Int, count: Int): Unit = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!(32 <= c && c < 127)) throw new FixError.NotAsciiException(c, u)
      u += 1
      v += 1
    }
  }

  protected def testDigit(offset: Int, count: Int): Unit = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!('0' <= c && c <= '9')) throw new FixError.NotDigitException(c, u)
      u += 1
      v += 1
    }
  }

  protected def testDigitBlank(offset: Int, count: Int): Unit = {
    var c = rawData(offset)
    if (c == ' ') {
      var u = offset + 1
      var v = 1
      while ( {
        v < count
      }) {
        if (rawData(u) != ' ') throw new FixError.NotBlankException(c, u + 1)

        u += 1
        v += 1
      }
    }
    else {
      var u = offset
      var v = 0
      while ( {
        v < count
      }) {
        c = rawData(u)
        if (!('0' <= c && c <= '9')) throw new FixError.NotDigitException(c, u + 1)

        u += 1
        v += 1
      }
    }
  }

  protected def checkEqual(name: String, offset: Int, count: Int, handler: FieldValidateHandler, value: String): Boolean = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      if (rawData(u) != value.charAt(v)) {
        handler.error(
          FieldValidateError(
            name = name,
            offset = offset,
            value = abc(offset, count),
            length = count,
            column = u + 1,
            code = ValidateError.NotEqual,
            wrong = rawData(u))
        )
        return true
      }

      u += 1
      v += 1
    }
    false
  }

  protected def fill(offset: Int, count: Int, s: String): Unit = {
    if (s.length == count) setAsIs(s, offset)
    else if (s.length < count) throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    else throw new FixError.FieldOverFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
  }

  protected def checkLatin(name: String, offset: Int, count: Int, handler: FieldValidateHandler): Boolean = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
        handler.error(
          FieldValidateError(
            name = name,
            offset = offset,
            value = abc(offset, count),
            length = count,
            column = u + 1,
            code = ValidateError.NotLatin,
            wrong = c)
        )
        return true
      }

      u += 1
      v += 1
    }
    false
  }

  protected def testLatin(offset: Int, count: Int): Unit = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (!(32 <= c && c < 127) && !(160 <= c && c <= 255))
        throw new FixError.NotLatinException(c, u)
      u += 1
      v += 1
    }
  }

  protected def checkValid(name: String, offset: Int, count: Int, handler: FieldValidateHandler): Boolean = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (Character.isISOControl(c) || !Character.isDefined(c)) {
        handler.error(
          FieldValidateError(
            name = name,
            offset = offset,
            value = abc(offset, count),
            length = count,
            column = u + 1,
            code = ValidateError.NotValid,
            wrong = c)
        )
        return true
      }

      u += 1
      v += 1
    }
    false
  }

  protected def checkArray(name: String, offset: Int, count: Int, handler: FieldValidateHandler, domain: Array[String]): Boolean = {
    if (!domain.search(abc(offset, count)).isInstanceOf[scala.collection.Searching.Found]) {
      handler.error(
        FieldValidateError(
          name = name,
          offset = offset,
          value = abc(offset, count),
          length = count,
          code = ValidateError.NotDomain)
      )
      return true
    }
    false
  }

  protected def checkRegex(name: String, offset: Int, count: Int, handler: FieldValidateHandler, regex: Regex): Boolean = {
    if (!regex.matches(abc(offset, count))) {
      handler.error(
        FieldValidateError(
          name = name,
          offset = offset,
          value = abc(offset, count),
          length = count,
          code = ValidateError.NotMatch)
      )
      return true
    }
    false
  }

  protected def testArray(offset: Int, count: Int, domain: Array[String]): Unit = {
    val value = abc(offset, count)
    if (!domain.search(value).isInstanceOf[scala.collection.Searching.Found]) throw new FixError.NotDomainException(value)
  }

  protected def testRegex(offset: Int, count: Int, regex: Regex): Unit = {
    val value = abc(offset, count)
    if (!regex.matches(value)) throw new FixError.NotMatchesException(value)
  }

  protected def testValid(offset: Int, count: Int): Unit = {
    var u = offset
    var v = 0
    while ( {
      v < count
    }) {
      val c = rawData(u)
      if (Character.isISOControl(c) || !Character.isDefined(c)) throw new FixError.NotValidException(c, u)
      u += 1
      v += 1
    }
  }

  protected def dump(offset: Int, count: Int): String = {
    val sb = new scala.collection.mutable.StringBuilder
    for (k <- 0 until count) {
      var c = rawData(offset + k)
      if (c <= 32) c = (0x2400 + c).toChar
      else if (c == 127) c = '\u2421' // delete
      sb.append(c)
    }
    sb.toString
  }

  private def buildEmpty(length: Int): Unit = {
    this.rawData = new Array[Char](length)
    initialize()
  }

  private def buildFromString(length: Int, s: String, overflowError: Boolean, underflowError: Boolean): Unit = {
    if (s.length == length) rawData = s.toCharArray
    else if (s.length > length) {
      if (overflowError) throw new FixError.RecordOverflowException(FixEngine.RECORD_LENGTH + s.length + FixEngine.EXPECTED + length)
      rawData = util.Arrays.copyOfRange(s.toCharArray, 0, length)
    }
    else {
      if (underflowError) throw new FixError.RecordUnderflowException(FixEngine.RECORD_LENGTH + s.length + FixEngine.EXPECTED + length)
      this.rawData = new Array[Char](length)
      initialize()
      System.arraycopy(s.toCharArray, 0, rawData, 0, s.length)
    }
  }

  private def buildFromRecord(lrec: Int, r: FixRecord, overflowError: Boolean, underflowError: Boolean): Unit = {
    if (r.rawData.length == lrec) rawData = r.rawData
    else if (r.rawData.length > lrec) {
      if (overflowError) throw new FixError.RecordOverflowException(FixEngine.RECORD_LENGTH + r.rawData.length + FixEngine.EXPECTED + lrec)
      rawData = util.Arrays.copyOfRange(r.rawData, 0, lrec)
    }
    else {
      if (underflowError) throw new FixError.RecordUnderflowException(FixEngine.RECORD_LENGTH + r.rawData.length + FixEngine.EXPECTED + lrec)
      this.rawData = new Array[Char](lrec)
      initialize()
      System.arraycopy(r.rawData, 0, rawData, 0, r.rawData.length)
    }
  }

  //___ static like ___
  //
  protected def testAscii(value: String): Unit = {
    if (value == null) return
    val raw = value.toCharArray
    for (u <- 0 until raw.length) {
      val c = raw(u)
      if (!(32 <= c && c < 127)) throw new FixError.NotAsciiException(c, u)
    }
  }

  protected def testLatin(value: String): Unit = {
    if (value == null) return
    val raw = value.toCharArray
    for (u <- 0 until raw.length) {
      val c = raw(u)
      if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) throw new FixError.NotLatinException(c, u)
    }
  }

  protected def testValid(value: String): Unit = {
    if (value == null) return
    val raw = value.toCharArray
    for (u <- 0 until raw.length) {
      val c = raw(u)
      if (Character.isISOControl(c) || !Character.isDefined(c)) throw new FixError.NotValidException(c, u)
    }
  }

  protected def testArray(value: String, domain: Array[String]): Unit = {
    if (value == null) return
    if (!domain.search(value).isInstanceOf[scala.collection.Searching.Found]) throw new FixError.NotDomainException(value)
  }

  protected def testRegex(value: String, regex: Regex): Unit = {
    if (value == null) return
    if (!regex.matches(value)) throw new FixError.NotMatchesException(value)
  }

  protected def testDigit(value: String): Unit = {
    if (value == null) return
    val raw = value.toCharArray
    for (u <- 0 until raw.length) {
      val c = raw(u)
      if (!('0' <= c && c <= '9')) throw new FixError.NotDigitException(c, u)
    }
  }

  protected def testDigitBlank(value: String): Unit = {
    if (value == null) return
    val raw = value.toCharArray
    if (raw(0) == ' ') for (u <- 1 until raw.length) {
      val c = raw(u)
      if (c != ' ') throw new FixError.NotBlankException(c, u + 1)
    }
    else for (u <- 0 until raw.length) {
      val c = raw(u)
      if (!('0' <= c && c <= '9')) throw new FixError.NotDigitException(c, u + 1)
    }
  }

  protected def normalize(s: String,
                          overflowAction: OverflowAction.OverflowAction, underflowAction: UnderflowAction.UnderflowAction,
                          pad: Char, init: Char,
                          offset: Int, count: Int
                         ): String = FixEngine.normalize(s, overflowAction, underflowAction, pad, init, offset, count)

  /**
   * Returns the char value at the specified index.
   *
   * @param k the index of the char value.
   * @return the char value at the specified index of this string. The first char value is at index 1.
   * @throws IndexOutOfBoundsException if the index argument is not between 1 and the length of this daraRecord.
   */
  def charAt(k: Int): Char = {
    if (k < 1 || k > rawData.length) throw new IndexOutOfBoundsException(String.valueOf(k))
    rawData(k - 1)
  }
}

object FixEngine {
  private val FIELD_AT = "Field @"
  private val EXPECTED = " expected "
  private val CHARS_FOUND = " chars , found "
  private val FOR_FIELD_AT = "> for field @"
  private val INVALID_NUMERIC = "Invalid numeric value <"
  private val RECORD_LENGTH = "Record length "

  private def normalize(s: String,
                    overflowAction: OverflowAction.OverflowAction, underflowAction: UnderflowAction.UnderflowAction,
                    pad: Char, init: Char,
                    offset: Int, count: Int
                   ): String = {
    if (s == null) {
      if (underflowAction eq UnderflowAction.Error) throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + " null")
      fill(count, init)
    }
    else if (s.length == count) s
    else if (s.length < count) underflowAction match {
      case UnderflowAction.PadR =>
        rpad(s, count, pad)

      case UnderflowAction.PadL =>
        lpad(s, count, pad)

      case UnderflowAction.Error =>
        throw new FixError.FieldUnderFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
    else overflowAction match {
      case OverflowAction.TruncR =>
        rtrunc(s, count)

      case OverflowAction.TruncL =>
        ltrunc(s, count)

      case OverflowAction.Error =>
        throw new FixError.FieldOverFlowException(FixEngine.FIELD_AT + offset + FixEngine.EXPECTED + count + FixEngine.CHARS_FOUND + s.length)
    }
  }

  private def fill(t: Int, pad: Char) = CharBuffer.allocate(t).toString.replace('\u0000', pad)

  private def rpad(s: String, t: Int, pad: Char): String = {
    val len = s.length
    if (len > t) return s.substring(0, t)
    if (len == t) return s
    s + CharBuffer.allocate(t - len).toString.replace('\u0000', pad)
  }

  private def lpad(s: String, t: Int, pad: Char): String = {
    val len = s.length
    if (len > t) return s.substring(len - t)
    if (len == t) return s
    CharBuffer.allocate(t - len).toString.replace('\u0000', pad) + s
  }

  private def rtrunc(s: String, t: Int) = {
    val len = s.length
    if (len > t) s.substring(0, t)
    else s
  }

  private def ltrunc(s: String, t: Int) = {
    val len = s.length
    if (len > t) s.substring(len - t)
    else s
  }


}
