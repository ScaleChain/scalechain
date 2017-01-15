package io.scalechain.crypto;


import io.scalechain.util.HexUtil;
import io.scalechain.util.Utils;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 Test Case
 */
public class ECKeySpec {
  @Before
  public void setUp() {
    // set up the test case
  }

  @After
  public void tearDown() {
    // tear down the test case
  }

  private byte[] getBigIntBytes(String value) {
    return Utils.bigIntegerToBytes(new BigInteger(value), new BigInteger(value).toByteArray().length);
  }

  @Test
  public void testIsEncodingCanonicalValidateSignatureFormat() {
    byte[] signature = HexUtil.bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01");

    assertTrue( ECKey.ECDSASignature.isEncodingCanonical(signature) );
  }


  private BigInteger PRIVATE_KEY = new BigInteger(1, HexUtil.bytes("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
  private byte[] ZERO_HASH = new byte[]{0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0};

  @Test
  public void testDoSignCreatesCorrectSignature() {
    // Test that we can construct an ECKey from a private key (deriving the public from the private), then signing
    // a message with it.
    byte[] publicKey = ECKey.publicKeyFromPrivate(PRIVATE_KEY, true);
    ECKey.ECDSASignature signature = ECKey.doSign(ZERO_HASH, PRIVATE_KEY);

    assertTrue(ECKey.verify(ZERO_HASH, signature, publicKey));

    //println( "encoded signature :" + HexUtil.hex(signature.encodeToDER()) )

    // Test interop with a signature from elsewhere.
    byte[] sig = HexUtil.bytes(
      "3046022100dffbc26774fc841bbe1c1362fd643609c6e42dcb274763476d87af2c0597e89e022100c59e3c13b96b316cae9fa0ab0260612c7a133a6fe2b3445b6bf80b3123bf274d");

    assertTrue( ECKey.verify(ZERO_HASH, ECKey.ECDSASignature.decodeFromDER(sig), publicKey) );

  }

  @Test
  public void testRoundTripDerFormat() {
    ECKey.ECDSASignature signature = ECKey.doSign(ZERO_HASH, PRIVATE_KEY);
    byte[] signatureInDerFormat = signature.encodeToDER();
    ECKey.ECDSASignature decodedSignature = ECKey.ECDSASignature.decodeFromDER(signatureInDerFormat);

    assertEquals( decodedSignature.hashCode(), signature.hashCode() );
    assertEquals( decodedSignature, signature);
  }

  private BigInteger generatePrivateKey() {
    SecureRandom random = new SecureRandom();
    byte[] keyValue = new byte[32];
    random.nextBytes(keyValue);
    return Utils.bytesToBigInteger(keyValue);
  }

  @Test
  public void testDecodePublicKey() {
    BigInteger privateKey = generatePrivateKey();

    for (boolean compressed : new boolean[] {true,false}) {
      byte[] encodedPublicKey = ECKey.publicKeyFromPrivate(privateKey, compressed);
      ECPoint point = ECKey.decodePublicKey(encodedPublicKey);
      assertTrue(
        Arrays.equals(
          encodedPublicKey,
          point.getEncoded(compressed)
        )
      );
    }
  }
}