package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.ByteBufExt
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class VariableByteArrayCodecSpec : MultiplePayloadTestSuite<ByteArray>()  {

    override val codec = Codecs.variableByteArray(VariableIntCodec())

    override val payloads =
        table(
            headers("message", "payload"),
            row( HexUtil.bytes(""),         HexUtil.bytes("00")),
            row( HexUtil.bytes("0A"),       HexUtil.bytes("01  0A")),
            row( HexUtil.bytes("0A 0B"),    HexUtil.bytes("02  0A 0B")),
            row( HexUtil.bytes("0A 0B 0C"), HexUtil.bytes("03  0A 0B 0C"))
        )
}