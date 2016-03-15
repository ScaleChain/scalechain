package io.scalechain.blockchain.cli

import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class GetBestBlockHashSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //
    startNode();

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
    stopNode();
  }

  "method" should "do something" in {

  }
}
