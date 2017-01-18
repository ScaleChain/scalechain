package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FixedByteArrayCodecSpec : MultiplePayloadTestSuite<ByteArray>()  {

  override val codec = Codecs.fixedByteArray(1)

  override val payloads =
    table(
      headers("message", "payload"),
      row( byteArrayOf(0),            bytes("00")),
      row( byteArrayOf(1),            bytes("01")),
      row( byteArrayOf(255.toByte()), bytes("FF"))
    )
}


@RunWith(KTestJUnitRunner::class)
class FixedByteArrayCodecWithLength2Spec : MultiplePayloadTestSuite<ByteArray>()  {

  override val codec = Codecs.fixedByteArray(2)

  override val payloads =
    table(
      headers("message", "payload"),
      row( byteArrayOf(0, 0),                       bytes("00 00")),
      row( byteArrayOf(1, 2),                       bytes("01 02")),
      row( byteArrayOf(254.toByte(), 255.toByte()), bytes("FE FF"))
    )
}
