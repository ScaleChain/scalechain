package io.scalechain.blockchain.proto.codec.privateparts

import io.scalechain.blockchain.proto.codec.messages.TransactionSpec
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockConsensusCodec, TransactionPoolEntryCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._


class BlockConsensusCodecSpec extends PayloadTestSuite[BlockConsensus]  {

  val codec = BlockConsensusCodec.codec


  val payload = bytes(
    """040000006354173fcc4b38c2d45948b19d2a527c4f10ada6834700060000000000000000661edc92f86278de6b38ab55b560cca3f6389c17c444e4611087be1251bdc8db5a2cab56f0280918f009a5d00100000000000000
    """)

  val message = BlockConsensus(
    header = BlockHeader(
                version=4,
                hashPrevBlock=Hash(bytes("000000000000000006004783a6ad104f7c522a9db14859d4c2384bcc3f175463")),
                hashMerkleRoot=Hash(bytes("dbc8bd5112be871061e444c4179c38f6a3cc60b555ab386bde7862f892dc1e66")),
                timestamp=1454058586L,
                target=403253488L,
                nonce=3500476912L),
    height = 1
  )
}