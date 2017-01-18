package io.scalechain.blockchain.oap.util;

import io.scalechain.blockchain.oap.exception.OapException;

/**
 * Created by shannon on 16. 11. 17.
 */
public class LEB128Codec {

    /**
     * encodes an integer value using LEB128 encoding.
     * @param value
     * @return
     */
    public static byte[] encode(int value) {
        byte[] buffer = new byte[5];
        int pos = 0;
        do {
            byte b = (byte) (value & 0x7f);
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buffer[pos++] = b;
        } while (value != 0);

        byte[] result = new byte[pos];
        System.arraycopy(buffer, 0, result, 0, pos);

        return result;
    }

    /**
     * decode byte array using LEB128 decoding
     *
     * @param encoded
     * @return
     */
    public static int decode(byte[] encoded) throws OapException {
        int result = 0, shift = 0;
        if ((encoded[encoded.length-1] & 0x80) != 0) throw new OapException(OapException.COLORING_ERROR, "Not a LEB128 encoded data");
        for (byte b : encoded) {
            result |= ((b & 0x7f) << shift);
            if ((b & 0x80) == 0)
                break;
            shift += 7;
        }
        return result;
    }

    /**
     * encodes an integer "value: using LEB128 encoding into "dest".
     * bytes are written to "dest" starting position "offset"
     *
     * returns the next position of encoded data.
     *
     * @param value
     * @param dest
     * @param offset
     * @return
     */
    public static int encode(int value, byte[] dest, int offset) {
        byte[] buffer = new byte[5];
        int pos = 0;
        do {
            byte b = (byte) (value & 0x7f);
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buffer[pos++] = b;
        } while (value != 0);
        if (dest.length < (pos + offset)) {
            throw new ArrayIndexOutOfBoundsException(pos + offset);
        }
        System.arraycopy(buffer, 0, dest, offset, pos);
        return offset + pos;
    }

    /**
     * decode LEB128 encoded integer from encoded.
     * return decoded integer and the next position after encoded data.
     *
     * @param encoded
     * @param offset
     * @return
     */
    public static int[] decode(byte[] encoded, int offset) {
        int result = 0, shift = 0;
        for (; offset < encoded.length; offset++) {
            result |= ((encoded[offset] & 0x7f) << shift);
            if ((encoded[offset] & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        if (offset == encoded.length) throw new ArrayIndexOutOfBoundsException(offset);
        return new int[]{result, ++offset}; // { result, offsetToNextElement }
    }
}
