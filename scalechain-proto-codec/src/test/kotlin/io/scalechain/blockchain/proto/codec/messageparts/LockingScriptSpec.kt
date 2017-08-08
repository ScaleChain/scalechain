package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.codec.LockingScriptCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
 * <Bitcoin Core Packets Not Captured>
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
class LockingScriptSpec : PayloadTestSuite<LockingScript>()  {

  override val codec = LockingScriptCodec

  override val payload = bytes(
    """
      19                                  
        76                                
        a9                                
        14                                
          cbc20a7664f2f69e5355aa427045bc15
          e7c6c772                        
        88                                
        ac                                
    """)

  override val message = LockingScript(Bytes.from("76a914cbc20a7664f2f69e5355aa427045bc15e7c6c77288ac"))

}
