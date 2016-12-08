package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.OrphanBlockDescriptorCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OrphanBlockDescriptorCodecSpec : PayloadTestSuite<OrphanBlockDescriptor>()  {

  override val codec = OrphanBlockDescriptorCodec

  override val payload = bytes(
    """040000007b1eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f3f701eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f4fd2029649010000000200000000
    """)

  override val message = OrphanBlockDescriptor(
    Block(
      header = BlockHeader(
        version = 4,
        hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
        hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
        timestamp = 1234567890L,
        target = 1,
        nonce = 2
      ),
      transactions = listOf()
    )
  )
}
