package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class CStringPrefixedCodecSpec : MultiplePayloadTestSuite<CStringPrefixed<Byte>>()  {

  override val codec = Codecs.cstringPrefixed( ByteCodec() )

  override val payloads =
    table(
      headers("message", "payload"),
      row( CStringPrefixed("a", 0.toByte()),  bytes("61 00  00")),
      row( CStringPrefixed("a", 1.toByte()),  bytes("61 00  01")),
      row( CStringPrefixed("ab", 1.toByte()), bytes("61 62 00  01"))
    )
}
