package io.scalechain.blockchain.api.http

import io.netty.buffer.{Unpooled, ByteBuf}
import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext, ChannelFutureListener}
import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http._
import io.netty.util.{ReferenceCountUtil, CharsetUtil}
import io.scalechain.blockchain.api.RequestHandler
import io.scalechain.util.{StackUtil, ExceptionUtil}
import org.slf4j.LoggerFactory
import collection.convert.wrapAll._
import java.util

object ApiServerHandler {
  private def send100Continue(ctx: ChannelHandlerContext) {
    val response: FullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE)
    ctx.write(response)
  }
}

class ApiServerHandler extends SimpleChannelInboundHandler[AnyRef] {
  private val logger = LoggerFactory.getLogger(classOf[ApiServerHandler])


  private var request: HttpRequest = null
  /** Buffer that stores the response content */
  private final val requestData: StringBuilder = new StringBuilder

  override def channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush
  }

  protected def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) {
    try {
      if (msg.isInstanceOf[HttpRequest]) {
        val request: HttpRequest = msg.asInstanceOf[HttpRequest]
        this.request = request
        if (HttpHeaders.is100ContinueExpected(request)) {
          ApiServerHandler.send100Continue(ctx)
        }
        requestData.setLength(0)
      }
      if (msg.isInstanceOf[HttpContent]) {
        val httpContent: HttpContent = msg.asInstanceOf[HttpContent]
        val content: ByteBuf = httpContent.content
        if (content.isReadable) {
          requestData.append(content.toString(CharsetUtil.UTF_8))
        }
        if (msg.isInstanceOf[LastHttpContent]) {
          val trailer: LastHttpContent = msg.asInstanceOf[LastHttpContent]

          val responseString = RequestHandler.handleRequest(requestData.toString)

          if (!writeResponse(trailer, ctx, responseString)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
          }
        }
      }
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }

  private def writeResponse(currentObj: HttpObject, ctx: ChannelHandlerContext, responseString : String): Boolean = {
    val keepAlive: Boolean = HttpHeaders.isKeepAlive(request)
    val response: FullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, if (currentObj.getDecoderResult.isSuccess) OK else BAD_REQUEST, Unpooled.copiedBuffer(responseString, CharsetUtil.UTF_8))
    response.headers.set(CONTENT_TYPE, "application/json; charset=UTF-8")
    if (keepAlive) {
      response.headers.set(CONTENT_LENGTH, response.content.readableBytes)
      response.headers.set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    }
    /*
    val cookieString: String = request.headers.get(COOKIE)
    if (cookieString != null) {
      val cookies: util.Set[Cookie] = CookieDecoder.decode(cookieString)
      if (!cookies.isEmpty) {
        import scala.collection.JavaConversions._
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

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    val causeDescription = ExceptionUtil.describe( cause.getCause )
    logger.error(s"${cause}. Stack : ${StackUtil.getStackTrace(cause)} ${causeDescription}")
    ctx.close
  }
}
