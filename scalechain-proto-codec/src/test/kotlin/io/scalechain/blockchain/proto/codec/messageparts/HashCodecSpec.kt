package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.proto.codec.LockingScriptCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

/**
 * Created by kangmo on 14/12/2016.
 */
@RunWith(KTestJUnitRunner::class)
class HashCodecSpec : PayloadTestSuite<Hash>()  {

  override val codec = HashCodec

  override val payload = HexUtil.bytes(
    """
      3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b
    """)

  override val message = Hash(Bytes.from("7b1eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f3f"))

}