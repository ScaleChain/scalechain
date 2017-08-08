package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.domain.RpcResponse
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.util.StringUtil

import org.slf4j.LoggerFactory
import java.util.*

object RequestHandler : ServiceDispatcher {
  private val logger = LoggerFactory.getLogger(RequestHandler.javaClass)

  //implicit val implicitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

  val requestLog = java.io.FileWriter("./build/request-${Math.abs(Random().nextInt())}.log")
  //var requestCount = 0
  fun handleRequest(requestString : String) : String {
    //requestCount += 1
    //requestLog.write(s"Request<${requestCount}> : ${requestString}\n\n")
    logger.trace("String Request : ${requestString}")

    val request = Json.get().fromJson(requestString, RpcRequest::class.java)
/*
    assert( parsedRequest != null)

    val id = parsedRequest.asJsObject.fields("id")
    val method = parsedRequest.asJsObject.fields("method")
    val params = parsedRequest.asJsObject.fields("params")

    val request = RpcRequest(
      jsonrpc = None,
      id = id.convertTo<Long>,
      method = method.convertTo<String>,
      params = params.convertTo<RpcParams>
    )
*/
    val serviceResponse : RpcResponse = dispatch(request)
    val stringResponse = Json.get().toJson(serviceResponse)
    // Show the first 256 chars of the response.
    //requestLog.write(s"Response<${requestCount}> : ${StringUtil.getBrief(stringResponse,2048)}\n\n")
    //requestLog.flush()
    logger.trace("String Response : ${StringUtil.getBrief(stringResponse,2048)}")
    return stringResponse
  }
}
