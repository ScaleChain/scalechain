package io.scalechain.blockchain.oap.util;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutputTest;
import org.junit.Test;

/**
 * Created by shannon on 16. 12. 5.
 */
public class LEB128Test {
    static {
        System.out.println(LEB128Test.class.getName()+"."+" LOADED.");
    }

    int quantities[] = {
            0,
            127,
            128,  // 0x80(1000 0000 ==> 000 0001 000 0000 ==> 1000 0000 0000 0001 ==> 0x80 0x01
            1000, //03e8(0000 0011 1110 1000)  000 0111 110 1000 ==> 1110 1000 0000 0111
            10000,
            100000,
            -1 // ff ff ff ff(1111 1111 1111 1111 1111 1111 1111 1111 ==> 1111 1111111 1111111 1111111 1111111
            // ==> 11111111 11111111 11111111 11111111 00001111 ==> ff ff ff ff 0f
    };

    byte[][] encoded = {
            { (byte)0x00 },
            { (byte)0x7f },
            { (byte)0x80, (byte)0x01 },
            { (byte)0xe8, (byte)0x07 },
            { (byte)0x90, (byte)0x4e },
            { (byte)0xa0, (byte)0x8d, (byte)0x06 },
            { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x0f }
    };

    @Test
    public void encodeTest() {
        for(int i = 0;i < quantities.length;i++) {
            byte[] encoded = LEB128Codec.encode(quantities[i]);
            assert comprareBytes(encoded, this.encoded[i]);
        }
    }

    @Test
    public void encode2Test() {
        byte[][] encoded2 = new byte[encoded.length][6];
        for(int i = 0;i < quantities.length;i++) {
            int offset = LEB128Codec.encode(quantities[i], encoded2[i], 1);
            assert offset == 1 + encoded[i].length;
            assert comprareBytes2(encoded[i], encoded2[i], 1, encoded[i].length);
        }
    }
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void encodeOverflowTest() {
        byte[] encoded3 = new byte[4];
        LEB128Codec.encode(-1, encoded3, 0);
    }

    @Test(expected = OapException.class)
    public void decodeInvalidTest() throws OapException {
        byte[] invalid = { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x8f };
        int decoded = LEB128Codec.decode(invalid);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void decodeInvalid2Test() throws OapException {
        byte[] invalid = { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x8f };
        int[] pair = LEB128Codec.decode(invalid, 0);
    }

    @Test
    public void decodeTest() throws Exception {
        for(int i = 0;i < encoded.length;i++) {
            int decoded = LEB128Codec.decode(encoded[i]);
            assert decoded == this.quantities[i];
        }
    }

    @Test
    public void encodeDecodeTest() throws Exception {
        for(int i = 0;i < quantities.length;i++) {
            assert quantities[i] == LEB128Codec.decode(LEB128Codec.encode(quantities[i]));
        }
    }

    @Test
    public void decodeEncodeTest() throws Exception {
        for(int i = 0;i < encoded.length;i++) {
            assert comprareBytes(encoded[i], LEB128Codec.encode(LEB128Codec.decode(encoded[i])));
        }
    }

    public static boolean comprareBytes(byte[] a1, byte[] a2) {
        if (a1.length != a2.length) return false;
        for(int i =0 ;i < a1.length;i++) {
            if (a1[i] != a2[i])
                return false;
        }
        return true;
    }

    public static boolean comprareBytes2(byte[] a1, byte[] a2, int offset, int length) {
        if (a1.length + offset > a2.length) return false;
        for(int i = 0 ;i < length;i++) {
            if (a1[i] != a2[i + offset])
                return false;
        }
        return true;
    }

}
