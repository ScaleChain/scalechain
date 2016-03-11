package io.scalechain.blockchain.storage.db

import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class ProtocolMessageSerializerSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  val serializer = new ProtocolMessageSerializer

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

  "encode" should "successfully encode a message" in {
    //serializer
  }

  "decode" should "successfully decode a message" in {
    //serializer
  }
}
