package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Row2
import io.kotlintest.properties.Table2
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.blockchain.proto.codec.primitive.VarIntValues.ENCODED_VALUE_MAP
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class VariableIntCodecSpec : MultiplePayloadTestSuite<Long>()  {

  override val codec = Codecs.VariableInt

  override val payloads =
    Table2<Long, ByteArray>(
      headers("message", "payload"),
      ENCODED_VALUE_MAP.entries.map { pair ->
        val varIntValue = pair.key
        val encodedVarInt = pair.value
        Row2(varIntValue, encodedVarInt)
      }
    )
}
