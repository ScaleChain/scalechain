package io.scalechain.blockchain.proto.codec

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BitcoinProtocolCodecSpec : FlatSpec(), Matchers {

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
    "encode" should "successfully encode a message" {
    }

    "decode" should "return an incomplete message with an empty message vector" {
    }

    "decode" should "return an incomplete message with a non-empty message vector" {
    }

    "decode" should "return no incomplete message with a non-empty message vector" {
    }
  }
}
