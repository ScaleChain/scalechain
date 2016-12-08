package io.scalechain.blockchain.api.domain

import com.google.gson.*
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.api.Json
import java.lang.reflect.Type

interface ParameterValidator<T> {
  fun validate(paramName : String, paramIndex : Int, paramValue : T) : Unit
}

data class IntRangeValidator(val minValueOption : Int?, val maxValueOption : Int?) : ParameterValidator<Int> {
  override fun validate(paramName : String, paramIndex : Int, paramValue : Int) : Unit {
    if (minValueOption != null) {
      if (paramValue < minValueOption) {
        throw RpcException( ErrorCode.RpcArgumentLessThanMinValue, "A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption}.")
      }
    }
    if (maxValueOption != null) {
      if (paramValue > maxValueOption) {
        throw RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, "A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption}.")
      }
    }
  }
}

data class LongRangeValidator(val minValueOption : Long?, val maxValueOption : Long?) : ParameterValidator<Long> {
  override fun validate(paramName : String, paramIndex : Int, paramValue : Long) : Unit {
    if (minValueOption != null) {
      if (paramValue < minValueOption) {
        throw RpcException( ErrorCode.RpcArgumentLessThanMinValue, "A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption}.")
      }
    }
    if (maxValueOption != null) {
      if (paramValue > maxValueOption) {
        throw RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, "A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption}.")
      }
    }
  }
}

data class BigDecimalRangeValidator(val minValueOption : java.math.BigDecimal?, val maxValueOption : java.math.BigDecimal?) : ParameterValidator<java.math.BigDecimal> {
  override fun validate(paramName : String, paramIndex : Int, paramValue : java.math.BigDecimal) : Unit {
    if (minValueOption != null) {
      if (paramValue < minValueOption) {
        throw RpcException( ErrorCode.RpcArgumentLessThanMinValue, "A parameter $paramName at index $paramIndex is less than the minimum value ${minValueOption}.")
      }
    }
    if (maxValueOption != null) {
      if (paramValue > maxValueOption) {
        throw RpcException( ErrorCode.RpcArgumentGreaterThanMaxValue, "A parameter $paramName at index $paramIndex is greater than the minimum value ${maxValueOption}.")
      }
    }
  }
}

data class RpcParams(val paramValues:List<JsonElement>) {
  inline fun <reified T : Any> getListOption(name: String, index: Int): List<T>? {
    if (index < paramValues.size) {
      val value = paramValues[index]
      if (value == JsonNull.INSTANCE) {
        // TODO : Add test case for this branch.
        // bitcoind's signrawtransaction treats null as if the parameter was not passed.
        // {"id":1463951880819,"method":"signrawtransaction","params":["010000000142cc891b5688fcab5b4e377f8d6270763caea8860e84f556917a60ee7f51bcec0000000000ffffffff02400d0300000000001976a914d07e90533f40a45d5d8bc3f3c22803a5fdbeac1788aca096022a010000001976a91426368831bb36b9285664570379c6e699b9bc72a988ac00000000",[],null]}
        return null
      } else {
        if (value is JsonArray) {
          try {
            return value.toList().map {
              Json.get().fromJson<T>(it, T::class.java)
            }
          } catch (e: JsonSyntaxException) {
            throw RpcException(ErrorCode.RpcParameterTypeConversionFailure, e.message!!)
          }
        } else {
          return null
        }
// throw RpcException(ErrorCode.RpcParameterTypeConversionFailure, e.getMessage)
      }
    } else {
      return null
    }
  }

  inline fun <reified T : Any> getOption(name: String, index: Int, parameterValidators: List<ParameterValidator<T>> = listOf()): T? {
    if (index < paramValues.size) {
      val value = paramValues[index]

      if (value == JsonNull.INSTANCE) {
        // TODO : Add test case for this branch.
        // Make it consistent with getListOption. Treat null value as if no parameter was given.
        return null
      } else {
        try {
          val convertedValue = Json.get().fromJson<T>(value, T::class.java)

          // Make sure all validation checks succeed.
          parameterValidators.forEach { validator ->
            validator.validate(name, index, convertedValue)
          }

          return convertedValue
        } catch (e: JsonSyntaxException) {
          throw RpcException(ErrorCode.RpcParameterTypeConversionFailure, e.message!!)
        }
      }
    } else {
      return null
    }
  }

  inline fun <reified T : Any> get(name: String, index: Int, parameterValidators: List<ParameterValidator<T>> = listOf()): T {
    val valueOption = getOption<T>(name, index, parameterValidators)
    return valueOption ?:
        throw RpcException(ErrorCode.RpcMissingRequiredParameter, "A mandatory parameter $name at index $index is missing.")
  }

  inline fun <reified T : Any> getlistOf(name: String, index: Int): List<T> {
    val listOption = getListOption<T>(name, index)
    return listOption ?:
        throw RpcException(ErrorCode.RpcMissingRequiredParameter, "A mandatory parameter $name at index $index is missing.")

  }
}

class RpcParamsDeserializer : JsonDeserializer<RpcParams> {
  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RpcParams {
    if (json == null)
      throw IllegalArgumentException()
    when {
      json.isJsonArray() -> {
        val jsonArray = json.getAsJsonArray()
        return RpcParams(jsonArray.toList())
      }
      else -> {
        throw IllegalArgumentException()
      }
    }
  }
}
