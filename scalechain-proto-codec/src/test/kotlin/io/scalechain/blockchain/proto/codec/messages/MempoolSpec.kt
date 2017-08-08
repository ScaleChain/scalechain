package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith


/**
 *  <Bitcoin Core Packets Not Captured>
 *  No payload.
 */

@RunWith(KTestJUnitRunner::class)
class MempoolSpec : PayloadTestSuite<Mempool>()  {

  override val codec = MempoolCodec

  override val payload = bytes("") // No payload.

  override val message = Mempool()

}
