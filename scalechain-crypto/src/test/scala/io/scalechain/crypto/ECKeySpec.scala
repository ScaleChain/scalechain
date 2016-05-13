package io.scalechain.crypto

import java.math.BigInteger

import io.scalechain.util.HexUtil
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class ECKeySpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  "ECKey.ECDSASignature.isEncodingCanonical" should "validate a correct signature format" in {
    val signature : Array[Byte] = HexUtil.bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01")

    ECKey.ECDSASignature.isEncodingCanonical(signature) shouldBe true
  }

  "sign" should "correctly sign input data" in {
    val ZERO_HASH : Array[Byte] = HexUtil.bytes("0" * 32)
    // Test that we can construct an ECKey from a private key (deriving the public from the private), then signing
    // a message with it.
    val privkey : BigInteger = new BigInteger(1, HexUtil.bytes("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
    val publicKey : Array[Byte] = ECKey.publicKeyFromPrivate(privkey, true)
    val signature : ECKey.ECDSASignature = ECKey.doSign(ZERO_HASH, privkey)
    ECKey.verify(ZERO_HASH, signature, publicKey) shouldBe true

    // Test interop with a signature from elsewhere.
    val sig : Array[Byte] = HexUtil.bytes(
      "3046022100dffbc26774fc841bbe1c1362fd643609c6e42dcb274763476d87af2c0597e89e022100c59e3c13b96b316cae9fa0ab0260612c7a133a6fe2b3445b6bf80b3123bf274d");

    ECKey.verify(ZERO_HASH, ECKey.ECDSASignature.decodeFromDER(sig), publicKey) shouldBe true
  }
}
