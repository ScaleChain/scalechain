package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class VariableStringCodecSpec : MultiplePayloadTestSuite<String>()  {

  override val codec = VariableStringCodec(VariableIntCodec())

  override val payloads =
    table(
      headers("message", "payload"),
      row( "",   bytes("00")),
      row( "a",  bytes("01  61")),
      row( "ab", bytes("02  61 62"))
    )
}
