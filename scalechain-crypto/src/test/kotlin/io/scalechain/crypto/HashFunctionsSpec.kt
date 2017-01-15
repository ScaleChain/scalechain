package io.scalechain.crypto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.HexUtil.hex
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class HashFunctionsSpec : FlatSpec(), Matchers {

  init {
    "sha1" should "return a correct hash value" {
      // expected value calculated from the following site :
      // http://hash.online-convert.com/sha1-generator
      HashFunctions.sha1("hello world".toByteArray()) shouldBe SHA1( Bytes(bytes("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed")) )
    }

    "sha256" should "return a correct hash value" {
      // expected value calculated from the following site :
      // http://hash.online-convert.com/sha256-generator
      HashFunctions.sha256("hello world".toByteArray()) shouldBe SHA256( Bytes(bytes("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9")) )
    }

    "sha256 with offset/length" should "return a correct hash value" {
      // expected value calculated from the following site :
      // http://hash.online-convert.com/sha256-generator
      HashFunctions.sha256("123hello world456".toByteArray(), 3, 11) shouldBe SHA256( Bytes(bytes("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9")) )
    }


    "ripemd160" should "return a correct hash value" {
      // expected value calculated from the following site :
      //http://hash.online-convert.com/ripemd160-generator

      HashFunctions.ripemd160("hello world".toByteArray()) shouldBe RIPEMD160( Bytes(bytes("98c615784ccb5fe5936fbc0cbe9dfdb408d92f0f")) )
    }


    "hash160" should "return a correct hash value" {
//      println("${hex(HashFunctions.hash160("hello world".toByteArray()).value.array)}")
      HashFunctions.hash160("hello world".toByteArray()) shouldBe Hash160( Bytes(bytes("d7d5ee7824ff93f94c3055af9382c86c68b5ca92")) )
    }


    "hash256" should "return a correct hash value" {
//      println("${hex(HashFunctions.hash256("hello world".toByteArray()).value.array)}")
      HashFunctions.hash256("hello world".toByteArray()) shouldBe Hash256( Bytes(bytes("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423")) )
    }

    "hash256 with offset/length" should "return a correct hash value" {
      HashFunctions.hash256("123hello world456".toByteArray(), 3, 11) shouldBe Hash256( Bytes(bytes("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423")) )
    }
  }
}
