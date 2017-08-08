package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.TransactionOutputCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
 * <Bitcoin Core Packets Captured>
 *
 *  f0ca052a01000000 ......................... Satoshis (49.99990000 BTC)
 *
 *  19 ....................................... Bytes in pubkey script: 25
 *  | 76 ..................................... OP_DUP
 *  | a9 ..................................... OP_HASH160
 *  | 14 ..................................... Push 20 bytes as data
 *  | | cbc20a7664f2f69e5355aa427045bc15
 *  | | e7c6c772 ............................. PubKey hash
 *  | 88 ..................................... OP_EQUALVERIFY
 *  | ac ..................................... OP_CHECKSIG
 */

@RunWith(KTestJUnitRunner::class)
class TransactionOutputSpec : PayloadTestSuite<TransactionOutput>()  {

  override val codec = TransactionOutputCodec

  override val payload = bytes(
    """
      f0ca052a01000000                     
      
      19                                   
        76                                 
        a9                                 
        14                                 
          cbc20a7664f2f69e5355aa427045bc15
          e7c6c772                         
        88                                 
        ac                                 
    """)

  override val message = TransactionOutput(value=4999990000L, lockingScript=LockingScript(Bytes.from("76a914cbc20a7664f2f69e5355aa427045bc15e7c6c77288ac")))

}
