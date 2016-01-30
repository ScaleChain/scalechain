package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.CoinbaseData
import io.scalechain.blockchain.proto.codec.{CoinbaseDataCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
 * [Bitcoin Core Packets Not Captured]
 *
 * 4d                                               .... coinbase data size. (4d => 77 bytes)
 *                                                       Length of the coinbase data, from 2 to 100 bytes
 * 04 ff ff 00 1d 01 04 45  54 68 65 20 54 69 6d 65
 * 73 20 30 33 2f 4a 61 6e  2f 32 30 30 39 20 43 68
 * 61 6e 63 65 6c 6c 6f 72  20 6f 6e 20 62 72 69 6e
 * 6b 20 6f 66 20 73 65 63  6f 6e 64 20 62 61 69 6c
 * 6f 75 74 20 66 6f 72 20  62 61 6e 6b 73          .... coinbase data
 *                                                  .... ^D��<GS>^A^DEThe Times 03/Jan/2009 Chancellor on brink of
 *                                                  .... second bailout for banks
 */
class CoinbaseDataSpec extends PayloadTestSuite[CoinbaseData]  {

  val codec = CoinbaseDataCodec.codec

  val payload = bytes(
    """
      4d

      04 ff ff 00 1d 01 04 45  54 68 65 20 54 69 6d 65
      73 20 30 33 2f 4a 61 6e  2f 32 30 30 39 20 43 68
      61 6e 63 65 6c 6c 6f 72  20 6f 6e 20 62 72 69 6e
      6b 20 6f 66 20 73 65 63  6f 6e 64 20 62 61 69 6c
      6f 75 74 20 66 6f 72 20  62 61 6e 6b 73
    """.stripMargin)

  val message = null//CoinbaseData()

}
