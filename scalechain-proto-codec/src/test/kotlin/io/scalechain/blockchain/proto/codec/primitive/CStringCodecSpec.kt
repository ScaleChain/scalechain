package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(KTestJUnitRunner::class)
class CStringCodecSpec : MultiplePayloadTestSuite<String>()  {

  override val codec = Codecs.CString

  override val payloads =
    table(
      headers("message", "payload"),
      row( "", bytes("00")),
      row( "a", bytes("61  00")),
      row( "abc", bytes("61 62 63  00"))
    )
}
