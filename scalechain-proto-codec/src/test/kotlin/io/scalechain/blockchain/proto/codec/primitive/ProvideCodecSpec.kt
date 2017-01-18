package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ProvideCodecSpec : MultiplePayloadTestSuite<OneByte>()  {

  override val codec = Codecs.provide(OneByte(1))

  override val payloads =
    table(
      headers("message", "payload"),
      row( OneByte(1), bytes(""))
    )
}
