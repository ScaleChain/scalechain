package io.scalechain.blockchain.cli.control

import io.scalechain.blockchain.api.Services
import io.scalechain.blockchain.api.command.help.Help
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._
import spray.json.JsString

/**
  * Created by kangmo on 11/2/15.
  */
class HelpSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  "Help" should "list commands if no argument is provided." in {
    val response = invoke(Help)
    val result = response.right.get.get.asInstanceOf[StringResult]
    result.value.contains("== Blockchain ==") shouldBe true
  }

  "Help" should "show a help for a command if the command argument is provided." in {
    // For each command, try to run help.
    for (command <- Services.serviceByCommand.keys) {
      println(s"Testing if help for command, $command works well.")
      val response = invoke(Help, List(JsString(command)))
      val result = response.right.get.get.asInstanceOf[StringResult]
      result.value.contains(command) shouldBe true
    }
  }

}
