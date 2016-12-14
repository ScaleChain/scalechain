package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxBigInteger
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxInt
import io.scalechain.blockchain.proto.codec.primitive.PrimitiveCodecTestUtil.maxLong
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith
import java.math.BigInteger

@RunWith(KTestJUnitRunner::class)
class Int64CodecSpec : MultiplePayloadTestSuite<Long>()  {

  override val codec = Int64Codec()

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0.toLong(),    bytes("00 00 00 00 00 00 00 00")),
      row( 1.toLong(),    bytes("00 00 00 00 00 00 00 01")),
      row( 255.toLong(),  bytes("00 00 00 00 00 00 00 FF")),
      row( 256.toLong(),  bytes("00 00 00 00 00 00 01 00")),
      row( maxLong(63)-1, bytes("7F FF FF FF FF FF FF FE")),
      row( maxLong(63),   bytes("7F FF FF FF FF FF FF FF"))
    )
}

@RunWith(KTestJUnitRunner::class)
class Int64LCodecSpec : MultiplePayloadTestSuite<Long>()  {

  override val codec = Int64LCodec()

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( 0.toLong(),    bytes("00 00 00 00 00 00 00 00")),
      row( 1.toLong(),    bytes("01 00 00 00 00 00 00 00")),
      row( 255.toLong(),  bytes("FF 00 00 00 00 00 00 00")),
      row( 256.toLong(),  bytes("00 01 00 00 00 00 00 00")),
      row( maxLong(63)-1, bytes("FE FF FF FF FF FF FF 7F")),
      row( maxLong(63),   bytes("FF FF FF FF FF FF FF 7F"))
    )
}

//
// No codec exists for UInt64
//


@RunWith(KTestJUnitRunner::class)
class UInt64LCodecSpec : MultiplePayloadTestSuite<BigInteger>()  {

  override val codec = UInt64LCodec()

  override val payloads =
    table(
      headers("message", "payload"),
      // Add case for -1
      row( BigInteger("0"),                   bytes("00 00 00 00 00 00 00 00")),
      row( BigInteger("1"),                   bytes("01 00 00 00 00 00 00 00")),
      row( BigInteger("255"),                 bytes("FF 00 00 00 00 00 00 00")),
      row( BigInteger("256"),                 bytes("00 01 00 00 00 00 00 00")),
      row( maxBigInteger(64)-BigInteger("1"), bytes("FE FF FF FF FF FF FF FF")),
      row( maxBigInteger(64),                 bytes("FF FF FF FF FF FF FF FF"))
    )
}
