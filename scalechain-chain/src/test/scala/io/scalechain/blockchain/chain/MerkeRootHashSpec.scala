package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Hash
import io.scalechain.crypto.HashFunctions
import org.scalatest._

import scala.collection.mutable.ArrayBuffer

class MerkleRootHashSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with ShouldMatchers {
  "mergeHash" should "merge two hash values" in {
    val expectedHash = Hash( HashFunctions.hash256(TXHASH1.value.array ++ TXHASH2.value.array).value )
    MerkleRootHash.mergeHash( TXHASH1, TXHASH2 ) shouldBe expectedHash
  }

  "mergeHashes" should "hit an assertion if the number of hashes is zero" in {
    an[AssertionError] should be thrownBy {
      MerkleRootHash.mergeHashes(ArrayBuffer())
    }
  }

  "mergeHashes" should "hit an assertion if the number of hashes is even" in {
    an[AssertionError] should be thrownBy {
      MerkleRootHash.mergeHashes(ArrayBuffer(TXHASH1))
    }

    an[AssertionError] should be thrownBy {
      MerkleRootHash.mergeHashes(ArrayBuffer(TXHASH1, TXHASH1, TXHASH1))
    }
  }

  "mergeHashes" should "merge two hashes into one" in {
    val expectedHash = MerkleRootHash.mergeHash(TXHASH1, TXHASH2)
    MerkleRootHash.mergeHashes(ArrayBuffer(TXHASH1, TXHASH2)).toList shouldBe List(expectedHash)
  }

  "mergeHashes" should "merge four hashes into two" in {
    val expectedHash = MerkleRootHash.mergeHash(
      MerkleRootHash.mergeHash(TXHASH1, TXHASH2),
      MerkleRootHash.mergeHash(TXHASH3, TXHASH3)
    )
    MerkleRootHash.mergeHashes(ArrayBuffer(TXHASH1, TXHASH2, TXHASH3, TXHASH3)).toList shouldBe List(expectedHash)
  }


  "calculateMerkleRoot" should "hit an assertion with hash count zero" in {
    an[AssertionError] should be thrownBy {
      MerkleRootHash.calculateMerkleRoot(ArrayBuffer())
    }
  }

  "calculateMerkleRoot" should "produce the input itself if it is the only hash value" in {
    MerkleRootHash.calculateMerkleRoot(ArrayBuffer(TXHASH1)).toList shouldBe List(TXHASH1)
  }

  "calculateMerkleRoot" should "merge two hashes into one if the number of hashes is even" in {
    val expectedHash = MerkleRootHash.mergeHash(TXHASH1, TXHASH2)
    MerkleRootHash.calculateMerkleRoot(ArrayBuffer(TXHASH1, TXHASH2)).toList shouldBe List(expectedHash)
  }

  "calculateMerkleRoot" should "merge two hashes into one if the number of hashes is odd" in {
    val expectedHash = MerkleRootHash.mergeHash(
      MerkleRootHash.mergeHash(TXHASH1, TXHASH2),
      MerkleRootHash.mergeHash(TXHASH3, TXHASH3)
    )
    MerkleRootHash.calculateMerkleRoot(ArrayBuffer(TXHASH1, TXHASH2, TXHASH3)).toList shouldBe List(expectedHash)
  }

  "calculate" should "hit an assertion with transaction count zero" in {
    an[AssertionError] should be thrownBy {
      MerkleRootHash.calculate(List())
    }
  }

  "calculate" should "produce the merkle root hash with a transaction" in {
    MerkleRootHash.calculate(List(transaction1)).value.length shouldBe 32
  }

  "calculate" should "produce the merkle root hash with two transactions" in {
    MerkleRootHash.calculate(List(transaction1, transaction2)).value.length shouldBe 32
  }

  "calculate" should "produce the merkle root hash with three transactions" in {
    MerkleRootHash.calculate(List(transaction1, transaction2, transaction3)).value.length shouldBe 32
  }

}