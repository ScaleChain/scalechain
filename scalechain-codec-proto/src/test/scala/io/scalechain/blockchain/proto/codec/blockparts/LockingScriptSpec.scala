package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.codec.{LockingScriptCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class LockingScriptSpec extends PayloadTestSuite[LockingScript]  {

  val codec = LockingScriptCodec.codec

  val payload = bytes("")

  val message = null//LockingScript()

}
