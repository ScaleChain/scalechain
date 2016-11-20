package io.scalechain.blockchain.proto.codec.messageparts

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{TransactionOutputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

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
class TransactionOutputSpec : PayloadTestSuite<TransactionOutput>  {

  val codec = TransactionOutputCodec.codec

  val payload = bytes(
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

  val message = TransactionOutput(value=4999990000L, lockingScript=LockingScript(bytes("76a914cbc20a7664f2f69e5355aa427045bc15e7c6c77288ac")))

}
