package io.scalechain.blockchain.cli

/**
  * Created by kangmo on 5/22/16.
  */

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
@RunWith(KTestJUnitRunner::class)
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
      isLessThan(Hash(Bytes.from("00000")), Hash(Bytes.from("00000"))) shouldBe false
      isLessThan(Hash(Bytes.from("00001")), Hash(Bytes.from("00000"))) shouldBe false
      isLessThan(Hash(Bytes.from("00000")), Hash(Bytes.from("00001"))) shouldBe true
      isLessThan(Hash(Bytes.from("0000F")), Hash(Bytes.from("0000E"))) shouldBe false
      isLessThan(Hash(Bytes.from("0000E")), Hash(Bytes.from("0000F"))) shouldBe true

      isLessThan(Hash(Bytes.from("10000")), Hash(Bytes.from("00000"))) shouldBe false
      isLessThan(Hash(Bytes.from("00000")), Hash(Bytes.from("10000"))) shouldBe true
      isLessThan(Hash(Bytes.from("F0000")), Hash(Bytes.from("E0000"))) shouldBe false
      isLessThan(Hash(Bytes.from("E0000")), Hash(Bytes.from("F0000"))) shouldBe true


      isLessThan(Hash(Bytes.from("FFFFF")), Hash(Bytes.from("FFFFF"))) shouldBe false
      isLessThan(Hash(Bytes.from("FFFFF")), Hash(Bytes.from("FFFFE"))) shouldBe false
      isLessThan(Hash(Bytes.from("FFFFE")), Hash(Bytes.from("FFFFF"))) shouldBe true
      isLessThan(Hash(Bytes.from("FFFFF")), Hash(Bytes.from("EFFFF"))) shouldBe false
      isLessThan(Hash(Bytes.from("EFFFF")), Hash(Bytes.from("FFFFF"))) shouldBe true
    }

  }
}

