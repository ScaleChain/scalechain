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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 Test Case
 */
public class UtilsSpec extends Utils{
  @Before
  public void setUp() {
    // set up the test case
  }

  @After
  public void tearDown() {
    // tear down the test case
  }

  private byte[] getBigIntBytes(String value) {
    return Utils.bigIntegerToBytes( new BigInteger(value), new BigInteger(value).toByteArray().length);
  }

  @Test
  public void testCastToBool() {
    assertFalse( Utils.castToBool( getBigIntBytes("0" ) ) );
//    assertFalse( Utils.castToBool( getBigIntBytes("0.0" ) ) );
    // We need to treat -0 and -0.0 as false
    assertFalse( Utils.castToBool( getBigIntBytes("-0" ) ) );

//    assertFalse( Utils.castToBool( getBigIntBytes("-0.0" ) ) );
    assertTrue( Utils.castToBool( getBigIntBytes("1" ) ) );
//    assertTrue( Utils.castToBool( getBigIntBytes("0.1" ) ) );
    assertTrue( Utils.castToBool( getBigIntBytes("-1" ) ) );
//    assertTrue( Utils.castToBool( getBigIntBytes("-0.1" ) ) );
  }


  @Test
  public void testCastToBigInteger() {

  }

  @Test
  public void testReverseBytes() {
    assertTrue(
      Arrays.equals(
        Utils.reverseBytes(new byte[] {}), new byte[] {}
      )
    );

    assertTrue(
      Arrays.equals(
        Utils.reverseBytes(new byte[] {1}), new byte[] {1}
      )
    );

    assertTrue(
      Arrays.equals(
        Utils.reverseBytes(new byte[] {1,2}), new byte[] {2,1}
      )
    );
  }

  private void checkDecodeMPI(byte[] bytes, boolean includeLength, long expectedValue)
  {
    BigInteger decodedValue = Utils.decodeMPI(bytes, includeLength);
    assertEquals(BigInteger.valueOf(expectedValue), decodedValue);
  }


  @Test
  public void testDecodeMPI() {
    checkDecodeMPI(new byte[] {}, false, 0);
    checkDecodeMPI(new byte[] {0,0,0,0}, true, 0);
  }

  private void checkEncodeMPI(long value, boolean includeLength, byte[] expectedBytes)
  {
    byte[] encoded = Utils.encodeMPI(BigInteger.valueOf(value), includeLength);
    assertTrue(
      Arrays.equals(
        encoded, expectedBytes
      )
    );
  }

  @Test
  public void testEncodeMPI() {
    checkEncodeMPI(0, false, new byte[] {});
    checkEncodeMPI(0, true, new byte[] {0,0,0,0});
  }

  private void checkRoundTripMPI(BigInteger value, boolean includeLength)
  {
    byte[] encoded = Utils.encodeMPI(value, includeLength);
    assertEquals( value, Utils.decodeMPI(encoded, includeLength) );
  }

  @Test
  public void testRoundTripMPI() {
    for (String stringValue : new String[]{
      "0",
      "-0",
      "1",
      "-1",
      "255",
      "-255",
      "256",
      "-256",
      "32767",
      "-32767",
      "32768",
      "-32768",
      "65535",
      "-65535",
      "65536",
      "-65536",
      "2147483647",
      "-2147483647",
      "2147483648",
      "-2147483648",
      "4294967295",
      "-4294967295",
      "4294967296",
      "-4294967296",
      "9223372036854775807",
      "-9223372036854775807",
      "9223372036854775808",
      "-9223372036854775808",
      "18446744073709551615",
      "-18446744073709551615",
      "18446744073709551616",
      "-18446744073709551616"
    }) {
      BigInteger value = new BigInteger(stringValue);
      checkRoundTripMPI(value, true);
      checkRoundTripMPI(value, false);
    }
  }

    private void checkUint32ToByteArrayBE(long value, byte[] expectedBytes) {
    {
      // Write at offset 0
      byte[] bytes = new byte[4];
      uint32ToByteArrayBE(value, bytes, 0);
      assertTrue(
        Arrays.equals(
          bytes, expectedBytes
        )
      );
    }

    {
      // Write at offset 1
      byte[] bytes = new byte[]{0,0,0,0,0};
      uint32ToByteArrayBE(value, bytes, 1);
      assertTrue( bytes[0] == 0 );
      assertTrue( bytes[1] == expectedBytes[0] );
      assertTrue( bytes[2] == expectedBytes[1] );
      assertTrue( bytes[3] == expectedBytes[2] );
      assertTrue( bytes[4] == expectedBytes[3] );
    }
  }

  @Test
  public void testUint32ToByteArrayBE() {
    checkUint32ToByteArrayBE(0, new byte[]{0,0,0,0});
    checkUint32ToByteArrayBE(1, new byte[]{0,0,0,1});
    checkUint32ToByteArrayBE(256, new byte[]{0,0,1,0});
    checkUint32ToByteArrayBE(256*256, new byte[]{0,1,0,0});
    checkUint32ToByteArrayBE(256*256*256, new byte[]{1,0,0,0});
    checkUint32ToByteArrayBE((1L << 32) -1, new byte[]{-1,-1,-1,-1});
  }


  @Test
  public void testReadUint32BE() {
    assertEquals(0, readUint32BE(new byte[]{0,0,0,0},0));
    assertEquals(1, readUint32BE(new byte[]{0,0,0,1},0));
    assertEquals(256, readUint32BE(new byte[]{0,0,1,0},0));
    assertEquals(256*256, readUint32BE(new byte[]{0,1,0,0},0));
    assertEquals(256*256*256, readUint32BE(new byte[]{1,0,0,0},0));
    assertEquals((1L << 32) -1, readUint32BE(new byte[]{-1,-1,-1,-1},0));

    // Make sure the it works well even though the offset is greater than 0
    assertEquals(1, readUint32BE(new byte[]{0,0,0,0,1},1));
  }


  @Test
  public void testEqualsRange() {
    // return false if the left array does not have enough bytes
    assertFalse( equalsRange(new byte[] {0,0}, 1, new byte[] {0,0}) );

    // offset : 0
    assertTrue( equalsRange(new byte[] {}, 0, new byte[] {}) );
    assertTrue( equalsRange(new byte[] {0}, 0, new byte[] {0}) );

    assertTrue( equalsRange(new byte[] {0}, 0, new byte[] {0}) );
    assertTrue( equalsRange(new byte[] {1}, 0, new byte[] {1}) );
    assertTrue( equalsRange(new byte[] {1,2}, 0, new byte[] {1,2}) );

    assertFalse( equalsRange(new byte[] {0}, 0, new byte[] {1}) );
    assertFalse( equalsRange(new byte[] {1}, 0, new byte[] {0}) );
    assertFalse( equalsRange(new byte[] {0,2}, 0, new byte[] {1,2}) );
    assertFalse( equalsRange(new byte[] {1,2}, 0, new byte[] {0,2}) );
    assertFalse( equalsRange(new byte[] {1,0}, 0, new byte[] {1,2}) );
    assertFalse( equalsRange(new byte[] {1,2}, 0, new byte[] {1,0}) );

    // offset : 1, with the same tests as above. Prefix the left array with 100
    assertTrue( equalsRange(new byte[] {100}, 1, new byte[] {}) );
    assertTrue( equalsRange(new byte[] {100,0}, 1, new byte[] {0}) );

    assertTrue( equalsRange(new byte[] {100,0}, 1, new byte[] {0}) );
    assertTrue( equalsRange(new byte[] {100,1}, 1, new byte[] {1}) );
    assertTrue( equalsRange(new byte[] {100,1,2}, 1, new byte[] {1,2}) );

    assertFalse( equalsRange(new byte[] {100,0}, 1, new byte[] {1}) );
    assertFalse( equalsRange(new byte[] {100,1}, 1, new byte[] {0}) );
    assertFalse( equalsRange(new byte[] {100,0,2}, 1, new byte[] {1,2}) );
    assertFalse( equalsRange(new byte[] {100,1,2}, 1, new byte[] {0,2}) );
    assertFalse( equalsRange(new byte[] {100,1,0}, 1, new byte[] {1,2}) );
    assertFalse( equalsRange(new byte[] {100,1,2}, 1, new byte[] {1,0}) );
  }

  private void checkRemoveAllInstancesOf(byte[] source, byte[] pattern, byte[] result) {
    assertTrue(
      Arrays.equals(
        result,
        Utils.removeAllInstancesOf(source, pattern)
      )
    );
  }

  private void checkRemoveAllInstancesOfOp(byte[] source, byte op, byte[] result) {
    assertTrue(
      Arrays.equals(
        result,
        Utils.removeAllInstancesOfOp(source, op)
      )
    );
  }


  @Test
  public void testRemoveAllInstancesOf() {
    // OpCode less than 0x4f indicate PUSH DATA operations.
    // Test without any PUSH DATA.
    // nothing to remove
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {0}, new byte[]{0x4f,0x50,0x51,0x52,0x53});
    // nothing to remove
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {1}, new byte[]{0x4f,0x50,0x51,0x52,0x53});
    // nothing to remove
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {0x4f,1}, new byte[]{0x4f,0x50,0x51,0x52,0x53});
    // remove the prepending bytes
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {0x4f}, new byte[]{0x50,0x51,0x52,0x53});
    // remove patterns in the mid of script operations
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {0x50}, new byte[]{0x4f,0x51,0x52,0x53});
    // remove patterns at the end of script operations
    checkRemoveAllInstancesOf(new byte[]{0x4f,0x50,0x51,0x52,0x53}, new byte[] {0x53}, new byte[]{0x4f,0x50,0x51,0x52});

    // with OpPushData. 0xf1 is the data pushed.
    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOf(new byte[]{0x01,0x71,0x51,0x52,0x53}, new byte[] {0x71}, new byte[]{0x01,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x01,0x71,0x51,0x52,0x53}, new byte[] {0x01}, new byte[]{0x51,0x52,0x53});

    // OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e)
    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOf(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, new byte[] {0x71}, new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, new byte[] {0x01}, new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, new byte[] {0x4c}, new byte[]{0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, new byte[] {0x4c,0x01}, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, new byte[] {0x52}, new byte[]{0x4c,0x01,0x71,0x51,0x53});

    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x71}, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x01}, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x00}, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x4d}, new byte[]{0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x4d,0x01}, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x52}, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x53});

    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x71}, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x01}, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x00}, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x4e}, new byte[]{0x51,0x52,0x53});
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x4e,0x01}, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOf(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, new byte[] {0x52}, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x53});
  }


  @Test
  public void testRemoveAllInstancesOfOp() {
    // OpCode less than 0x4f indicate PUSH DATA operations.
    // Test without any PUSH DATA.
    // nothing to remove
    checkRemoveAllInstancesOfOp(new byte[]{0x4f,0x50,0x51,0x52,0x53}, (byte)0, new byte[]{0x4f,0x50,0x51,0x52,0x53});
    // nothing to remove
    checkRemoveAllInstancesOfOp(new byte[]{0x4f,0x50,0x51,0x52,0x53}, (byte)1, new byte[]{0x4f,0x50,0x51,0x52,0x53});
    // remove the prepending bytes
    checkRemoveAllInstancesOfOp(new byte[]{0x4f,0x50,0x51,0x52,0x53}, (byte)0x4f, new byte[]{0x50,0x51,0x52,0x53});
    // remove patterns in the mid of script operations
    checkRemoveAllInstancesOfOp(new byte[]{0x4f,0x50,0x51,0x52,0x53}, (byte)0x50, new byte[]{0x4f,0x51,0x52,0x53});
    // remove patterns at the end of script operations
    checkRemoveAllInstancesOfOp(new byte[]{0x4f,0x50,0x51,0x52,0x53}, (byte)0x53, new byte[]{0x4f,0x50,0x51,0x52});

    // with OpPushData. 0xf1 is the data pushed.
    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOfOp(new byte[]{0x01,0x71,0x51,0x52,0x53}, (byte)0x71, new byte[]{0x01,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x01,0x71,0x51,0x52,0x53}, (byte)0x01, new byte[]{0x51,0x52,0x53});

    // OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e)
    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOfOp(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, (byte)0x71, new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOfOp(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, (byte)0x01, new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, (byte)0x4c, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4c,0x01,0x71,0x51,0x52,0x53}, (byte)0x52, new byte[]{0x4c,0x01,0x71,0x51,0x53});

    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOfOp(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, (byte)0x71, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOfOp(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, (byte)0x01, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOfOp(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, (byte)0x00, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, (byte)0x4d, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4d,0x01,0x00,0x71,0x51,0x52,0x53}, (byte)0x52, new byte[]{0x4d,0x01,0x00,0x71,0x51,0x53});

    // The data to be pushed is not scrubbed
    checkRemoveAllInstancesOfOp(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, (byte)0x71, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOfOp(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, (byte)0x01, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    checkRemoveAllInstancesOfOp(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, (byte)0x00, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53});
    // The script as well as data to be pushed is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, (byte)0x4e, new byte[]{0x51,0x52,0x53});
    // The script op code is scrubbed.
    checkRemoveAllInstancesOfOp(new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x52,0x53}, (byte)0x52, new byte[]{0x4e,0x01,0x00,0x00,0x00,0x71,0x51,0x53});
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