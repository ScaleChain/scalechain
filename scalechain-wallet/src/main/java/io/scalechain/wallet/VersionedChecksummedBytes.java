package io.scalechain.wallet;

import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import io.scalechain.crypto.Base58;
import io.scalechain.crypto.HashFunctions;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Source code copied from Mike Hearn's BitcoinJ.
 */
// TODO: Convert VersionedChecksummedBytes into scala version
public class VersionedChecksummedBytes implements Serializable, Cloneable, Comparable<VersionedChecksummedBytes> {
    protected final int version;
    protected byte[] bytes;

    protected VersionedChecksummedBytes(int version, byte[] bytes) {
        checkArgument(version >= 0 && version < 256);
        this.version = version;
        this.bytes = bytes;
    }

    /**
     * Returns the base-58 encoded String representation of this
     * object, including version and checksum bytes.
     */
    public final String toBase58() {
        //   1 byte version + data bytes + 4 bytes check code (a truncated hash)
        byte[] addressBytes = new byte[1 + bytes.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        byte[] checksum = HashFunctions.sha256Twice(addressBytes, 0, bytes.length + 1).value();
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    @Override
    public String toString() {
        return toBase58();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation uses an optimized Google Guava method to compare <code>bytes</code>.
     */
    @Override
    public int compareTo(VersionedChecksummedBytes o) {
        int result = Ints.compare(this.version, o.version);
        return result != 0 ? result : UnsignedBytes.lexicographicalComparator().compare(this.bytes, o.bytes);
    }
}
