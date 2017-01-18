package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.ByteBufExt
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class VariableByteBufCodecSpec : MultiplePayloadTestSuite<ByteBuf>()  {

  override val codec = Codecs.variableByteBuf(VariableIntCodec())

  override val payloads =
    table(
      headers("message", "payload"),
      row( ByteBufExt.from(""),         HexUtil.bytes("00")),
      row( ByteBufExt.from("0A"),       HexUtil.bytes("01  0A")),
      row( ByteBufExt.from("0A 0B"),    HexUtil.bytes("02  0A 0B")),
      row( ByteBufExt.from("0A 0B 0C"), HexUtil.bytes("03  0A 0B 0C"))
    )
}
