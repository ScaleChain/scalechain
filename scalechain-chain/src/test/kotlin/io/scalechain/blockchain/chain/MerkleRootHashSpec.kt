package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import io.scalechain.crypto.HashFunctions
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class MerkleRootHashSpec : FlatSpec(), TransactionTestDataTrait, Matchers {
  init {
    val D = TransactionTestDataTrait
    "mergeHash" should "merge two hash values" {
      val expectedHash = Hash(HashFunctions.hash256(D.TXHASH1.value + D.TXHASH2.value).value)
      MerkleRootCalculator.mergeHash(D.TXHASH1, D.TXHASH2) shouldBe expectedHash
    }

    "mergeHashes" should "hit an assertion if the number of hashes is zero" {
      shouldThrow<AssertionError> {
        MerkleRootCalculator.mergeHashes(listOf<Hash>())
      }
    }

    "mergeHashes" should "hit an assertion if the number of hashes is even" {
      shouldThrow<AssertionError> {
        MerkleRootCalculator.mergeHashes(listOf(D.TXHASH1))
      }

      shouldThrow<AssertionError> {
        MerkleRootCalculator.mergeHashes(listOf(D.TXHASH1, D.TXHASH1, D.TXHASH1))
      }
    }

    "mergeHashes" should "merge two hashes into one" {
      val expectedHash = MerkleRootCalculator.mergeHash(D.TXHASH1, D.TXHASH2)
      MerkleRootCalculator.mergeHashes(listOf(D.TXHASH1, D.TXHASH2)) shouldBe listOf(expectedHash)
    }

    "mergeHashes" should "merge four hashes into two" {
      val expectedHash = MerkleRootCalculator.mergeHash(
        MerkleRootCalculator.mergeHash(D.TXHASH1, D.TXHASH2),
        MerkleRootCalculator.mergeHash(D.TXHASH3, D.TXHASH3)
      )
      MerkleRootCalculator.mergeHashes(listOf(D.TXHASH1, D.TXHASH2, D.TXHASH3, D.TXHASH3)) shouldBe listOf(expectedHash)
    }


    "calculateMerkleRoot" should "hit an assertion with hash count zero" {
      shouldThrow<AssertionError> {
        MerkleRootCalculator.calculateMerkleRoot(mutableListOf<Hash>())
      }
    }

    "calculateMerkleRoot" should "produce the input itself if it is the only hash value" {
      MerkleRootCalculator.calculateMerkleRoot(mutableListOf(D.TXHASH1)) shouldBe listOf(D.TXHASH1)
    }

    "calculateMerkleRoot" should "merge two hashes into one if the number of hashes is even" {
      val expectedHash = MerkleRootCalculator.mergeHash(D.TXHASH1, D.TXHASH2)
      MerkleRootCalculator.calculateMerkleRoot(mutableListOf(D.TXHASH1, D.TXHASH2)) shouldBe listOf(expectedHash)
    }

    "calculateMerkleRoot" should "merge two hashes into one if the number of hashes is odd" {
      val expectedHash = MerkleRootCalculator.mergeHash(
        MerkleRootCalculator.mergeHash(D.TXHASH1, D.TXHASH2),
        MerkleRootCalculator.mergeHash(D.TXHASH3, D.TXHASH3)
      )
      MerkleRootCalculator.calculateMerkleRoot(mutableListOf(D.TXHASH1, D.TXHASH2, D.TXHASH3)) shouldBe listOf(expectedHash)
    }

    "calculate" should "hit an assertion with transaction count zero" {
      shouldThrow<AssertionError> {
        MerkleRootCalculator.calculate(listOf())
      }
    }

    "calculate" should "produce the merkle root hash with a transaction" {
      MerkleRootCalculator.calculate(listOf(transaction1())).value.size shouldBe 32
    }

    "calculate" should "produce the merkle root hash with two transactions" {
      MerkleRootCalculator.calculate(listOf(transaction1(), transaction2())).value.size shouldBe 32
    }

    "calculate" should "produce the merkle root hash with three transactions" {
      MerkleRootCalculator.calculate(listOf(transaction1(), transaction2(), transaction3())).value.size shouldBe 32
    }
  }
}