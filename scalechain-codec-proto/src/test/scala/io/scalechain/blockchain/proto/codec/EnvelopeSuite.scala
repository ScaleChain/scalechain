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

    encode(message) shouldBe payloadBits
  }

  "payload" should "be correctly decoded" in {
    decodeFully(payloadBits) shouldBe message
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
    //println("actual = " + actual)
    //println("expected = " + envelope)

    envelopeCodec.decode(envelopeBits).require.value shouldBe envelope
  }
}
