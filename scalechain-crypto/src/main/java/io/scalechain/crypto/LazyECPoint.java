package io.scalechain.crypto;

import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.math.ec.ECPoint;

import java.util.Arrays;

/**
 * Source code copied from Mike Hearn's BitcoinJ.
 */
// TODO: Convert LazyECPoint into scala version
public class LazyECPoint {
    // If curve is set, bits is also set. If curve is unset, point is set and bits is unset. Point can be set along
    // with curve and bits when the cached form has been accessed and thus must have been converted.

    private final ECCurve curve;
    private final byte[] bits;

    // This field is effectively final - once set it won't change again. However it can be set after
    // construction.
    private ECPoint point;

    public LazyECPoint(ECCurve curve, byte[] bits) {
        this.curve = curve;
        this.bits = bits;
    }

    public ECPoint get() {
        if (point == null)
            point = curve.decodePoint(bits);
        return point;
    }

    // Delegated methods.

    public byte[] getEncoded() {
        if (bits != null)
            return Arrays.copyOf(bits, bits.length);
        else
            return get().getEncoded();
    }

    public boolean isCompressed() {
        if (bits != null)
            return bits[0] == 2 || bits[0] == 3;
        else
            return get().isCompressed();
    }

    public boolean isValid() {
        return get().isValid();
    }

    public byte[] getEncoded(boolean compressed) {
        if (compressed == isCompressed() && bits != null)
            return Arrays.copyOf(bits, bits.length);
        else
            return get().getEncoded(compressed);
    }

    public ECPoint add(ECPoint b) {
        return get().add(b);
    }

    public ECPoint normalize() {
        return get().normalize();
    }

    public ECFieldElement getX() {
        return this.normalize().getXCoord();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(getCanonicalEncoding(), ((LazyECPoint)o).getCanonicalEncoding());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCanonicalEncoding());
    }

    private byte[] getCanonicalEncoding() {
        return getEncoded(true);
    }
}
