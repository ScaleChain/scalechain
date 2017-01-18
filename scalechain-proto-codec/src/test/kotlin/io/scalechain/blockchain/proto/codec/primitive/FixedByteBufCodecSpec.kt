package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.ByteBufExt
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FixedByteBufCodecSpec : MultiplePayloadTestSuite<ByteBuf>()  {
  override val codec = Codecs.fixedByteBuf(1)

  override val payloads =
    table(
      headers("message", "payload"),
      row( ByteBufExt.from("00"), bytes("00")),
      row( ByteBufExt.from("01"), bytes("01")),
      row( ByteBufExt.from("FF"), bytes("FF"))
    )
}

@RunWith(KTestJUnitRunner::class)
class FixedByteBufCodecLength2Spec : MultiplePayloadTestSuite<ByteBuf>()  {
  override val codec = Codecs.fixedByteBuf(2)

  override val payloads =
    table(
      headers("message", "payload"),
      row( ByteBufExt.from("00 00"), bytes("00 00")),
      row( ByteBufExt.from("01 02"), bytes("01 02")),
      row( ByteBufExt.from("FE FF"), bytes("FE FF"))
    )
}
