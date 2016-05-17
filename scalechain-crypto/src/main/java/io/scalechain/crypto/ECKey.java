package io.scalechain.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.*;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;

import java.math.BigInteger;
import java.io.*;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Source code copied from Mike Hearn's BitcoinJ.
 */
public class ECKey {
    private static final Logger log = LoggerFactory.getLogger(ECKey.class);

    /**
     * Signs the given hash and returns the R and S components as BigIntegers.
     *
     * @param inputBytes The input bytes to sign.
     * @param privateKeyForSigning The private key we use for signing.
     */
    public static ECDSASignature doSign(byte[] inputBytes, BigInteger privateKeyForSigning) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(inputBytes);
        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }

    /** Decode the encoded public key to get the ECPoint.
     *
     * @param pub The encoded public key.
     * @return The public key, which is a point on the elliptic curve.
     */
    public static ECPoint decodePublicKey(byte[] pub ) {
        return CURVE.getCurve().decodePoint(pub);
    }

    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     *
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(decodePublicKey(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures. Those signatures
            // are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            log.error("Caught NPE inside bouncy castle");
            e.printStackTrace();
            return false;
        }
    }

    // The parameters of the secp256k1 curve that Bitcoin uses.
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    /** The parameters of the secp256k1 curve that Bitcoin uses. */
    public static final ECDomainParameters CURVE;
    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
     * sure what this is about, you can ignore it.
     */
    public static final BigInteger HALF_CURVE_ORDER;


    /**
     * Returns public key bytes from the given private key. To convert a byte array into a BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = publicPointFromPrivate(privKey);
        return point.getEncoded(compressed);
    }

    /**
     * Returns public key point from the given private key. To convert a byte array into a BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     */
    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order,
         * but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    static {
        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations. Increasing the width
        // number makes calculations faster, but at a cost of extra memory usage and with decreasing returns. 12 was
        // picked after consulting with the BC team.
        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }


    /**
     * Groups the two components that make up a signature, and provides a way to encode to DER form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        /** The two components of the signature. */
        public final BigInteger r, s;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * Returns true if the S component is "low", that means it is below {@link ECKey#HALF_CURVE_ORDER}. See <a
         * href="https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures">BIP62</a>.
         */
        public boolean isCanonical() {
            return s.compareTo(HALF_CURVE_ORDER) <= 0;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         */
        public ECDSASignature toCanonicalised() {
            if (!isCanonical()) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        /**
         * DER is an international standard for serializing data structures which is widely used in cryptography.
         * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
         * of the signature, as recognized by OpenSSL and other libraries.
         */
        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        public static ECDSASignature decodeFromDER(byte[] bytes) {
            ASN1InputStream decoder = null;
            try {
                decoder = new ASN1InputStream(bytes);
                DLSequence seq = (DLSequence) decoder.readObject();
                if (seq == null)
                    throw new RuntimeException("Reached past end of ASN.1 stream.");
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) seq.getObjectAt(0);
                    s = (ASN1Integer) seq.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(e);
                }
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
                // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
                return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (decoder != null)
                    try { decoder.close(); } catch (IOException x) {}
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-72 bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ECDSASignature other = (ECDSASignature) o;
            return r.equals(other.r) && s.equals(other.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, s);
        }

        public static boolean isEncodingCanonical(byte[] signature) {
            // See reference client's IsValidSignatureEncoding, https://bitcointalk.org/index.php?topic=8392.msg127623#msg127623
            // A canonical signature exists of: <30> <total len> <02> <len R> <R> <02> <len S> <S> <hashtype>
            // Where R and S are not negative (their first byte has its highest bit not set), and not
            // excessively padded (do not start with a 0 byte, unless an otherwise negative number follows,
            // in which case a single 0 byte is necessary and even required).
            if (signature.length < 9 || signature.length > 73)
                return false;
/*
            // BUGBUG : ANYONECANPAY was byte type, but changed it to int. Make sure it is OK.
            int hashType = signature[signature.length-1] & ~TransactionSigHash$.MODULE$.HASH_TYPE_MASK();
            if (hashType == 0)
                hashType = 1;

            if (hashType < (TransactionSigHash$.MODULE$.ALL()) || hashType > (TransactionSigHash$.MODULE$.SINGLE()))
                return false;
*/
            //                   "wrong type"                  "wrong length marker"
            if ((signature[0] & 0xff) != 0x30 || (signature[1] & 0xff) != signature.length-3)
                return false;

            int lenR = signature[3] & 0xff;
            if (5 + lenR >= signature.length || lenR == 0)
                return false;
            int lenS = signature[5+lenR] & 0xff;
            if (lenR + lenS + 7 != signature.length || lenS == 0)
                return false;

            // R value type mismatch
            if (signature[4-2] != 0x02)
                return false;
/*
            Fix for : https://github.com/ScaleChain/scalechain/issues/33
            BUGBUG : Need to make sure if we can safely get rid of this check.

            // R value negative
            if ((signature[4] & 0x80) == 0x80)
                return false;
*/
            if (lenR > 1 && signature[4] == 0x00 && (signature[4+1] & 0x80) != 0x80)
                return false; // R value excessively padded

            // S value type mismatch
            if (signature[6 + lenR - 2] != 0x02)
                return false;
/*
            Fix for : https://github.com/ScaleChain/scalechain/issues/33
            BUGBUG : Need to make sure if we can safely get rid of this check.

            // S value negative
            if ((signature[6 + lenR] & 0x80) == 0x80)
                return false;
*/
            if (lenS > 1 && signature[6 + lenR] == 0x00 && (signature[6 + lenR + 1] & 0x80) != 0x80)
                return false; // S value excessively padded
            return true;
        }
    }


}
