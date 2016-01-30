package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.UnlockingScript
import io.scalechain.blockchain.proto.codec.{UnlockingScriptCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class UnlockingScriptSpec extends PayloadTestSuite[UnlockingScript]  {

  val codec = UnlockingScriptCodec.codec

  val payload = bytes("")

  val message = null//UnlockingScript()

}
