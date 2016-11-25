package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class VerackMessageHandlerSpec : MessageHandlerTestTrait with Matchers {
  this: Suite =>

  val testPath = File("./target/unittests-VerackMessageHandlerSpec/")

  "handle" should "" in {
  }
}