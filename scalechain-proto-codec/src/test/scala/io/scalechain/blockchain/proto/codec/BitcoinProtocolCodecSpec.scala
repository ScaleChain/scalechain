package io.scalechain.blockchain.proto.codec

/**
  * Created by kangmo on 2/7/16.
  */
class BitcoinProtocolCodecSpec {

}


package io.scalechain.blockchain.cli

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ProtocolCodecException
import io.scalechain.blockchain.cli.BlockDirectoryReader
import io.scalechain.blockchain.cli.BlockReadListener
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto.Block
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BitcoinProtocolCodecSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
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

  "encode" should "successfully encode a message" in {
  }

  "decode" should "return an incomplete message with an empty message vector" in {
  }

  "decode" should "return an incomplete message with a non-empty message vector" in {
  }

  "decode" should "return no incomplete message with a non-empty message vector" in {
  }
}
