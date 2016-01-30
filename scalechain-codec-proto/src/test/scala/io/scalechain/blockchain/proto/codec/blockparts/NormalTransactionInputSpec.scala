package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.NormalTransactionInput
import io.scalechain.blockchain.proto.codec.{NormalTransactionInputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
 * [Bitcoin Core Packets Captured]
 *
 *  7b1eabe0209b1fe794124575ef807057
 *  c77ada2138ae4fa8d6c4de0398a14f3f ......... Outpoint TXID
 *  00000000 ................................. Outpoint index number
 *
 *  49 ....................................... Bytes in sig. script: 73
 *  | 48 ..................................... Push 72 bytes as data
 *  | | 30450221008949f0cb400094ad2b5eb3
 *  | | 99d59d01c14d73d8fe6e96df1a7150de
 *  | | b388ab8935022079656090d7f6bac4c9
 *  | | a94e0aad311a4268e082a725f8aeae05
 *  | | 73fb12ff866a5f01 ..................... Secp256k1 signature
 *
 *  ffffffff ................................. Sequence number: UINT32_MAX
 */
class NormalTransactionInputSpec extends PayloadTestSuite[NormalTransactionInput]  {

  val codec = NormalTransactionInputCodec.codec

  val payload = bytes(
    """
      7b1eabe0209b1fe794124575ef807057
      c77ada2138ae4fa8d6c4de0398a14f3f    
      00000000                            
      
      49                                  
        48                                
          30450221008949f0cb400094ad2b5eb3
          99d59d01c14d73d8fe6e96df1a7150de
          b388ab8935022079656090d7f6bac4c9
          a94e0aad311a4268e082a725f8aeae05
          73fb12ff866a5f01                
      
      ffffffff                            
    """)

  val message = null// NormalTransactionInput()

}
