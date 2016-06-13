package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class PongMessageHandlerSpec extends MessageHandlerTestTrait with ShouldMatchers {
  this: Suite =>

  val testPath = new File("./target/unittests-PongMessageHandlerSpec/")

  "handle" should "" in {
  }
}