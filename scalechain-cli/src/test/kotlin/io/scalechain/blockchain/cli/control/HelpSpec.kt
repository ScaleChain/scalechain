package io.scalechain.blockchain.cli.control

import com.google.gson.JsonPrimitive
import io.scalechain.blockchain.api.Services
import io.scalechain.blockchain.api.command.help.Help
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite

/**
  * Created by kangmo on 11/2/15.
  */
class HelpSpec : APITestSuite() {

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  init {

    "Help" should "list commands if no argument is provided." {
      val response = invoke(Help)
      val result = response.right()!! as StringResult
      result.value.contains("== Blockchain ==") shouldBe true
    }

    "Help" should "show a help for a command if the command argument is provided." {
      // For each command, try to run help.
      for (command in Services.serviceByCommand.keys) {
        println("Testing if help for command, $command works well.")
        val response = invoke(Help, listOf(JsonPrimitive(command)))
        val result = response.right()!! as StringResult
        result.value.contains(command) shouldBe true
      }
    }
  }
}
