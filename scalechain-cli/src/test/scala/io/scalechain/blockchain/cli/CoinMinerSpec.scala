package io.scalechain.blockchain.cli

/**
  * Created by kangmo on 5/22/16.
  */

import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.HexUtil
import HexUtil._

import org.scalatest._

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class CoinMinerSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
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

  "isLessThan" should "return true only if the left hash is less than the right hash." in {
    import CoinMiner._
    isLessThan(Hash("00000"), Hash("00000")) shouldBe false
    isLessThan(Hash("00001"), Hash("00000")) shouldBe false
    isLessThan(Hash("00000"), Hash("00001")) shouldBe true
    isLessThan(Hash("0000F"), Hash("0000E")) shouldBe false
    isLessThan(Hash("0000E"), Hash("0000F")) shouldBe true

    isLessThan(Hash("10000"), Hash("00000")) shouldBe false
    isLessThan(Hash("00000"), Hash("10000")) shouldBe true
    isLessThan(Hash("F0000"), Hash("E0000")) shouldBe false
    isLessThan(Hash("E0000"), Hash("F0000")) shouldBe true


    isLessThan(Hash("FFFFF"), Hash("FFFFF")) shouldBe false
    isLessThan(Hash("FFFFF"), Hash("FFFFE")) shouldBe false
    isLessThan(Hash("FFFFE"), Hash("FFFFF")) shouldBe true
    isLessThan(Hash("FFFFF"), Hash("EFFFF")) shouldBe false
    isLessThan(Hash("EFFFF"), Hash("FFFFF")) shouldBe true
  }
}

