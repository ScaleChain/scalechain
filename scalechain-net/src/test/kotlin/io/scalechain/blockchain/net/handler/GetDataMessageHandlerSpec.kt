package io.scalechain.blockchain.net.handler

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import org.junit.runner.RunWith
import java.io.File

@RunWith(KTestJUnitRunner::class)
class GetDataMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./build/unittests-GetDataMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}