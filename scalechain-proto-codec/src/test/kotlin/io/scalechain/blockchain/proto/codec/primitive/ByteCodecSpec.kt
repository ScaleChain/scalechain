package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ByteCodecSpec : MultiplePayloadTestSuite<Byte>()  {

  override val codec = Codecs.Byte

  override val payloads =
    table(
      headers("message", "payload"),
      row( 0.toByte(), bytes("00")),
      row( 1.toByte(), bytes("01")),
      row( 255.toByte(), bytes("FF"))
    )
}
