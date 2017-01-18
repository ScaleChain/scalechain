package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class VariableListCodecSpec : MultiplePayloadTestSuite<List<Byte>>()  {

  override val codec = Codecs.variableListOf(VariableIntCodec(), ByteCodec())

  override val payloads =
    table(
      headers("message", "payload"),
      row( listOf<Byte>(),                             bytes("00")),
      row( listOf(5.toByte()),                         bytes("01  05")),
      row( listOf(5.toByte(), 6.toByte()),             bytes("02  05 06")),
      row( listOf(5.toByte(), 6.toByte(), 7.toByte()), bytes("03  05 06 07"))
    )
}
