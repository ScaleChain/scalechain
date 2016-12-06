package io.scalechain.blockchain.api

import com.google.gson.*
import io.scalechain.blockchain.api.command.network.GetPeerInfoResult
import io.scalechain.blockchain.api.command.wallet.ListTransactionsResult
import io.scalechain.blockchain.api.command.wallet.ListUnspentResult
import io.scalechain.blockchain.api.domain.*
import io.scalechain.blockchain.api.http.ApiServer
import java.lang.reflect.Type

//import scala.io.StdIn

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
      src is ListUnspentResult -> toJsonArray(src.unspentCoins)
      else -> {
        Json.get().toJsonTree(src, typeOfSrc)
      }
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
                .registerTypeAdapter(RpcParams::class.java, RpcParamsDeserializer())
                .registerTypeAdapter(RpcResponse::class.java, RpcResponseSerializer())
                .registerTypeAdapter(StringResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(StringListResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(NumberResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(GetPeerInfoResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListTransactionsResult::class.java, RpcResultSerializer())
                .registerTypeAdapter(ListUnspentResult::class.java, RpcResultSerializer())
                .setPrettyPrinting()
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

