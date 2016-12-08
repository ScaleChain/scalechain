package io.scalechain.blockchain.api.http

import com.typesafe.scalalogging.Logger
import io.netty.buffer.Unpooled
import io.netty.buffer.ByteBuf
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelFutureListener
import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.HttpHeaders.Names.*
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.HttpVersion.*
import io.netty.handler.codec.http.*
import io.netty.util.ReferenceCountUtil
import io.netty.util.CharsetUtil
import io.scalechain.blockchain.api.RequestHandler
import io.scalechain.blockchain.net.p2p.NodeThrottle
import io.scalechain.util.StackUtil
import io.scalechain.util.ExceptionUtil
import org.slf4j.LoggerFactory
import java.util.*

class ApiServerHandler : SimpleChannelInboundHandler<Any>() {
  private val logger = LoggerFactory.getLogger(ApiServerHandler::class.java)

  private var request: HttpRequest? = null
  /** Buffer that stores the response content */
  private final val requestData: StringBuilder = StringBuilder()

  override fun channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush()
  }

  override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
    if (msg is HttpRequest) {
      val request: HttpRequest = msg
      this.request = request
      if (HttpHeaders.is100ContinueExpected(request)) {
        ApiServerHandler.send100Continue(ctx)
      }
      requestData.setLength(0)
    }
    if (msg is HttpContent) {
      val httpContent: HttpContent = msg
      val content: ByteBuf = httpContent.content()
      if (content.isReadable) {
        requestData.append(content.toString(CharsetUtil.UTF_8))
      }
      if (msg is LastHttpContent) {
        val trailer: LastHttpContent = msg

        val responseString = RequestHandler.handleRequest(requestData.toString())

        if (!writeResponse(trailer, ctx, responseString)) {
          ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }

        NodeThrottle.throttle(logger)
      }
    }
  }

  private fun writeResponse(currentObj: HttpObject, ctx: ChannelHandlerContext, responseString : String): Boolean {
    val keepAlive: Boolean = HttpHeaders.isKeepAlive(request)
    val response: FullHttpResponse = DefaultFullHttpResponse(HTTP_1_1, if (currentObj.getDecoderResult().isSuccess) OK else BAD_REQUEST, Unpooled.copiedBuffer(responseString, CharsetUtil.UTF_8))
    response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8")
    if (keepAlive) {
      response.headers().set(CONTENT_LENGTH, response.content().readableBytes())
      response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    }
    /*
    val cookieString: String = request.headers.get(COOKIE)
    if (cookieString != null) {
      val cookies: util.Set<Cookie> = CookieDecoder.decode(cookieString)
      if (!cookies.isEmpty) {
        import scala.collection.JavaConversions.*
        for (cookie <- cookies) {
          response.headers.add(SET_COOKIE, ServerCookieEncoder.encode(cookie))
        }
      }
    }
    else {
      response.headers.add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"))
      response.headers.add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"))
    }
    */
    ctx.write(response)
    return keepAlive
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    val causeDescription = ExceptionUtil.describe( cause.cause )
    logger.error("${cause}. Stack : ${StackUtil.getStackTrace(cause)} ${causeDescription}")
    ctx.close()
  }

  companion object {
    private fun send100Continue(ctx: ChannelHandlerContext) {
      val response: FullHttpResponse = DefaultFullHttpResponse(HTTP_1_1, CONTINUE)
      ctx.write(response)
    }
  }
}
