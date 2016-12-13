package io.scalechain.blockchain.net.handler

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import org.junit.runner.RunWith
import java.io.File

@RunWith(KTestJUnitRunner::class)
class VerackMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-VerackMessageHandlerSpec/")

  init {
    "handle" should "" {
    }
  }
}