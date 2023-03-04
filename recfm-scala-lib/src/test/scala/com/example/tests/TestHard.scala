package com.example.tests

import io.github.epi155.recfm.scala.{FieldValidateError, FixError}
import org.scalatest.funsuite.AnyFunSuite
import com.example.syss.test.{FooAlpha, FooCustom, FooDigit, FooDom}

import java.nio.CharBuffer

class TestHard extends AnyFunSuite {

  test("Abc") {
    val alpha = new FooAlpha()
    // Field @0 expected 4 chars, found  null
    assertThrows[FixError.FieldUnderFlowException] {
      alpha.strict = null
    }

    // Field @0 expected 4 chars, found 3
    assertThrows[FixError.FieldUnderFlowException] {
      alpha.strict = "123"
    }

    // Field @0 expected 4 chars, found 5
    assertThrows[FixError.FieldOverFlowException] {
      alpha.strict = "12345"
    }

    alpha.weak = null
    alpha.weak = "123"
    alpha.weak = "12345"

    // Record length 3 expected 10
    assertThrows[FixError.RecordUnderflowException] {
      FooAlpha.decode("123")
    }

    // Record length 12 expected 10
    assertThrows[FixError.RecordOverflowException] {
      FooAlpha.decode("123456789012")
    }

    // Offending char: U+0000 @+1
    assertThrows[FixError.NotAsciiException] {
      alpha.strict = "\u0000"
    }

    alpha.weak = null

    // Offending char: U+0000 @+1
    assertThrows[FixError.NotLatinException] {
      alpha.weak = "\u0000"
    }

    alpha.utf8 = null

    // Offending char: U+0000 @+1
    assertThrows[FixError.NotValidException] {
      alpha.utf8 = "\u0000"
    }

    alpha.all = "\u0000"
    alpha.all = null

    alpha.weak
    alpha.utf8
    val s = alpha.encode

    if (!alpha.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    val a = FooAlpha.decode(CharBuffer.allocate(10).toString)

    assertThrows[FixError.NotAsciiException] {
      a.strict
    }
    assertThrows[FixError.NotLatinException] {
      a.weak
    }
    assertThrows[FixError.NotValidException] {
      a.utf8
    }

    a.all
  }

  test("Num") {
    val digit = new FooDigit()

    assertThrows[FixError.FieldUnderFlowException] {
      digit.strict = null
    }
    assertThrows[FixError.FieldUnderFlowException] {
      digit.strict = "123"
    }
    assertThrows[FixError.FieldOverFlowException] {
      digit.strict = "12345"
    }
    digit.weak = null
    digit.weak = "123"
    digit.weak = "12345"

    assertThrows[FixError.NotMatchesException] {
      digit.rex = "Hi"
    }


    val alpha = FooAlpha.of(digit) // cast

    //val numer = digit.copy // clone / deep-copy

    if (!digit.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    val n = FooDigit.decode(CharBuffer.allocate(10).toString)
    if (!n.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    assertThrows[FixError.NotDigitException] {
      n.strict
    }
    assertThrows[FixError.NotMatchesException] {
      n.rex
    }

    n.rex = "11"
    println(n.rex)
    if (!n.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    assertThrows[FixError.NotMatchesException] {
      n.rex = null
    }

    FooDigit.decode("123")
    FooDigit.decode("123456789012")
  }
  test("Cus") {
    val cust = new FooCustom();

    assertThrows[FixError.FieldUnderFlowException] {
      cust.fix = null
    }
    assertThrows[FixError.FieldUnderFlowException] {
      cust.fix = "a"
    }
    assertThrows[FixError.FieldOverFlowException] {
      cust.fix = "12345"
    }
    cust.fix = "ab"

    cust.lft = "a";
    println(cust.lft)
    cust.lft = "ab";
    println(cust.lft)
    cust.lft = "abcdefg";
    println(cust.lft)
    cust.lft = null;
    println(cust.lft)

    cust.rgt = "a";
    println(cust.rgt)
    cust.rgt = "abc";
    println(cust.rgt)
    cust.rgt = "abcdefg";
    println(cust.rgt)
    cust.rgt = null;
    println(cust.rgt)

    assertThrows[FixError.NotDigitException] {
      cust.dig = "a"
    }

    cust.dig = "1"
    cust.dig = "  "
    cust.dig = "     "
    cust.dig = "12345"
    cust.dig = null
    println(cust.toString)
    cust.charAt(1)

    assertThrows[java.lang.IndexOutOfBoundsException] {
      cust.charAt(0)
    }
    assertThrows[java.lang.IndexOutOfBoundsException] {
      cust.charAt(11)
    }

    if (!cust.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    val cu1 = FooCustom.decode(CharBuffer.allocate(10).toString.replace('\u0000', ' '))
    if (!cu1.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    val cu2 = FooCustom.decode(CharBuffer.allocate(10).toString.replace('\u0000', '*'))
    if (!cu2.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")
    assertThrows[FixError.NotDigitException] {
      cu2.dig
    }

    val cu3 = FooCustom.decode("12345678x0")
    if (!cu3.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")
    assertThrows[FixError.NotDigitException] {
      cu3.dig
    }

    val cu4 = FooCustom.decode("1234567 x0")
    if (!cu4.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")
    assertThrows[FixError.NotBlankException] {
      cu4.dig
    }

  }
  test("Dom") {
    val dom = new FooDom();

//    dom.cur = null
//    val domNull = dom.cur
//    assert("EUR".equals(domNull))

    assertThrows[FixError.NotDomainException] { dom.cur = "AAA" }
    dom.cur = "USD"
    if (!dom.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")

    val d1 = FooDom.decode("AAA")
    if (!d1.validateFails((it) => System.out.printf("Error field %s@%d+%d: %s%n", it.name, it.offset, it.length, it.code))) System.out.println("Valid Date")
    assertThrows[FixError.NotDomainException] { d1.cur }
  }
}