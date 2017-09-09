package io.scalechain.blockchain.proto.codec
import io.scalechain.blockchain.proto.*
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.HexUtil.hex
import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.properties.Table2
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.scalechain.util.toByteArray

/**
  * Created by kangmo on 1/29/16.
  */
abstract class PayloadTestSuite<T> : FlatSpec(), Matchers, CodecTestUtil {
  abstract val codec : Codec<T>
  abstract val message : T?
  abstract val payload : ByteArray

  init {
    "payload" should "be correctly encoded" {
      if (message != null) {
        val encodedBytes = encode<T>(codec, message!!)

        println("encodedBits=${HexUtil.hex(encodedBytes)}")
        encodedBytes.toList() shouldBe payload.toList()
      }
    }

    "payload" should "be correctly decoded" {
      val actual = decodeFully(codec, payload)

      if (message != null) {
        actual shouldBe message
      }
    }

    "payload" should "be correctly decoded and encoded" {
      val obj = decodeFully(codec, payload)
      encode(codec, obj).toList() shouldBe payload.toList()
    }
  }
}


abstract class MultiplePayloadTestSuite<T> : FlatSpec(), Matchers, CodecTestUtil {
  abstract val codec : Codec<T>
  abstract val payloads : Table2<T, ByteArray>

  init {
    "payloads" should "be correctly decoded" {
      forAll(payloads) { message: T, payload: ByteArray ->
        val actual = decodeFully(codec, payload)

        //println("message : ${(message as ByteBuf).toByteArray().toList()}, actual : ${(actual as ByteBuf).toByteArray().toList()},  payload : ${payload.toList()}")

        if (message != null) {
          if (message is ByteArray) {
            // Compare the contents of two ByteArrays.
            // Note that ByteArray.equals check reference equality.
            val decoded = actual as ByteArray
            //println("check : ${decoded.toList()}, ${message.toList()}")
            decoded.toList() shouldBe message.toList()
          } else {
            actual shouldBe message
          }
        }
      }
    }

    "payloads" should "be correctly encoded" {
      forAll(payloads) { message : T, payload : ByteArray ->
        //println("message : ${(message as ByteBuf).toByteArray().toList()}, payload : ${payload.toList()}")

        if (message != null) {
          val encodedBytes = encode<T>(codec, message)

          println("encodedBits=${HexUtil.hex(encodedBytes)}")
          encodedBytes.toList() shouldBe payload.toList()
        }
      }
    }

    "payloads" should "be correctly decoded and encoded" {
      forAll(payloads) { _ : T, payload: ByteArray ->
        val obj = decodeFully(codec, payload)
        encode(codec, obj).toList() shouldBe payload.toList()
      }
    }
  }
}


/**
  * Created by kangmo on 1/29/16.
  */
abstract class EnvelopeTestSuite<T> : PayloadTestSuite<T>() {
  val envelopeCodec = BitcoinMessageEnvelopeCodec /* BUGBUG Need to set the code instance */

  abstract val envelope : BitcoinMessageEnvelope
  abstract val envelopeHeader : ByteArray

  fun envelopeBytes() = envelopeHeader + payload

  init {
    "envelope" should "be correctly encoded" {
//    println(s"encoded111 : ${HexUtil.hex(envelopeCodec.encode(envelope).require.toByteArray)}")
      envelopeCodec.encode(envelope).toList() shouldBe envelopeBytes().toList()
    }

    "envelope" should "be correctly decoded" {
      val actual = envelopeCodec.decode(envelopeBytes())
      actual shouldBe envelope
    }
  }
}
