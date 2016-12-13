package io.scalechain.blockchain.net.handler

import io.kotlintest.matchers.Matchers
import java.io.File

class GetHeadersMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-GetHeadersMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}