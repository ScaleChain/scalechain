package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxInt
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxLong
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class Int32CodecSpec : MultiplePayloadTestSuite<Int>()  {

  override val codec = Codecs.Int32

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0,            bytes("00 00 00 00")),
      row( 1,            bytes("00 00 00 01")),
      row( 255,          bytes("00 00 00 FF")),
      row( 256,          bytes("00 00 01 00")),
      row( maxInt(31)-1, bytes("7F FF FF FE")),
      row( maxInt(31),   bytes("7F FF FF FF"))
    )
}

@RunWith(KTestJUnitRunner::class)
class Int32LCodecSpec : MultiplePayloadTestSuite<Int>()  {

  override val codec = Codecs.Int32L

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0,            bytes("00 00 00 00")),
      row( 1,            bytes("01 00 00 00")),
      row( 255,          bytes("FF 00 00 00")),
      row( 256,          bytes("00 01 00 00")),
      row( maxInt(31)-1, bytes("FE FF FF 7F")),
      row( maxInt(31),   bytes("FF FF FF 7F"))
    )
}


@RunWith(KTestJUnitRunner::class)
class UInt32CodecSpec : MultiplePayloadTestSuite<Long>()  {

  override val codec = Codecs.UInt32

  override val payloads =
    table(
      headers("message", "payload"),
      row( 0.toLong(),    bytes("00 00 00 00")),
      row( 1.toLong(),    bytes("00 00 00 01")),
      row( 255.toLong(),  bytes("00 00 00 FF")),
      row( 256.toLong(),  bytes("00 00 01 00")),
      row( maxLong(32)-1, bytes("FF FF FF FE")),
      row( maxLong(32),   bytes("FF FF FF FF"))
    )
}


@RunWith(KTestJUnitRunner::class)
class UInt32LCodecSpec : MultiplePayloadTestSuite<Long>()  {

  override val codec = Codecs.UInt32L

  override val payloads =
    table(
      headers("message", "payload"),
      row( 0.toLong(),    bytes("00 00 00 00")),
      row( 1.toLong(),    bytes("01 00 00 00")),
      row( 255.toLong(),  bytes("FF 00 00 00")),
      row( 256.toLong(),  bytes("00 01 00 00")),
      row( maxLong(32)-1, bytes("FE FF FF FF")),
      row( maxLong(32),   bytes("FF FF FF FF"))
    )
}
