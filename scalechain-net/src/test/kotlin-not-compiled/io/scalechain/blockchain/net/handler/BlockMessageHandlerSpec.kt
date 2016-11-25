package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class BlockMessageHandlerSpec : MessageHandlerTestTrait with Matchers {
  this: Suite =>

  val testPath = File("./target/unittests-BlockMessageHandlerSpec/")

  "handle" should "" in {
  }
}