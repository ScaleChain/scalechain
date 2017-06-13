package io.scalechain.blockchain.api

import com.google.gson.*
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.api.command.network.GetPeerInfoResult
import io.scalechain.blockchain.api.command.oap.IssueAssetResultResult
import io.scalechain.blockchain.api.command.oap.ListAssetTransactionsResult
import io.scalechain.blockchain.api.command.wallet.ListAssetBalanceDescResult
import io.scalechain.blockchain.api.command.wallet.ListTransactionsResult
import io.scalechain.blockchain.api.command.wallet.ListUnspentResult
import io.scalechain.blockchain.api.domain.*
import io.scalechain.blockchain.api.http.ApiServer
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.HexUtil
import java.lang.reflect.Type

// TODO : Need to move to scalechain-api-domain?

class RpcResultSerializer : JsonSerializer<RpcResult> {
  private inline fun<reified T> toJsonArray( elements : List<T>) : JsonArray {
    val jsonArray = JsonArray()

    elements.forEach { element ->
      val jsonTree = Json.get().toJsonTree( element )
      jsonArray.add(jsonTree)
    }
    return jsonArray
  }
  override fun serialize(src: RpcResult?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    return when {
      src is StringResult -> JsonPrimitive(src.value)
      src is StringListResult -> toJsonArray(src.value)
      src is NumberResult -> JsonPrimitive(src.value)
      src is GetPeerInfoResult -> toJsonArray(src.peerInfos)
      src is ListTransactionsResult -> toJsonArray(src.transactionDescs)
      src is ListAssetTransactionsResult -> toJsonArray(src.transactionDescs)
      src is ListUnspentResult -> toJsonArray(src.unspentCoins)
      src is IssueAssetResultResult -> Json.get().toJsonTree(src.item)
      src is ListAssetBalanceDescResult -> toJsonArray(src.transactionDescs)
      else -> {
        Json.get().toJsonTree(src, typeOfSrc)
      }
    }
  }
}

class HashSerialzier : JsonSerializer<Hash> {
  override fun serialize(src: Hash?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    if (src == null) {
      return JsonNull.INSTANCE
    }
    else {
      return JsonPrimitive(HexUtil.hex(src.value.array) )
    }
  }
}

class RpcRequestDeserializer : JsonDeserializer<RpcRequest> {
  open class JsonParser( val fieldName : String ) {
    fun optional(json : JsonObject) : JsonElement? {
      if (json.has(fieldName)) return json.get(fieldName)
      else return null
    }
    fun required(json : JsonObject) : JsonElement {
      if (json.has(fieldName)) {
        return json.get(fieldName)
      } else throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field is missing.")
    }

    fun JsonElement.getLongValue() : Long {
      val value = try { this.asJsonPrimitive } catch(e:Exception) {
        throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be a number.")
      }
      if (value.isNumber()) {
        return value.asNumber.toLong()
      }
      else if (value.isString()) {
        try {
          return value.asString.toLong()
        } catch (e : NumberFormatException ) {
          throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be a number.")
        }
      } else throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be a number.")
    }

    fun JsonElement.getStringValue() : String {
      val value = try { this.asJsonPrimitive } catch(e:Exception) {
        throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be a number.")
      }
      if (value.isString) return value.asString
      else throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be a string.")
    }

    fun JsonElement.getArray() : List<JsonElement> {
      val value = try { this.asJsonArray } catch(e:Exception) {
        throw RpcException(ErrorCode.RpcRequestParseFailure, "${fieldName} field should be an array.")
      }
      return value.toList()
    }
  }


  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RpcRequest {
    if (json is JsonObject) {
      val jsonrpc : String?           = object : JsonParser("jsonrpc") { operator fun invoke() = optional(json)?.getStringValue() }()
      val id      : Long              = object : JsonParser("id")      { operator fun invoke() = required(json).getLongValue()    }()
      val method  : String            = object : JsonParser("method")  { operator fun invoke() = required(json).getStringValue()  }()
      val params  : List<JsonElement> = object : JsonParser("params")  { operator fun invoke() = required(json).getArray()        }()
      return RpcRequest(jsonrpc, id, method, RpcParams(params))
    } else {
      throw RpcException(ErrorCode.RpcRequestParseFailure, "Expected Json object for the rpc request.")
    }
  }
}

/** Serializes RpcResponse to Json string.
  */
class RpcResponseSerializer : JsonSerializer<RpcResponse> {
  private inline fun <reified T> toJsonArray(elements: List<T>): JsonArray {
    val jsonArray = JsonArray()

    elements.forEach { element ->
      val jsonTree = Json.get().toJsonTree(element)
      jsonArray.add(jsonTree)
    }
    return jsonArray
  }

  override fun serialize(src: RpcResponse?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    if (src == null) throw IllegalArgumentException()

    val jsonObject = JsonObject()

    jsonObject.add(
        "result",
        if (src.result == null) {
          JsonNull.INSTANCE
        } else {
          Json.get().toJsonTree(src.result)
        }
    )

    jsonObject.add(
        "error",
        if (src.error == null) {
          JsonNull.INSTANCE
        } else {
          Json.get().toJsonTree(src.error)
        }
    )

    jsonObject.add("id", JsonPrimitive(src.id))

    return jsonObject
  }
}


/**
 * Created by kangmo on 01/12/2016.
 */
object Json {
  var gson : Gson? = null
  fun get() : Gson {
    if (gson == null) {
      gson = GsonBuilder()
                .registerTypeAdapter(Hash::class.java, HashSerialzier())
                .registerTypeAdapter(RpcParams::class.java, RpcParamsDeserializer())
                .registerTypeAdapter(RpcRequest::class.java, RpcRequestDeserializer())
                .registerTypeAdapter(RpcResponse::class.java, RpcResponseSerializer())
                .registerTypeAdapter(StringResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(StringListResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(NumberResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(GetPeerInfoResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListTransactionsResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListUnspentResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListAssetTransactionsResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(IssueAssetResultResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListAssetBalanceDescResult::class.java, RpcResultSerializer())
                .setPrettyPrinting()
                .serializeNulls()
                .create()
    }
    return gson!!
  }
}

open class JsonRpcMicroservice {
  fun runService(inboundPort : Int) {
    ApiServer().listen(inboundPort)
  }

  companion object : JsonRpcMicroservice()
}

