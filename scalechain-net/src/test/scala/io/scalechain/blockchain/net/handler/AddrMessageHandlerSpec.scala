package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded
import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.{ChainTestDataTrait, BlockchainTestTrait}
import org.scalatest._

class AddrMessageHandlerSpec extends MessageHandlerTestTrait with ShouldMatchers {
  this: Suite =>

  val testPath = new File("./target/unittests-AddrMessageHandlerSpec/")

  "handle" should "" in {
  }
}