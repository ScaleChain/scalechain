package io.scalechain.blockchain.cli

/**
  * Created by kangmo on 5/22/16.
  */

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.HexUtil.bytes

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class CoinMinerSpec : FlatSpec(), Matchers {

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

  fun isLessThan(left:Hash, right:Hash) : Boolean {
    return left.compareTo(right) < 0
  }

  init {

    "isLessThan" should "return true only if the left hash is less than the right hash." {
      isLessThan(Hash(bytes("00000")), Hash(bytes("00000"))) shouldBe false
      isLessThan(Hash(bytes("00001")), Hash(bytes("00000"))) shouldBe false
      isLessThan(Hash(bytes("00000")), Hash(bytes("00001"))) shouldBe true
      isLessThan(Hash(bytes("0000F")), Hash(bytes("0000E"))) shouldBe false
      isLessThan(Hash(bytes("0000E")), Hash(bytes("0000F"))) shouldBe true

      isLessThan(Hash(bytes("10000")), Hash(bytes("00000"))) shouldBe false
      isLessThan(Hash(bytes("00000")), Hash(bytes("10000"))) shouldBe true
      isLessThan(Hash(bytes("F0000")), Hash(bytes("E0000"))) shouldBe false
      isLessThan(Hash(bytes("E0000")), Hash(bytes("F0000"))) shouldBe true


      isLessThan(Hash(bytes("FFFFF")), Hash(bytes("FFFFF"))) shouldBe false
      isLessThan(Hash(bytes("FFFFF")), Hash(bytes("FFFFE"))) shouldBe false
      isLessThan(Hash(bytes("FFFFE")), Hash(bytes("FFFFF"))) shouldBe true
      isLessThan(Hash(bytes("FFFFF")), Hash(bytes("EFFFF"))) shouldBe false
      isLessThan(Hash(bytes("EFFFF")), Hash(bytes("FFFFF"))) shouldBe true
    }

  }
}

