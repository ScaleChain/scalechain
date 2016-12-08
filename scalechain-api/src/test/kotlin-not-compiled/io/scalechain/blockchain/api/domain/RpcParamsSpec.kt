package io.scalechain.blockchain.api.domain

import io.scalechain.blockchain.{ErrorCode, RpcException}
import org.scalatest.*
import spray.json.DefaultJsonProtocol.*
import spray.json.{JsArray, JsNumber, JsString}

/**
  * Created by kangmo on 2/25/16.
  */
@Ignore
class RpcParamsSpec : FlatSpec with BeforeAndAfterEach with Matchers {
  this: Suite =>

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  // These tests are not passing. We will make it pass soon.
  "getListOption" should "return some list if the parameter exists" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getListOption<String>("param1", 0) shouldBe listOf("foo", "bar"))
  }

  "getListOption" should "return none if the parameter is missing" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getListOption<String>("param2", 1) shouldBe null
  }

  "getListOption" should "throw an exception if the parameter type mismatches" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getListOption<Long>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getListOption" should "throw an exception if the parameter is not an JsArray" {
    val arguments = listOf( // array of parameters
      JsString("foo") // the first parameter is NOT an array
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getListOption<String>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getOption" should "return some value if the parameter exists" {
    val arguments = listOf( // array of parameters
       JsString("foo")
    )

    val params = RpcParams(arguments)

    params.getOption<String>("param1", 0) shouldBe "foo")

  }

  "getOption" should "return none if the parameter is missing" {
    val arguments = listOf( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    params.getOption<String>("param2", 1) shouldBe null

  }

  "getOption" should "throw an exception if the parameter type mismatches" {
    val arguments = listOf( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getOption<Long>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

  }

  "getOption" should "return some value if the parameter validation succeeds" {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : java.math.BigDecimal = java.math.BigDecimal(100)

    val arguments = listOf( // array of parameters
      JsNumber("100")
    )

    val params = RpcParams(arguments)

    params.getOption<Int>("param1", 0, listOf(IntRangeValidator(Some(1),Some(200)))) shouldBe intValue)
    params.getOption<Long>("param1", 0, listOf(LongRangeValidator(Some(1L),Some(200L)))) shouldBe longValue)
    params.getOption<java.math.BigDecimal>(
      "param1",
      0,
      listOf(BigDecimalRangeValidator(
            Some(java.math.BigDecimal(1)),
            Some(java.math.BigDecimal(200)))
      )
    ) shouldBe decimalValue)
  }

  "getOption" should "throw an exception if the parameter validation fails" {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : java.math.BigDecimal = java.math.BigDecimal(100)

    val arguments = listOf( // array of parameters
      JsNumber("0"),  // less than the min value
      JsNumber("201") // greater than the max value
    )

    val params = RpcParams(arguments)

    val intValidators = listOf(IntRangeValidator(Some(1),Some(200)))
    val longValidators = listOf(LongRangeValidator(Some(1L),Some(200L)))
    val bigDecimalValidators = listOf(BigDecimalRangeValidator(
      Some(java.math.BigDecimal(1)),
      Some(java.math.BigDecimal(200)))
    )

    (the <RpcException> thrownBy params.getOption<Int>("param1", 0, intValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the <RpcException> thrownBy params.getOption<Long>("param1", 0, longValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the <RpcException> thrownBy params.getOption<java.math.BigDecimal>("param1", 0, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

    (the <RpcException> thrownBy params.getOption<Int>("param2", 1, intValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the <RpcException> thrownBy params.getOption<Long>("param2", 1, longValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the <RpcException> thrownBy params.getOption<java.math.BigDecimal>("param2", 1, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
  }


  "get" should "return a value if the parameter exists" {
    val arguments = listOf( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    params.get<String>("param1", 0) shouldBe "foo"
  }

  "get" should "throw an exception if a required parameter is missing" {
    val arguments = listOf( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.get<String>("param2", 1)
    thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
  }

  "get" should "throw an exception if the parameter type mismatches" {
    val arguments = listOf( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.get<Int>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

  }

  "get" should "return an value if the parameter validation succeeds" {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : java.math.BigDecimal = java.math.BigDecimal(100)

    val arguments = listOf( // array of parameters
      JsNumber("100")
    )

    val params = RpcParams(arguments)

    params.get<Int>("param1", 0, listOf(IntRangeValidator(Some(1),Some(200)))) shouldBe intValue
    params.get<Long>("param1", 0, listOf(LongRangeValidator(Some(1L),Some(200L)))) shouldBe longValue
    params.get<java.math.BigDecimal>(
      "param1",
      0,
      listOf(BigDecimalRangeValidator(
        Some(java.math.BigDecimal(1)),
        Some(java.math.BigDecimal(200)))
      )
    ) shouldBe decimalValue

  }


  "get" should "throw an exception if the parameter validation fails" {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : java.math.BigDecimal = java.math.BigDecimal(100)

    val arguments = listOf( // array of parameters
      JsNumber("0"),  // less than the min value
      JsNumber("201") // greater than the max value
    )

    val params = RpcParams(arguments)

    val intValidators = listOf(IntRangeValidator(Some(1),Some(200)))
    val longValidators = listOf(LongRangeValidator(Some(1L),Some(200L)))
    val bigDecimalValidators = listOf(BigDecimalRangeValidator(
      Some(java.math.BigDecimal(1)),
      Some(java.math.BigDecimal(200)))
    )

    (the <RpcException> thrownBy params.get<Int>("param1", 0, intValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the <RpcException> thrownBy params.get<Long>("param1", 0, longValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the <RpcException> thrownBy params.get<java.math.BigDecimal>("param1", 0, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

    (the <RpcException> thrownBy params.get<Int>("param2", 1, intValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the <RpcException> thrownBy params.get<Long>("param2", 1, longValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the <RpcException> thrownBy params.get<java.math.BigDecimal>("param2", 1, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
  }



  "getList" should "return a list if the parameter exists" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getList<String>("param1", 0) shouldBe listOf("foo", "bar")

  }

  "getList" should "throw an exception if a required parameter is missing" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getList<String>("param2", 1)
    thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
  }

  "getList" should "throw an exception if the parameter type mismatches" {
    val arguments = listOf( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getList<Long>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getList" should "throw an exception if the parameter is not an JsArray" {
    val arguments = listOf( // array of parameters
      JsString("foo") // the first parameter is NOT an array
    )

    val params = RpcParams(arguments)

    val thrown = the <RpcException> thrownBy params.getList<String>("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }
}