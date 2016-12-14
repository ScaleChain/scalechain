package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxInt
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxShort
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class Int16CodecSpec : MultiplePayloadTestSuite<Short>()  {

  override val codec = Int16Codec()

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0.toShort(),                bytes("00 00")),
      row( 1.toShort(),                bytes("00 01")),
      row( 255.toShort(),              bytes("00 FF")),
      row( 256.toShort(),              bytes("01 00")),
      row( (maxShort(15)-1).toShort(), bytes("7F FE")),
      row( maxShort(15),               bytes("7F FF"))
    )
}

@RunWith(KTestJUnitRunner::class)
class Int16LCodecSpec : MultiplePayloadTestSuite<Short>()  {

  override val codec = Int16LCodec()

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0.toShort(),                bytes("00 00")),
      row( 1.toShort(),                bytes("01 00")),
      row( 255.toShort(),              bytes("FF 00")),
      row( 256.toShort(),              bytes("00 01")),
      row( (maxShort(15)-1).toShort(), bytes("FE 7F")),
      row( maxShort(15),               bytes("FF 7F"))
    )
}


@RunWith(KTestJUnitRunner::class)
class UInt16CodecSpec : MultiplePayloadTestSuite<Int>()  {

  override val codec = UInt16Codec()

  override val payloads =
    table(
      headers("message", "payload"),
      row( 0,            bytes("00 00")),
      row( 1,            bytes("00 01")),
      row( maxInt(16)-1, bytes("FF FE")),
      row( maxInt(16),   bytes("FF FF"))
    )
}


@RunWith(KTestJUnitRunner::class)
class UInt16LCodecSpec : MultiplePayloadTestSuite<Int>()  {

  override val codec = UInt16LCodec()

  override val payloads =
    table(
      headers("message", "payload"),
      row( 0,            bytes("00 00")),
      row( 1,            bytes("01 00")),
      row( maxInt(16)-1, bytes("FE FF")),
      row( maxInt(16),   bytes("FF FF"))
    )
}
