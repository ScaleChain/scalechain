package io.scalechain.blockchain.util;

import io.scalechain.blockchain.BlockDataInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copied some functions from BitcoinJ by Mike Hearn.
 * Source : core/src/main/java/org/bitcoinj/core/Utils.java
 */
public class Utils {
    /*
    public static long readUint32(byte[] bytes, int offset) {
        return (bytes[offset++] & 0xFFL) |
                ((bytes[offset++] & 0xFFL) << 8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset] & 0xFFL) << 24);
    }

    public static long readInt64(byte[] bytes, int offset) {
        return (bytes[offset++] & 0xFFL) |
                ((bytes[offset++] & 0xFFL) << 8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset++] & 0xFFL) << 24) |
                ((bytes[offset++] & 0xFFL) << 32) |
                ((bytes[offset++] & 0xFFL) << 40) |
                ((bytes[offset++] & 0xFFL) << 48) |
                ((bytes[offset] & 0xFFL) << 56);
    }
    */

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


}


