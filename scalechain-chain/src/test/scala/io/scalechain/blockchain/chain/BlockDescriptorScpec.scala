package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.BlockHash
import org.scalatest._

class BlockDescriptorSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with ShouldMatchers {
  "getHashCalculations" should "calculate the number of hash calculations based on a hash value" in {
    BlockDescriptor.getHashCalculations(BlockHash("F000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(BlockHash("E000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(BlockHash("9000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(BlockHash("8000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(BlockHash("7000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(BlockHash("5000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(BlockHash("4000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(BlockHash("3000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4
    BlockDescriptor.getHashCalculations(BlockHash("2000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4
    BlockDescriptor.getHashCalculations(BlockHash("1000000000000000000000000000000000000000000000000000000000000000")) shouldBe 8
    BlockDescriptor.getHashCalculations(BlockHash("0F00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(BlockHash("0E00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(BlockHash("0900000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(BlockHash("0800000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(BlockHash("0700000000000000000000000000000000000000000000000000000000000000")) shouldBe 32
  }
}