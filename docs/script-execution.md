# Introduction 
Implement Bitcoin script according to the script specification in the following link. 

https://en.bitcoin.it/wiki/Script

As a reference, appendix A in Mastering Bitcoin book is also good to read.

https://github.com/aantonop/bitcoinbook/blob/develop/appdx-scriptops.asciidoc

# Test data
These are hash values of following input.
We used following web site for getting the hash values.

http://www.sha1-online.com/

```
Hello World!
```

## SHA1
```
2ef7bde608ce5404e97d5f042f95f89f1c232871
```
## SHA256
```
7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069
```
## RIPEMD160
```
8476ee4631b9b30ac2754b0ee0c47e161d3f724c
```

# Code snippets
## SHA1
```
import java.security.MessageDigest
val md = MessageDigest.getInstance("SHA-1");
md.digest("Hello World!".getBytes())
```

## SHA256
```
import java.security.MessageDigest
val md = MessageDigest.getInstance("SHA-256");
md.digest("Hello World!".getBytes())
```

## RIPEMD160
```
import org.spongycastle.crypto.digests.RIPEMD160Digest
val md = new RIPEMD160Digest()
val raw = "HelloWorld".getBytes("US-ASCII")
md.update(raw, 0, raw.length)
val out = Array.fill[Byte](md.getDigestSize())(0)
md.doFinal(out, 0)
```

# Libraries
## Spongy Castle
For implementing RIPEMD160.

http://rtyley.github.io/spongycastle/

# Investigations
These are list of operations that needs investigation.

## OP_CHECKSIG
Investigate : Code snippet for checking a signature.

## OP_CHECKMUILTISIG
Investigate : Code snippet for checking multiple signatures.

## Parsing varint numbers
Investigate : Push, pop variable int, little endian encoded numbers.

## Disabled Ops
Investigate : Does the script execution halts before execution, or halts when it hits the op during execution?

# References
## Script Number encoding, decoding.

### Bitcoin Core 
```
class CScriptNum {
    ... 중간생략 ...
    explicit CScriptNum(const std::vector<unsigned char>& vch, bool fRequireMinimal,
                        const size_t nMaxNumSize = nDefaultMaxNumSize)
    {
        if (vch.size() > nMaxNumSize) {
            throw scriptnum_error("script number overflow");
        }
        if (fRequireMinimal && vch.size() > 0) {
            // Check that the number is encoded with the minimum possible
            // number of bytes.
            //
            // If the most-significant-byte - excluding the sign bit - is zero
            // then we're not minimal. Note how this test also rejects the
            // negative-zero encoding, 0x80.
            if ((vch.back() & 0x7f) == 0) {
                // One exception: if there's more than one byte and the most
                // significant bit of the second-most-significant-byte is set
                // it would conflict with the sign bit. An example of this case
                // is +-255, which encode to 0xff00 and 0xff80 respectively.
                // (big-endian).
                if (vch.size() <= 1 || (vch[vch.size() - 2] & 0x80) == 0) {
                    throw scriptnum_error("non-minimally encoded script number");
                }
            }
        }
        m_value = set_vch(vch);
    }
    
    static std::vector<unsigned char> serialize(const int64_t& value)
    {
        if(value == 0)
            return std::vector<unsigned char>();

        std::vector<unsigned char> result;
        const bool neg = value < 0;
        uint64_t absvalue = neg ? -value : value;

        while(absvalue)
        {
            result.push_back(absvalue & 0xff);
            absvalue >>= 8;
        }

//    - If the most significant byte is >= 0x80 and the value is positive, push a
//    new zero-byte to make the significant byte < 0x80 again.

//    - If the most significant byte is >= 0x80 and the value is negative, push a
//    new 0x80 byte that will be popped off when converting to an integral.

//    - If the most significant byte is < 0x80 and the value is negative, add
//    0x80 to it, since it will be subtracted and interpreted as a negative when
//    converting to an integral.

        if (result.back() & 0x80)
            result.push_back(neg ? 0x80 : 0);
        else if (neg)
            result.back() |= 0x80;

        return result;
    }

private:
    static int64_t set_vch(const std::vector<unsigned char>& vch)
    {
      if (vch.empty())
          return 0;

      int64_t result = 0;
      for (size_t i = 0; i != vch.size(); ++i)
          result |= static_cast<int64_t>(vch[i]) << 8*i;

      // If the input vector's most significant byte is 0x80, remove it from
      // the result's msb and return a negative.
      if (vch.back() & 0x80)
          return -((int64_t)(result & ~(0x80ULL << (8 * (vch.size() - 1)))));

      return result;
    }

```

### BitcoinJ 
Utils.encodeMPI, decodeMPI :
```

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param hasLength can be set to false if the given array is missing the 4 byte length field
     */
    public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
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
    public static byte[] encodeMPI(BigInteger value, boolean includeLength) {
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
    
    ... 중간생략 ...
    
```

Script.castToBigInteger converts byte array in the script execution stack to a BigInteger number. 
```
    private static BigInteger castToBigInteger(byte[] chunk) throws ScriptException {
        if (chunk.length > 4)
            throw new ScriptException("Script attempted to use an integer larger than 4 bytes");
        return Utils.decodeMPI(Utils.reverseBytes(chunk), false);
    }
    
    /*
        This function is used to convert an item(a byte array) on the script execution stack to a big integer number. 
            BigInteger numericOPnum2 = castToBigInteger(stack.pollLast());
            BigInteger numericOPnum1 = castToBigInteger(stack.pollLast());    
    */
```

Script.executeScript ; OP_1NEGATE - Push a number onto stack.
```
stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false)));
```

## Script Execution

```
bool EvalScript(vector<vector<unsigned char> >& stack, const CScript& script, unsigned int flags, const BaseSignatureChecker& checker, ScriptError* serror)
```