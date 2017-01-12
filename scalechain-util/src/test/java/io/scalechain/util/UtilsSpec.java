package io.scalechain.util;

import io.kotlintest.KTestJUnitRunner;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 Test Case
 */
public class UtilsSpec {
  @Before
  public void setUp() {
    // set up the test case
  }

  @After
  public void tearDown() {
    // tear down the test case
  }


  @Test
  public void testBigIntegerToBytes() {
  }

  @Test
  public void testBytesToBigInteger() {
  }

  @Test
  public void testCastToBool() {
  }

  @Test
  public void testCastToBigInteger() {
  }

  @Test
  public void testReverseBytes() {
  }

  @Test
  public void testDecodeMPI() {
  }

  @Test
  public void testEncodeMPI() {
  }

  @Test
  public void testUint32ToByteArrayBE() {
  }


  @Test
  public void testReadUint32BE() {
  }


  @Test
  public void testEqualsRange() {
  }


  @Test
  public void testRemoveAllInstancesOf() {
  }


  @Test
  public void testRemoveAllInstancesOfOp() {
  }


  @Test
  public void testBigIntegerToBytes_ShouldRoundTrip() {
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