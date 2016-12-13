package io.scalechain.blockchain.net.handler

import io.kotlintest.matchers.Matchers
import java.io.File

class BlockMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-BlockMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}