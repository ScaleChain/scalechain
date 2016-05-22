package io.scalechain.blockchain.api.domain

import io.scalechain.blockchain.{ErrorCode, RpcException}
import spray.json.DefaultJsonProtocol._
import spray.json._

trait ParameterValidator[T] {
  def validate(paramName : String, paramIndex : Int, paramValue : T) : Unit
}

case class IntRangeValidator(minValueOption : Option[Int], maxValueOption : Option[Int]) extends ParameterValidator[Int] {
  def validate(paramName : String, paramIndex : Int, paramValue : Int) : Unit = {
    if (minValueOption.isDefined) {
      if (paramValue < minValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentLessThanMinValue, s"A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption.get}.")
      }
    }
    if (maxValueOption.isDefined) {
      if (paramValue > maxValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, s"A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption.get}.")
      }
    }
  }
}

case class LongRangeValidator(minValueOption : Option[Long], maxValueOption : Option[Long]) extends ParameterValidator[Long] {
  def validate(paramName : String, paramIndex : Int, paramValue : Long) : Unit = {
    if (minValueOption.isDefined) {
      if (paramValue < minValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentLessThanMinValue, s"A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption.get}.")
      }
    }
    if (maxValueOption.isDefined) {
      if (paramValue > maxValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, s"A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption.get}.")
      }
    }
  }
}

case class BigDecimalRangeValidator(minValueOption : Option[scala.math.BigDecimal], maxValueOption : Option[scala.math.BigDecimal]) extends ParameterValidator[scala.math.BigDecimal] {
  def validate(paramName : String, paramIndex : Int, paramValue : scala.math.BigDecimal) : Unit = {
    if (minValueOption.isDefined) {
      if (paramValue < minValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentLessThanMinValue, s"A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption.get}.")
      }
    }
    if (maxValueOption.isDefined) {
      if (paramValue > maxValueOption.get) {
        throw new RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, s"A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption.get}.")
      }
    }
  }
}

case class RpcParams(paramValues:List[JsValue]) {
  def getListOption[T](name:String, index:Int)(implicit reader : spray.json.JsonReader[List[T]]): Option[List[T]] = {
    if (index < paramValues.length) {
      val value = paramValues(index)
      if ( value == JsNull) {
        // TODO : Add test case for this branch.
        // bitcoind's signrawtransaction treats null as if the parameter was not passed.
        // {"id":1463951880819,"method":"signrawtransaction","params":["010000000142cc891b5688fcab5b4e377f8d6270763caea8860e84f556917a60ee7f51bcec0000000000ffffffff02400d0300000000001976a914d07e90533f40a45d5d8bc3f3c22803a5fdbeac1788aca096022a010000001976a91426368831bb36b9285664570379c6e699b9bc72a988ac00000000",[],null]}
        None
      } else {
        try {
          val convertedValue = value.convertTo[List[T]]
          Some( convertedValue )
        } catch {
          case e : spray.json.DeserializationException => {
            throw new RpcException(ErrorCode.RpcParameterTypeConversionFailure, e.getMessage)
          }
        }
      }
    } else {
      None
    }
  }

  def getOption[T](name:String, index: Int, parameterValidators : List[ParameterValidator[T]] = List())(implicit reader : spray.json.JsonReader[T]): Option[T] = {
    if (index < paramValues.length) {
      val value = paramValues(index)

      if ( value == JsNull) {
        // TODO : Add test case for this branch.
        // Make it consistent with getListOption. Treat null value as if no parameter was given.
        None
      } else {
        try {
          val convertedValue = value.convertTo[T]

          // Make sure all validation checks succeed.
          parameterValidators.foreach { validator =>
            validator.validate(name, index, convertedValue)
          }

          Some( convertedValue )
        } catch {
          case e : spray.json.DeserializationException => {
            throw new RpcException(ErrorCode.RpcParameterTypeConversionFailure, e.getMessage)
          }
        }
      }
    } else {
      None
    }
  }

  def get[T](name:String, index: Int, parameterValidators : List[ParameterValidator[T]] = List())(implicit reader : spray.json.JsonReader[T]): T = {
    val valueOption = getOption[T](name, index, parameterValidators)
    valueOption.getOrElse {
      throw new RpcException( ErrorCode.RpcMissingRequiredParameter, s"A mandatory parameter $name at index $index is missing.")
    }
  }

  def getList[T](name:String, index: Int)(implicit reader : spray.json.JsonReader[List[T]]) : List[T] = {
    val listOption = getListOption[T](name, index)
    listOption.getOrElse {
      throw new RpcException( ErrorCode.RpcMissingRequiredParameter, s"A mandatory parameter $name at index $index is missing.")
    }
  }
}

object RpcParamsJsonFormat {
  implicit object rpcParamsJsonFormat extends RootJsonFormat[RpcParams] {
    def write( anyList : RpcParams ) = {
      // Not used.
      assert(false);
      "".toJson
    }

    def read( jsValue: JsValue ) : RpcParams = {
      jsValue match {
        case jsArray : JsArray => {
          RpcParams(jsArray.elements.toList)
        }
        case _ => {
          throw new DeserializationException("JsArray expected for the params field.")
        }
      }
    }
  }
}
