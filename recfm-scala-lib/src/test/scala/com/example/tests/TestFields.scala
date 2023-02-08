package com.example.tests

import com.example.syss.test.FooTest
import io.github.epi155.recfm.scala.{FieldValidateError, FixError}
import org.scalatest.funsuite.AnyFunSuite

class TestFields extends AnyFunSuite {
  val foo = new FooTest

  test("Abc") {
    foo.alpha01 = "A"
    assert("A         " === foo.alpha01)

    foo.alpha01 = "precipitevolissimevolmente"
    assert("precipitev" === foo.alpha01)

    assertThrows[FixError.NotAsciiException] {  foo.alpha01 = "Niña" }
    assertThrows[FixError.NotAsciiException] { foo.alpha02 = "Niña" }
    assertThrows[FixError.NotLatinException] { foo.alpha03 = "10 €" }
    assertThrows[FixError.NotValidException] { foo.alpha04 = "Los\u2fe0Ageles" }
    foo.alpha05 = "Los\u2fe0Ageles"
  }

  test("Num") {
    foo.digit01 = 12
    assert("00012" === foo.digit01)
    foo.digit01 = "1415926535897932384626433832"
    assert("33832" === foo.digit01)
    assertThrows[FixError.NotDigitException] {
      foo.digit01 = "one"
    }
  }

  def onError(it: FieldValidateError): Unit = {
    println(s"Error at field ${it.name}@${it.offset}+${it.length}, column ${it.column}: ${it.code}")
  }

  test("Cus") {
    foo.custom01 = "12"
    assert("00012" === foo.custom01)
    foo.custom01 = "1415926535897932384626433832"
    assert("33832" === foo.custom01)
    assertThrows[FixError.NotDigitException] {
      foo.custom01 = "three"
    }
    assertThrows[FixError.NotDigitException] {
      foo.custom01 = " "
    }
    assertThrows[FixError.NotBlankException] {
      foo.custom01 = " 1234"
    }
    foo.custom01 = "     "

    foo.custom02 = "12"
    assert("00012" === foo.custom02)
    foo.custom02 = "1415926535897932384626433832"
    assert("33832" === foo.custom02)
    assertThrows[FixError.NotDigitException] {
      foo.custom02 = "three"
    }
    assertThrows[FixError.NotDigitException] {
      foo.custom02 = " "
    }
    assertThrows[FixError.NotDigitException] {
      foo.custom02 = " 1234"
    }
    assertThrows[FixError.NotDigitException] {
      foo.custom02 = "     "
    }

    foo.custom03 = "12"
    assert("12   " === foo.custom03)
    foo.custom03 = "1415926535897932384626433832"
    assert("14159" === foo.custom03)
    foo.custom03 = "three"
    assertThrows[FixError.NotAsciiException] {
      foo.custom03 = "Niña"
    }

    foo.custom07 = "12"
    assert("12   " === foo.custom07)
    foo.custom07 = "1415926535897932384626433832"
    assert("14159" === foo.custom07)
    assertThrows[FixError.NotMatchesException] {
      foo.custom07 = "three"
    }
    foo.validateFails(it => onError(it))
  }

  test("Dom") {
    assert("EUR" === foo.domain01)
    foo.domain01 = "USD"
    assert("USD" === foo.domain01)
    assertThrows[FixError.NotDomainException] { foo.domain01 = "EURO" }
    assertThrows[FixError.NotDomainException] { foo.domain01 = "AUD" }
    foo.hackDom1 = "AUD"
    assertThrows[FixError.NotDomainException] { val x = foo.domain01  }
  }

  test("Grp") {
    foo.group01.alpha01 = "HELLO"
    assert("HELLO     " === foo.group01.alpha01)
    foo.group01.alpha01 = "HELLO WORLD"
    assert("HELLO WORL" === foo.group01.alpha01)
    assertThrows[FixError.NotAsciiException] { foo.group01.alpha01 = "привет" }

    foo.group01.digit01 = "12"
    assert("00012" === foo.group01.digit01)
    foo.group01.digit01 = "1415926535897932384626433832"
    assert("33832" === foo.group01.digit01)
    assertThrows[FixError.NotDigitException] { foo.group01.digit01 = "one" }

    foo.group01.custom01 = "12"
    assert("12   " === foo.group01.custom01)
    foo.group01.custom01 = "1415926535897932384626433832"
    assert("14159" === foo.group01.custom01)
    foo.group01.custom01 = "three"
    assertThrows[FixError.NotAsciiException] { foo.group01.custom01 = "Niña" }
  }

  test("Occ") {
    foo.errors.count = 2
    foo.errors.item(1).code = "NUL-PTR"
    foo.errors.item(1).message = "Null Pointer"

    val errs = foo.errors
    errs.item(5).code = "oo"
    assertThrows[ArrayIndexOutOfBoundsException] {  errs.item(0) }
    assertThrows[ArrayIndexOutOfBoundsException] {  errs.item(6).code = "00" }

    println(foo)

  }
}
