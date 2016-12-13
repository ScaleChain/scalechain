package io.scalechain.blockchain.api.domain

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.RpcException
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
  * Created by kangmo on 2/25/16.
  */
// BUGBUG : This test was ignored previously
@RunWith(KTestJUnitRunner::class)
class RpcParamsSpec : FlatSpec(), Matchers {

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

  init {
    // These tests are not passing. We will make it pass soon.
    "getListOption" should "return some list if the parameter exists" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      params.getListOption<String>("param1", 0) shouldBe listOf("foo", "bar")
    }

    "getListOption" should "return none if the parameter is missing" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      params.getListOption<String>("param2", 1) shouldBe null
    }

    "getListOption" should "throw an exception if the parameter type mismatches" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> {
        params.getListOption<Long>("param1", 0)
      }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
    }

    "getListOption" should "throw an exception if the parameter is not an JsArray" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo") // the first parameter is NOT an array
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> {
        params.getListOption<String>("param1", 0)
      }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
    }

    "getOption" should "return some value if the parameter exists" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      params.getOption<String>("param1", 0) shouldBe "foo"

    }

    "getOption" should "return none if the parameter is missing" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      params.getOption<String>("param2", 1) shouldBe null

    }

    "getOption" should "throw an exception if the parameter type mismatches" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> {
        params.getOption<Long>("param1", 0)
      }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

    }

    "getOption" should "return some value if the parameter validation succeeds" {
      val intValue : Int = 100
      val longValue : Long = 100L
      val decimalValue : BigDecimal = BigDecimal(100)

      val arguments = listOf( // array of parameters
        JsonPrimitive("100")
      )

      val params = RpcParams(arguments)

      params.getOption<Int>("param1", 0, listOf(IntRangeValidator(1, 200))) shouldBe intValue
      params.getOption<Long>("param1", 0, listOf(LongRangeValidator(1L, 200L))) shouldBe longValue
      params.getOption<BigDecimal>(
        "param1",
        0,
        listOf(BigDecimalRangeValidator(
              BigDecimal(1),
              BigDecimal(200))
        )
      ) shouldBe decimalValue
    }

    "getOption" should "throw an exception if the parameter validation fails" {

      val arguments = listOf( // array of parameters
        JsonPrimitive("0"),  // less than the min value
        JsonPrimitive("201") // greater than the max value
      )

      val params = RpcParams(arguments)

      val intValidators = listOf(IntRangeValidator(1, 200))
      val longValidators = listOf(LongRangeValidator(1L, 200L))
      val bigDecimalValidators = listOf(BigDecimalRangeValidator(
        BigDecimal(1),
        BigDecimal(200))
      )

      (shouldThrow<RpcException> {params.getOption<Int>("param1", 0, intValidators)}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
      (shouldThrow<RpcException> {params.getOption<Long>("param1", 0, longValidators)}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
      (shouldThrow<RpcException> {params.getOption<BigDecimal>("param1", 0, bigDecimalValidators )}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

      (shouldThrow<RpcException> {params.getOption<Int>("param2", 1, intValidators)}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
      (shouldThrow<RpcException> {params.getOption<Long>("param2", 1, longValidators)}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
      (shouldThrow<RpcException> {params.getOption<BigDecimal>("param2", 1, bigDecimalValidators )}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    }


    "get" should "return a value if the parameter exists" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      params.get<String>("param1", 0) shouldBe "foo"
    }

    "get" should "throw an exception if a required parameter is missing" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow<RpcException> {
        params.get<String>("param2", 1)
      }
      thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
    }

    "get" should "throw an exception if the parameter type mismatches" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo")
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> {
        params.get<Int>("param1", 0)
      }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

    }

    "get" should "return an value if the parameter validation succeeds" {
      val intValue : Int = 100
      val longValue : Long = 100L
      val decimalValue : BigDecimal = BigDecimal(100)

      val arguments = listOf( // array of parameters
        JsonPrimitive("100")
      )

      val params = RpcParams(arguments)

      params.get<Int>("param1", 0, listOf(IntRangeValidator(1, 200))) shouldBe intValue
      params.get<Long>("param1", 0, listOf(LongRangeValidator(1L, 200L))) shouldBe longValue
      params.get<BigDecimal>(
        "param1",
        0,
        listOf(BigDecimalRangeValidator(
          BigDecimal(1),
          BigDecimal(200))
        )
      ) shouldBe decimalValue

    }


    "get" should "throw an exception if the parameter validation fails" {

      val arguments = listOf( // array of parameters
        JsonPrimitive("0"),  // less than the min value
        JsonPrimitive("201") // greater than the max value
      )

      val params = RpcParams(arguments)

      val intValidators = listOf(IntRangeValidator(1, 200))
      val longValidators = listOf(LongRangeValidator(1L, 200L))
      val bigDecimalValidators = listOf(BigDecimalRangeValidator(
        BigDecimal(1),
        BigDecimal(200))
      )

      (shouldThrow<RpcException> {params.get<Int>("param1", 0, intValidators)}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
      (shouldThrow<RpcException> {params.get<Long>("param1", 0, longValidators)}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
      (shouldThrow<RpcException> {params.get<BigDecimal>("param1", 0, bigDecimalValidators )}).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

      (shouldThrow<RpcException> {params.get<Int>("param2", 1, intValidators)}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
      (shouldThrow<RpcException> {params.get<Long>("param2", 1, longValidators)}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
      (shouldThrow<RpcException> {params.get<BigDecimal>("param2", 1, bigDecimalValidators )}).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    }



    "getList" should "return a list if the parameter exists" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      params.getList<String>("param1", 0) shouldBe listOf("foo", "bar")

    }

    "getList" should "throw an exception if a required parameter is missing" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> { params.getList<String>("param2", 1) }
      thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
    }

    "getList" should "throw an exception if the parameter type mismatches" {
      val arrayParam = JsonArray()
      arrayParam.add("foo")
      arrayParam.add("bar")
      val arguments = listOf( // array of parameters
        arrayParam // the first parameter is an array
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> { params.getList<Long>("param1", 0) }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
    }

    "getList" should "throw an exception if the parameter is not an JsArray" {
      val arguments = listOf( // array of parameters
        JsonPrimitive("foo") // the first parameter is NOT an array
      )

      val params = RpcParams(arguments)

      val thrown = shouldThrow <RpcException> { params.getList<String>("param1", 0) }
      thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
    }
  }
}