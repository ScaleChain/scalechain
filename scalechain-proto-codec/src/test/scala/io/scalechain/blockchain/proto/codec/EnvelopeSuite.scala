package io.scalechain.blockchain.proto.codec
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

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
/*
  "test" should "pass" in {
    BlockInfo(100,1004,0,
      BlockHeader(version=4,
        hashPrevBlock=BlockHash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
        hashMerkleRoot=MerkleRootHash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
        timestamp=1234567890L,
        target=1000000L,
        nonce=1234L),
      None) shouldBe(

      BlockInfo(100,1004,0,
        BlockHeader(version=4,
          hashPrevBlock=BlockHash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
          hashMerkleRoot=MerkleRootHash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
          timestamp=1234567890L,
          target=1000000L,
          nonce=1234L),
        None)
      )
  }
*/
  "payload" should "be correctly encoded" in {
    if (message != null) {
      val encodedBits = encode(message)

      println("Encoded bits : " + encodedBits.bytes.toString)

      encodedBits shouldBe payloadBits
    }
  }

  "payload" should "be correctly decoded" in {
    val actual = decodeFully(payloadBits)

    println("actual message = " + actual)
    println("expected message = " + message)

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
