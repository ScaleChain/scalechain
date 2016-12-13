package io.scalechain.blockchain.net.handler

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import org.junit.runner.RunWith
import java.io.File

@RunWith(KTestJUnitRunner::class)
class GetHeadersMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-GetHeadersMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}