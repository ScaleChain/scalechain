package io.scalechain.blockchain.api

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.api.domain.{RpcResponse, RpcParams, RpcRequest, RpcParamsJsonFormat}
import io.scalechain.util.StringUtil

import spray.json.DefaultJsonProtocol._
import spray.json._
import org.slf4j.LoggerFactory
import scala.util.Random

object RequestHandler : ServiceDispatcher {
  private lazy val logger = Logger(LoggerFactory.getLogger(RequestHandler.getClass))
  import RpcParamsJsonFormat._

  //implicit val implicitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

  import RpcResponseJsonFormat._

  val requestLog = java.io.FileWriter(s"./target/request-${Math.abs(Random.nextInt)}.log")
  //var requestCount = 0
  fun handleRequest(requestString : String) : String {
    //requestCount += 1
    //requestLog.write(s"Request<${requestCount}> : ${requestString}\n\n")
    logger.trace(s"String Request : ${requestString}")

    val parsedRequest = requestString.parseJson
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

    val serviceResponse : RpcResponse = dispatch(request)
    val stringResponse = serviceResponse.toJson.prettyPrint
    // Show the first 256 chars of the response.
    //requestLog.write(s"Response<${requestCount}> : ${StringUtil.getBrief(stringResponse,2048)}\n\n")
    //requestLog.flush()
    logger.trace(s"String Response : ${StringUtil.getBrief(stringResponse,2048)}")
    stringResponse
  }
}
