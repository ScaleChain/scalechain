package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Hash
import org.scalatest._

class BlockDescriptorSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with ShouldMatchers {
  "getHashCalculations" should "calculate the number of hash calculations based on a hash value" in {
    BlockDescriptor.getHashCalculations(Hash("F000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(Hash("E000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(Hash("9000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(Hash("8000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1
    BlockDescriptor.getHashCalculations(Hash("7000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(Hash("5000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(Hash("4000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2
    BlockDescriptor.getHashCalculations(Hash("3000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4
    BlockDescriptor.getHashCalculations(Hash("2000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4
    BlockDescriptor.getHashCalculations(Hash("1000000000000000000000000000000000000000000000000000000000000000")) shouldBe 8
    BlockDescriptor.getHashCalculations(Hash("0F00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(Hash("0E00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(Hash("0900000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(Hash("0800000000000000000000000000000000000000000000000000000000000000")) shouldBe 16
    BlockDescriptor.getHashCalculations(Hash("0700000000000000000000000000000000000000000000000000000000000000")) shouldBe 32
  }
}