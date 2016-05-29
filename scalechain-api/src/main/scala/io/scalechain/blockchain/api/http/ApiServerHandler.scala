package io.scalechain.blockchain.api.http

import io.netty.buffer.{Unpooled, ByteBuf}
import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext, ChannelFutureListener}
import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil
import collection.convert.wrapAll._
import java.util

object ApiServerHandler {
  private def appendDecoderResult(buf: StringBuilder, o: HttpObject) {
    val result: DecoderResult = o.getDecoderResult
    if (result.isSuccess) {
      return
    }
    buf.append(".. WITH DECODER FAILURE: ")
    buf.append(result.cause)
    buf.append("\r\n")
  }

  private def send100Continue(ctx: ChannelHandlerContext) {
    val response: FullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE)
    ctx.write(response)
  }
}

class ApiServerHandler extends SimpleChannelInboundHandler[AnyRef] {
  private var request: HttpRequest = null
  /** Buffer that stores the response content */
  private final val buf: StringBuilder = new StringBuilder

  override def channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush
  }

  protected def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) {
    if (msg.isInstanceOf[HttpRequest]) {
      val request: HttpRequest = msg.asInstanceOf[HttpRequest]
      this.request = request
      if (HttpHeaders.is100ContinueExpected(request)) {
        ApiServerHandler.send100Continue(ctx)
      }
      buf.setLength(0)
      buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n")
      buf.append("===================================\r\n")
      buf.append("VERSION: ").append(request.getProtocolVersion).append("\r\n")
      buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n")
      buf.append("REQUEST_URI: ").append(request.getUri).append("\r\n\r\n")
      val headers: HttpHeaders = request.headers
      if (!headers.isEmpty) {
        import scala.collection.JavaConversions._
        for (h <- headers) {
          val key: String = h.getKey
          val value: String = h.getValue
          buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n")
        }
        buf.append("\r\n")
      }
      val queryStringDecoder: QueryStringDecoder = new QueryStringDecoder(request.getUri)
      val params: util.Map[String, util.List[String]] = queryStringDecoder.parameters
      if (!params.isEmpty) {
        import scala.collection.JavaConversions._
        for (p <- params.entrySet) {
          val key: String = p.getKey
          val vals: util.List[String] = p.getValue
          import scala.collection.JavaConversions._
          for (v <- vals) {
            buf.append("PARAM: ").append(key).append(" = ").append(v).append("\r\n")
          }
        }
        buf.append("\r\n")
      }
      ApiServerHandler.appendDecoderResult(buf, request)
    }
    if (msg.isInstanceOf[HttpContent]) {
      val httpContent: HttpContent = msg.asInstanceOf[HttpContent]
      val content: ByteBuf = httpContent.content
      if (content.isReadable) {
        buf.append("CONTENT: ")
        buf.append(content.toString(CharsetUtil.UTF_8))
        buf.append("\r\n")
        ApiServerHandler.appendDecoderResult(buf, request)
      }
      if (msg.isInstanceOf[LastHttpContent]) {
        buf.append("END OF CONTENT\r\n")
        val trailer: LastHttpContent = msg.asInstanceOf[LastHttpContent]
        if (!trailer.trailingHeaders.isEmpty) {
          buf.append("\r\n")
          import scala.collection.JavaConversions._
          for (name <- trailer.trailingHeaders.names) {
            import scala.collection.JavaConversions._
            for (value <- trailer.trailingHeaders.getAll(name)) {
              buf.append("TRAILING HEADER: ")
              buf.append(name).append(" = ").append(value).append("\r\n")
            }
          }
          buf.append("\r\n")
        }
        if (!writeResponse(trailer, ctx)) {
          ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
      }
    }
  }

  private def writeResponse(currentObj: HttpObject, ctx: ChannelHandlerContext): Boolean = {
    val keepAlive: Boolean = HttpHeaders.isKeepAlive(request)
    val response: FullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, if (currentObj.getDecoderResult.isSuccess) OK else BAD_REQUEST, Unpooled.copiedBuffer(buf.toString, CharsetUtil.UTF_8))
    response.headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8")
    if (keepAlive) {
      response.headers.set(CONTENT_LENGTH, response.content.readableBytes)
      response.headers.set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    }
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
    ctx.write(response)
    return keepAlive
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace
    ctx.close
  }
}
