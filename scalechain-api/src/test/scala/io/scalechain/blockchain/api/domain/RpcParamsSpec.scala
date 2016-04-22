package io.scalechain.blockchain.api.domain

import io.scalechain.blockchain.{ErrorCode, RpcException}
import org.scalatest._
import spray.json.DefaultJsonProtocol._
import spray.json.{JsArray, JsNumber, JsString}

/**
  * Created by kangmo on 2/25/16.
  */
class RpcParamsSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  "getListOption" should "return some list if the parameter exists" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getListOption[String]("param1", 0) shouldBe Some(List("foo", "bar"))
  }

  "getListOption" should "return none if the parameter is missing" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getListOption[String]("param2", 1) shouldBe None
  }

  "getListOption" should "throw an exception if the parameter type mismatches" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getListOption[Long]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getListOption" should "throw an exception if the parameter is not an JsArray" in {
    val arguments = List( // array of parameters
      JsString("foo") // the first parameter is NOT an array
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getListOption[String]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getOption" should "return some value if the parameter exists" in {
    val arguments = List( // array of parameters
       JsString("foo")
    )

    val params = RpcParams(arguments)

    params.getOption[String]("param1", 0) shouldBe Some("foo")

  }

  "getOption" should "return none if the parameter is missing" in {
    val arguments = List( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    params.getOption[String]("param2", 1) shouldBe None

  }

  "getOption" should "throw an exception if the parameter type mismatches" in {
    val arguments = List( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getOption[Long]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

  }

  "getOption" should "return some value if the parameter validation succeeds" in {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : scala.math.BigDecimal = scala.math.BigDecimal(100)

    val arguments = List( // array of parameters
      JsNumber("100")
    )

    val params = RpcParams(arguments)

    params.getOption[Int]("param1", 0, List(IntRangeValidator(Some(1),Some(200)))) shouldBe Some(intValue)
    params.getOption[Long]("param1", 0, List(LongRangeValidator(Some(1L),Some(200L)))) shouldBe Some(longValue)
    params.getOption[scala.math.BigDecimal](
      "param1",
      0,
      List(BigDecimalRangeValidator(
            Some(scala.math.BigDecimal(1)),
            Some(scala.math.BigDecimal(200)))
      )
    ) shouldBe Some(decimalValue)
  }

  "getOption" should "throw an exception if the parameter validation fails" in {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : scala.math.BigDecimal = scala.math.BigDecimal(100)

    val arguments = List( // array of parameters
      JsNumber("0"),  // less than the min value
      JsNumber("201") // greater than the max value
    )

    val params = RpcParams(arguments)

    val intValidators = List(IntRangeValidator(Some(1),Some(200)))
    val longValidators = List(LongRangeValidator(Some(1L),Some(200L)))
    val bigDecimalValidators = List(BigDecimalRangeValidator(
      Some(scala.math.BigDecimal(1)),
      Some(scala.math.BigDecimal(200)))
    )

    (the [RpcException] thrownBy params.getOption[Int]("param1", 0, intValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the [RpcException] thrownBy params.getOption[Long]("param1", 0, longValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the [RpcException] thrownBy params.getOption[scala.math.BigDecimal]("param1", 0, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

    (the [RpcException] thrownBy params.getOption[Int]("param2", 1, intValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the [RpcException] thrownBy params.getOption[Long]("param2", 1, longValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the [RpcException] thrownBy params.getOption[scala.math.BigDecimal]("param2", 1, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
  }


  "get" should "return a value if the parameter exists" in {
    val arguments = List( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    params.get[String]("param1", 0) shouldBe "foo"
  }

  "get" should "throw an exception if a required parameter is missing" in {
    val arguments = List( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.get[String]("param2", 1)
    thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
  }

  "get" should "throw an exception if the parameter type mismatches" in {
    val arguments = List( // array of parameters
      JsString("foo")
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.get[Int]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure

  }

  "get" should "return an value if the parameter validation succeeds" in {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : scala.math.BigDecimal = scala.math.BigDecimal(100)

    val arguments = List( // array of parameters
      JsNumber("100")
    )

    val params = RpcParams(arguments)

    params.get[Int]("param1", 0, List(IntRangeValidator(Some(1),Some(200)))) shouldBe intValue
    params.get[Long]("param1", 0, List(LongRangeValidator(Some(1L),Some(200L)))) shouldBe longValue
    params.get[scala.math.BigDecimal](
      "param1",
      0,
      List(BigDecimalRangeValidator(
        Some(scala.math.BigDecimal(1)),
        Some(scala.math.BigDecimal(200)))
      )
    ) shouldBe decimalValue

  }


  "get" should "throw an exception if the parameter validation fails" in {
    val intValue : Int = 100
    val longValue : Long = 100L
    val decimalValue : scala.math.BigDecimal = scala.math.BigDecimal(100)

    val arguments = List( // array of parameters
      JsNumber("0"),  // less than the min value
      JsNumber("201") // greater than the max value
    )

    val params = RpcParams(arguments)

    val intValidators = List(IntRangeValidator(Some(1),Some(200)))
    val longValidators = List(LongRangeValidator(Some(1L),Some(200L)))
    val bigDecimalValidators = List(BigDecimalRangeValidator(
      Some(scala.math.BigDecimal(1)),
      Some(scala.math.BigDecimal(200)))
    )

    (the [RpcException] thrownBy params.get[Int]("param1", 0, intValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the [RpcException] thrownBy params.get[Long]("param1", 0, longValidators)).code shouldBe ErrorCode.RpcArgumentLessThanMinValue
    (the [RpcException] thrownBy params.get[scala.math.BigDecimal]("param1", 0, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentLessThanMinValue

    (the [RpcException] thrownBy params.get[Int]("param2", 1, intValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the [RpcException] thrownBy params.get[Long]("param2", 1, longValidators)).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
    (the [RpcException] thrownBy params.get[scala.math.BigDecimal]("param2", 1, bigDecimalValidators )).code shouldBe ErrorCode.RpcArgumentGreaterThanMaxValue
  }



  "getList" should "return a list if the parameter exists" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    params.getList[String]("param1", 0) shouldBe List("foo", "bar")

  }

  "getList" should "throw an exception if a required parameter is missing" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getList[String]("param2", 1)
    thrown.code shouldBe ErrorCode.RpcMissingRequiredParameter
  }

  "getList" should "throw an exception if the parameter type mismatches" in {
    val arguments = List( // array of parameters
      JsArray( // the first parameter is an array
        JsString("foo"),
        JsString("bar")
      )
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getList[Long]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

  "getList" should "throw an exception if the parameter is not an JsArray" in {
    val arguments = List( // array of parameters
      JsString("foo") // the first parameter is NOT an array
    )

    val params = RpcParams(arguments)

    val thrown = the [RpcException] thrownBy params.getList[String]("param1", 0)
    thrown.code shouldBe ErrorCode.RpcParameterTypeConversionFailure
  }

}