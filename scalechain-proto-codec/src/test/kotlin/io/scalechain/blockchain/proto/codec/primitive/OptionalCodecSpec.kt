package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Row2
import io.kotlintest.properties.Table2
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.Option
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OptionalCodecSpec : MultiplePayloadTestSuite<Option<Int>>()  {

  override val codec = OptionalCodec(BooleanCodec(), Int32Codec())

  override val payloads =
    Table2<Option<Int>, ByteArray>(
      headers("message", "payload"),
      listOf<Row2<Option<Int>, ByteArray>>(
        row(Option.None<Int>(), bytes("00")),
        row(Option.Some(1), bytes("01  00 00 00 01"))
      )
    )
}
