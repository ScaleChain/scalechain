package io.scalechain.blockchain.transaction

import org.scalatest._

class PublicKeySpec extends FlatSpec with BeforeAndAfterEach with ChainTestTrait with ShouldMatchers {
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

  "PublicKey.from(encoded)" should "decode a uncompressed public key" in {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey)

    val parsedPublicKey = PublicKey.from( publicKey.encode() )

    parsedPublicKey shouldBe publicKey
  }
/*
  "PublicKey.from(encoded)" should "decode an comcompressed public key" in {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey, compressed = true)

    val parsedPublicKey = PublicKey.from( publicKey.encode(compressed = true) )

    parsedPublicKey shouldBe publicKey
  }
*/

/*
  "PublicKey.from(privateKey)" should "create a compressed public key" in {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey, compressed = true)

    publicKey.encode(compressed = true).length shouldBe 33
    publicKey.encode(compressed = false).length shouldBe 65
  }
*/

  "PublicKey.from(privateKey)" should "create an uncompressed public key" in {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey)

    publicKey.encode().length shouldBe 65
  }

  "getHash" should "" ignore {
    // TODO : Implement
  }

}