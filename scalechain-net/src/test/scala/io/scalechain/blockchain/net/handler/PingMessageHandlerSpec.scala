package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class PingMessageHandlerSpec extends MessageHandlerTestTrait with Matchers {
  this: Suite =>

  val testPath = new File("./target/unittests-PingMessageHandlerSpec/")

  "handle" should "" in {
  }
}