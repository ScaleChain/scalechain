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
      isLessThan(Hash(Bytes.from("0000")), Hash(Bytes.from("0000"))) shouldBe false
      isLessThan(Hash(Bytes.from("0001")), Hash(Bytes.from("0000"))) shouldBe false
      isLessThan(Hash(Bytes.from("0000")), Hash(Bytes.from("0001"))) shouldBe true
      isLessThan(Hash(Bytes.from("000F")), Hash(Bytes.from("000E"))) shouldBe false
      isLessThan(Hash(Bytes.from("000E")), Hash(Bytes.from("000F"))) shouldBe true

      isLessThan(Hash(Bytes.from("1000")), Hash(Bytes.from("0000"))) shouldBe false
      isLessThan(Hash(Bytes.from("0000")), Hash(Bytes.from("1000"))) shouldBe true
      isLessThan(Hash(Bytes.from("F000")), Hash(Bytes.from("E000"))) shouldBe false
      isLessThan(Hash(Bytes.from("E000")), Hash(Bytes.from("F000"))) shouldBe true


      isLessThan(Hash(Bytes.from("FFFF")), Hash(Bytes.from("FFFF"))) shouldBe false
      isLessThan(Hash(Bytes.from("FFFF")), Hash(Bytes.from("FFFE"))) shouldBe false
      isLessThan(Hash(Bytes.from("FFFE")), Hash(Bytes.from("FFFF"))) shouldBe true
      isLessThan(Hash(Bytes.from("FFFF")), Hash(Bytes.from("EFFF"))) shouldBe false
      isLessThan(Hash(Bytes.from("EFFF")), Hash(Bytes.from("FFFF"))) shouldBe true
    }

  }
}

