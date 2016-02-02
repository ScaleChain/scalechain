package io.scalechain.blockchain.proto.codec
import io.scalechain.blockchain.proto.ProtocolMessage

import org.scalatest.{FlatSpec, ShouldMatchers}
import scodec.Codec
import scodec.bits.BitVector


/**
  * Created by kangmo on 1/29/16.
  */
trait PayloadTestSuite[T] extends FlatSpec with ShouldMatchers with CodecTestUtil {
  implicit val codec : Codec[T]

  val message : T
  val payload : Array[Byte]

  def payloadBits() = BitVector.view(payload)

  "payload" should "be correctly encoded" in {
    if (message != null) {
      encode(message) shouldBe payloadBits
    }
  }

  "payload" should "be correctly decoded" in {
    val actual = decodeFully(payloadBits)
    println("actual payload = " + actual)
    if (message != null) {
      decodeFully(payloadBits) shouldBe message
    }
  }

  "payload" should "be correctly decoded and encoded" in {
    val obj = decodeFully(payloadBits)
    encode(obj) shouldBe payloadBits
  }
}


/**
  * Created by kangmo on 1/29/16.
  */
trait EnvelopeTestSuite[T] extends PayloadTestSuite[T] {
  val envelopeCodec = BitcoinMessageEnvelope.codec

  val envelope : BitcoinMessageEnvelope
  val envelopeHeader : Array[Byte]

  def envelopeBits() = BitVector.view(envelopeHeader) ++ payloadBits

  "envelope" should "be correctly encoded" in {
    envelopeCodec.encode(envelope).require shouldBe envelopeBits
  }

  "envelope" should "be correctly decoded" in {
    val actual = envelopeCodec.decode(envelopeBits).require.value
    envelopeCodec.decode(envelopeBits).require.value shouldBe envelope
  }
}
