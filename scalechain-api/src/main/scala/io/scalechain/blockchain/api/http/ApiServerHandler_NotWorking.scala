package io.scalechain.blockchain.api.http

import java.nio.charset.StandardCharsets

import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelFutureListener, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders.Values
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, DefaultHttpDataFactory}
import io.netty.handler.codec.http._
import io.scalechain.blockchain.api.RequestHandler
import org.slf4j.LoggerFactory


class ApiServerHandler_NotWorking extends ChannelInboundHandlerAdapter {
  private lazy val logger = LoggerFactory.getLogger(classOf[ApiServerHandler_NotWorking])

  override def channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) {
    println(s"DEBUG!!!!  In channelRead. message : ${msg}")
    if (msg.isInstanceOf[FullHttpRequest]) {
      println("DEBUG!!!!  In FullHttpRequest")
      val req: FullHttpRequest = msg.asInstanceOf[FullHttpRequest]
      if (HttpHeaders.is100ContinueExpected(req)) {
        ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE))
      }
      val keepAlive: Boolean = HttpHeaders.isKeepAlive(req)

      // TODO : BUGBUG - Need to check if the request is post by using req.getMethod

      //val decoder : HttpPostRequestDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);

      val requestString = req.content().toString(StandardCharsets.UTF_8)
      println(s"DEBUG!!!!  requestString=${requestString}")

      val responseString = RequestHandler.handleRequest(requestString)
      println(s"DEBUG!!!!  responseString=${responseString}")

      val response: FullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(responseString, StandardCharsets.UTF_8))
      response.headers.set(CONTENT_TYPE, "application/json")
      response.headers.set(CONTENT_LENGTH, response.content.readableBytes)
      if (!keepAlive) {
        ctx.write(response).addListener(ChannelFutureListener.CLOSE)
      }
      else {
        response.headers.set(CONNECTION, Values.KEEP_ALIVE)
        ctx.write(response)
      }
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace
    ctx.close
  }
}
