package io.scalechain.wallet;

/**
 * Source code copied from Mike Hearn's BitcoinJ.
 */
// TODO: Convert Address into scala version
public class Address extends VersionedChecksummedBytes {
    /**
     * An address is a RIPEMD160 hash of a public key, therefore is always 160 bits or 20 bytes.
     */
    public static final int LENGTH = 20;

    /**
     * Construct an address from parameters and the hash160 form. Example:<p>
     *
     * <pre>new Address(MainNetParams.get(), Hex.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));</pre>
     */
    public Address(int version, byte[] hash160) {
        super(version, hash160);
    }
}
