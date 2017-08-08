package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.NormalTransactionInputCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
 * <Bitcoin Core Packets Captured>
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

@RunWith(KTestJUnitRunner::class)
class NormalTransactionInputSpec : PayloadTestSuite<NormalTransactionInput>()  {

  override val codec = NormalTransactionInputCodec

  override val payload = bytes(
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

  override val message = NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")), outputIndex=0L, unlockingScript=UnlockingScript(Bytes.from("4830450221008949f0cb400094ad2b5eb399d59d01c14d73d8fe6e96df1a7150deb388ab8935022079656090d7f6bac4c9a94e0aad311a4268e082a725f8aeae0573fb12ff866a5f01")), sequenceNumber=4294967295L)

}
