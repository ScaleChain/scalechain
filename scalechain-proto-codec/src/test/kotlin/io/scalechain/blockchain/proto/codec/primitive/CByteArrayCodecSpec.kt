package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class CByteArrayCodecSpec : MultiplePayloadTestSuite<ByteArray>()  {

  override val codec = Codecs.CByteArray

  override val payloads =
    table(
      headers("message", "payload"),
      row( byteArrayOf(), bytes("00")),
      row( byteArrayOf(1), bytes("01  00")),
      row( byteArrayOf(1,255.toByte()), bytes("01 FF  00"))
    )
}
