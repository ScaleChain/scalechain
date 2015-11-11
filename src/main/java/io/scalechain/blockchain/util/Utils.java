package io.scalechain.blockchain.util;

import io.scalechain.blockchain.ErrorCode$;
import io.scalechain.blockchain.ScriptEvalException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Copied some functions from BitcoinJ by Mike Hearn.
 * Source : core/src/main/java/org/bitcoinj/core/Utils.java
 * Source : core/src/main/java/org/bitcoinj/script/Script.java
 */
public class Utils {

    public static long readUint32(InputStream stream) throws IOException {
        return (stream.read() & 0xFFL) |
                ((stream.read() & 0xFFL) << 8) |
                ((stream.read() & 0xFFL) << 16) |
                ((stream.read() & 0xFFL) << 24);
    }

    public static long readInt64(InputStream stream) throws IOException {
        return (stream.read() & 0xFFL) |
                ((stream.read() & 0xFFL) << 8) |
                ((stream.read() & 0xFFL) << 16) |
                ((stream.read() & 0xFFL) << 24) |
                ((stream.read() & 0xFFL) << 32) |
                ((stream.read() & 0xFFL) << 40) |
                ((stream.read() & 0xFFL) << 48) |
                ((stream.read() & 0xFFL) << 56);
    }

    public static boolean castToBool(byte[] data) {
        for (int i = 0; i < data.length; i++)
        {
            // "Can be negative zero" -reference client (see OpenSSL's BN_bn2mpi)
            if (data[i] != 0)
                return !(i == data.length - 1 && (data[i] & 0xFF) == 0x80);
        }
        return false;
    }

    private static BigInteger castToBigInteger(byte[] chunk) throws ScriptEvalException {
        if (chunk.length > 4)
            throw new ScriptEvalException(ErrorCode$.MODULE$.TooBigScriptInteger());
        return Utils.decodeMPI(Utils.reverseBytes(chunk), false);
    }

    /**
     * Returns a copy of the given byte array in reverse order.
     */
    private static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param hasLength can be set to false if the given array is missing the 4 byte length field
     */
    private static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
        byte[] buf;
        if (hasLength) {
            int length = (int) readUint32BE(mpi, 0);
            buf = new byte[length];
            System.arraycopy(mpi, 4, buf, 0, length);
        } else
            buf = mpi;
        if (buf.length == 0)
            return BigInteger.ZERO;
        boolean isNegative = (buf[0] & 0x80) == 0x80;
        if (isNegative)
            buf[0] &= 0x7f;
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param includeLength indicates whether the 4 byte length field should be included
     */
    private static byte[] encodeMPI(BigInteger value, boolean includeLength) {
        if (value.equals(BigInteger.ZERO)) {
            if (!includeLength)
                return new byte[] {};
            else
                return new byte[] {0x00, 0x00, 0x00, 0x00};
        }
        boolean isNegative = value.signum() < 0;
        if (isNegative)
            value = value.negate();
        byte[] array = value.toByteArray();
        int length = array.length;
        if ((array[0] & 0x80) == 0x80)
            length++;
        if (includeLength) {
            byte[] result = new byte[length + 4];
            System.arraycopy(array, 0, result, length - array.length + 3, array.length);
            uint32ToByteArrayBE(length, result, 0);
            if (isNegative)
                result[4] |= 0x80;
            return result;
        } else {
            byte[] result;
            if (length != array.length) {
                result = new byte[length];
                System.arraycopy(array, 0, result, 1, array.length);
            }else
                result = array;
            if (isNegative)
                result[0] |= 0x80;
            return result;
        }
    }

    private static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

    private static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFFL) << 24) |
                ((bytes[offset + 1] & 0xFFL) << 16) |
                ((bytes[offset + 2] & 0xFFL) << 8) |
                (bytes[offset + 3] & 0xFFL);
    }

    public static byte[] encodeStackInt(BigInteger value) throws ScriptEvalException {
        return Utils.reverseBytes(Utils.encodeMPI(value, false));
    }

    public static BigInteger decodeStackInt(byte[] encoded) throws ScriptEvalException {
        return castToBigInteger(encoded);
    }

}


