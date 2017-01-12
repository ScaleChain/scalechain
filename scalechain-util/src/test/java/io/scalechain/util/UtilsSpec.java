package io.scalechain.util;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 Test Case
 */
class UtilsSpec {
  @Before
  void setUp() {
    // set up the test case
  }

  @After
  void tearDown() {
    // tear down the test case
  }


  @Test
  void testBigIntegerToBytes() {
  }

  @Test
  void testBytesToBigInteger() {
  }
  @Test
  void testCastToBool() {
  }
  @Test
  void testCastToBigInteger() {
  }
  @Test
  void testReverseBytes() {
  }

  @Test
  void testDecodeMPI() {
  }

  @Test
  void testEncodeMPI() {
  }

  @Test
  void testUint32ToByteArrayBE() {
  }


  @Test
  void testReadUint32BE() {
  }


  @Test
  void testEqualsRange() {
  }


  @Test
  void testRemoveAllInstancesOf() {
  }


  @Test
  void testRemoveAllInstancesOfOp() {
  }


  @Test
  void testBigIntegerToBytes_ShouldRoundTrip() {
    for (int i = 0; i<1000; i++) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
      SecureRandom random = new SecureRandom();
      random.setSeed(random.generateSeed(32));

      byte[] originalKeyArray = new byte[32];

      random.nextBytes(originalKeyArray);

      BigInteger originalBigInteger = Utils.bytesToBigInteger(originalKeyArray);

      byte[] encodedKeyArray = Utils.bigIntegerToBytes(originalBigInteger, 32);

      BigInteger createdBigInteger = Utils.bytesToBigInteger(encodedKeyArray);


      assertTrue(Arrays.equals( encodedKeyArray, originalKeyArray) );
      assertEquals( createdBigInteger, originalBigInteger);
    }
  }
}