package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BooleanCodecSpec : MultiplePayloadTestSuite<Boolean>()  {

  override val codec = BooleanCodec()

  override val payloads =
    table(
      headers("message", "payload"),
      row( false, bytes("00") ),
      row( true,  bytes("01") )
    )
}
