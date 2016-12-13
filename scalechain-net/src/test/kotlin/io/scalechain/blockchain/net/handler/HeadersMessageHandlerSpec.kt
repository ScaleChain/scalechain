package io.scalechain.blockchain.net.handler

import io.kotlintest.matchers.Matchers
import java.io.File

class HeadersMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-HeadersMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}