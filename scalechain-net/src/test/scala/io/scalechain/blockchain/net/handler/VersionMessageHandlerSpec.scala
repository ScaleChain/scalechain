package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class VersionMessageHandlerSpec extends MessageHandlerTestTrait with ShouldMatchers {
  this: Suite =>

  val testPath = new File("./target/unittests-VersionMessageHandlerSpec/")

  "handle" should "" in {
  }
}