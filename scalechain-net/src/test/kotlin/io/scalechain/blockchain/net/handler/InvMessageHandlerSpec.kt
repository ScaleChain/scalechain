package io.scalechain.blockchain.net.handler

import io.kotlintest.matchers.Matchers
import java.io.File

class InvMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-InvMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}