package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class PublicKeySpec : FlatSpec(), Matchers, ChainTestTrait {

  init {
    "PublicKey.from(encoded)" should "decode a uncompressed public key" {
      val privateKey = PrivateKey.generate()
      val publicKey = PublicKey.from(privateKey)

      val parsedPublicKey = PublicKey.from( publicKey.encode() )

      parsedPublicKey shouldBe publicKey
    }
/*
  "PublicKey.from(encoded)" should "decode an comcompressed public key" {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey, compressed = true)

    val parsedPublicKey = PublicKey.from( publicKey.encode(compressed = true) )

    parsedPublicKey shouldBe publicKey
  }
*/

/*
  "PublicKey.from(privateKey)" should "create a compressed public key" {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey, compressed = true)

    publicKey.encode(compressed = true).length shouldBe 33
    publicKey.encode(compressed = false).length shouldBe 65
  }
*/

    "PublicKey.from(privateKey)" should "create an uncompressed public key" {
      val privateKey = PrivateKey.generate()
      val publicKey = PublicKey.from(privateKey)

      publicKey.encode().size shouldBe 65
    }
/*
    "getHash" should "" {
      // TODO : Implement
    }
*/
  }

}