package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.domain.{RpcResponse, RpcParams, RpcRequest, RpcParamsJsonFormat}
import io.scalechain.util.StringUtil
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

object RequestHandler extends ServiceDispatcher {
  private lazy val logger = LoggerFactory.getLogger(RequestHandler.getClass)
  import RpcParamsJsonFormat._

  //implicit val implicitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

  import RpcResponseJsonFormat._

  def handleRequest(requestString : String) : String = {
    logger.info(s"String Request : ${requestString}")

    val parsedRequest = requestString.parseJson
    assert( parsedRequest != null)

    val id = parsedRequest.asJsObject.fields("id")
    val method = parsedRequest.asJsObject.fields("method")
    val params = parsedRequest.asJsObject.fields("params")

    val request = RpcRequest(
      jsonrpc = None,
      id = id.convertTo[Long],
      method = method.convertTo[String],
      params = params.convertTo[RpcParams]
    )

    val serviceResponse : RpcResponse = dispatch(request)
    val stringResponse = serviceResponse.toJson.prettyPrint
    // Show the first 256 chars of the response.
    logger.info(s"String Response : ${StringUtil.getBrief(stringResponse,1024)}")
    stringResponse
  }
}
