# Introduction
Most of script operations are pretty simple. 
Ex> OP_ADD; pop two int values from the stack, push an int value that sums up the two values.

But the OP_CHECKSIG and OP_CHECKMULTISIG is not as simple as OP_ADD.
We will investigate how OP_CHECKSIG and OP_CHECKMULTISIG works before implementing them.

# How elliptic curve cryptography works
http://arstechnica.com/security/2013/10/a-relatively-easy-to-understand-primer-on-elliptic-curve-cryptography/

# How digital signature works
http://www.developer.com/java/ent/article.php/3092771/How-Digital-Signatures-Work-Digitally-Signing-Messages.htm

# How OP_CHECKSIG works
https://en.bitcoin.it/wiki/OP_CHECKSIG

# Data Encoding
## Signature : ASN.1/DER
Signatures are encoded using ASN.1/DER inside the Bitcoin protocol(source : BitcoinJ)

https://en.wikipedia.org/wiki/Abstract_Syntax_Notation_One

Microsoft's documentation on ASN.1/DER encoding for a product they develop.

https://msdn.microsoft.com/en-us/library/windows/desktop/bb648640(v=vs.85).aspx

### Implementation : Apache Harmony
https://harmony.apache.org/subcomponents/classlibrary/asn1_framework.html

### Implementation : Spongy Castle
Package :
```
import org.spongycastle.asn1.*;
```
Code examples :

http://www.programcreek.com/java-api-examples/index.php?api=org.spongycastle.asn1.DERApplicationSpecific


# OP_CHECKSIG (BitcoinJ)
## ECDSA
### ECKey 
Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.

Location : core/src/main/java/org/bitcoinj/core

```
public class ECKey implements EncryptableItem {
    public ECDSASignature sign(Sha256Hash input) throws KeyCrypterException {
        ...
    }
    
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ...
    }
}

```

### Elliptic curve
Bitcoin uses the secp256k1 elliptic curve.
```
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.asn1.x9.X9ECParameters;

X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
        CURVE_PARAMS.getH());
```

### Signature : ECKey.ECDSASignature
ECDSA signature encoding and decoding.

```
public static class ECDSASignature {
    ...
    public byte[] encodeToDER() { ... }

    public static ECDSASignature decodeFromDER(byte[] bytes) { ... }
}
```

### Signature format validation
```
    public static boolean isEncodingCanonical(byte[] signature) {
        // See reference client's IsCanonicalSignature, https://bitcointalk.org/index.php?topic=8392.msg127623#msg127623
        // A canonical signature exists of: <30> <total len> <02> <len R> <R> <02> <len S> <S> <hashtype>
        // Where R and S are not negative (their first byte has its highest bit not set), and not
        // excessively padded (do not start with a 0 byte, unless an otherwise negative number follows,
        // in which case a single 0 byte is necessary and even required).
        if (signature.length < 9 || signature.length > 73)
            return false;

        int hashType = signature[signature.length-1] & ~Transaction.SIGHASH_ANYONECANPAY_VALUE;
        if (hashType < (Transaction.SigHash.ALL.ordinal() + 1) || hashType > (Transaction.SigHash.SINGLE.ordinal() + 1))
            return false;

        //                   "wrong type"                  "wrong length marker"
        if ((signature[0] & 0xff) != 0x30 || (signature[1] & 0xff) != signature.length-3)
            return false;

        int lenR = signature[3] & 0xff;
        if (5 + lenR >= signature.length || lenR == 0)
            return false;
        int lenS = signature[5+lenR] & 0xff;
        if (lenR + lenS + 7 != signature.length || lenS == 0)
            return false;

        //    R value type mismatch          R value negative
        if (signature[4-2] != 0x02 || (signature[4] & 0x80) == 0x80)
            return false;
        if (lenR > 1 && signature[4] == 0x00 && (signature[4+1] & 0x80) != 0x80)
            return false; // R value excessively padded

        //       S value type mismatch                    S value negative
        if (signature[6 + lenR - 2] != 0x02 || (signature[6 + lenR] & 0x80) == 0x80)
            return false;
        if (lenS > 1 && signature[6 + lenR] == 0x00 && (signature[6 + lenR + 1] & 0x80) != 0x80)
            return false; // S value excessively padded

        return true;
    }
```
## Script execution
### Script.executeCheckSig

```
    private static void executeCheckSig(Transaction txContainingThis, int index, Script script, LinkedList<byte[]> stack,
                                        int lastCodeSepLocation, int opcode, 
                                        Set<VerifyFlag> verifyFlags) throws ScriptException {
        final boolean requireCanonical = verifyFlags.contains(VerifyFlag.STRICTENC)
            || verifyFlags.contains(VerifyFlag.DERSIG)
            || verifyFlags.contains(VerifyFlag.LOW_S);
        if (stack.size() < 2)
            throw new ScriptException("Attempted OP_CHECKSIG(VERIFY) on a stack with size < 2");
        byte[] pubKey = stack.pollLast();
        byte[] sigBytes = stack.pollLast();

        byte[] prog = script.getProgram();
        byte[] connectedScript = Arrays.copyOfRange(prog, lastCodeSepLocation, prog.length);

        UnsafeByteArrayOutputStream outStream = new UnsafeByteArrayOutputStream(sigBytes.length + 1);
        try {
            writeBytes(outStream, sigBytes);
        } catch (IOException e) {
            throw new RuntimeException(e); // Cannot happen
        }
        connectedScript = removeAllInstancesOf(connectedScript, outStream.toByteArray());

        // TODO: Use int for indexes everywhere, we can't have that many inputs/outputs
        boolean sigValid = false;
        try {
            // TODO: Should pass through LOW_S verification flag
            TransactionSignature sig  = TransactionSignature.decodeFromBitcoin(sigBytes, requireCanonical);

            // TODO: Should check hash type is known
            Sha256Hash hash = txContainingThis.hashForSignature(index, connectedScript, (byte) sig.sighashFlags);
            sigValid = ECKey.verify(hash.getBytes(), sig, pubKey);
        } catch (Exception e1) {
            // There is (at least) one exception that could be hit here (EOFException, if the sig is too short)
            // Because I can't verify there aren't more, we use a very generic Exception catch

            // This RuntimeException occurs when signing as we run partial/invalid scripts to see if they need more
            // signing work to be done inside LocalTransactionSigner.signInputs.
            if (!e1.getMessage().contains("Reached past end of ASN.1 stream"))
                log.warn("Signature checking failed!", e1);
        }

        if (opcode == OP_CHECKSIG)
            stack.add(sigValid ? new byte[] {1} : new byte[] {});
        else if (opcode == OP_CHECKSIGVERIFY)
            if (!sigValid)
                throw new ScriptException("Script failed OP_CHECKSIGVERIFY");
    }
```

### Script.executeMultiSig
```
    private static int executeMultiSig(Transaction txContainingThis, int index, Script script, LinkedList<byte[]> stack,
                                       int opCount, int lastCodeSepLocation, int opcode, 
                                       Set<VerifyFlag> verifyFlags) throws ScriptException {
        final boolean requireCanonical = verifyFlags.contains(VerifyFlag.STRICTENC)
            || verifyFlags.contains(VerifyFlag.DERSIG)
            || verifyFlags.contains(VerifyFlag.LOW_S);
        if (stack.size() < 2)
            throw new ScriptException("Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < 2");
        int pubKeyCount = castToBigInteger(stack.pollLast()).intValue();
        if (pubKeyCount < 0 || pubKeyCount > 20)
            throw new ScriptException("OP_CHECKMULTISIG(VERIFY) with pubkey count out of range");
        opCount += pubKeyCount;
        if (opCount > 201)
            throw new ScriptException("Total op count > 201 during OP_CHECKMULTISIG(VERIFY)");
        if (stack.size() < pubKeyCount + 1)
            throw new ScriptException("Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < num_of_pubkeys + 2");

        LinkedList<byte[]> pubkeys = new LinkedList<byte[]>();
        for (int i = 0; i < pubKeyCount; i++) {
            byte[] pubKey = stack.pollLast();
            pubkeys.add(pubKey);
        }

        int sigCount = castToBigInteger(stack.pollLast()).intValue();
        if (sigCount < 0 || sigCount > pubKeyCount)
            throw new ScriptException("OP_CHECKMULTISIG(VERIFY) with sig count out of range");
        if (stack.size() < sigCount + 1)
            throw new ScriptException("Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < num_of_pubkeys + num_of_signatures + 3");

        LinkedList<byte[]> sigs = new LinkedList<byte[]>();
        for (int i = 0; i < sigCount; i++) {
            byte[] sig = stack.pollLast();
            sigs.add(sig);
        }

        byte[] prog = script.getProgram();
        byte[] connectedScript = Arrays.copyOfRange(prog, lastCodeSepLocation, prog.length);

        for (byte[] sig : sigs) {
            UnsafeByteArrayOutputStream outStream = new UnsafeByteArrayOutputStream(sig.length + 1);
            try {
                writeBytes(outStream, sig);
            } catch (IOException e) {
                throw new RuntimeException(e); // Cannot happen
            }
            connectedScript = removeAllInstancesOf(connectedScript, outStream.toByteArray());
        }

        boolean valid = true;
        while (sigs.size() > 0) {
            byte[] pubKey = pubkeys.pollFirst();
            // We could reasonably move this out of the loop, but because signature verification is significantly
            // more expensive than hashing, its not a big deal.
            try {
                TransactionSignature sig = TransactionSignature.decodeFromBitcoin(sigs.getFirst(), requireCanonical);
                Sha256Hash hash = txContainingThis.hashForSignature(index, connectedScript, (byte) sig.sighashFlags);
                if (ECKey.verify(hash.getBytes(), sig, pubKey))
                    sigs.pollFirst();
            } catch (Exception e) {
                // There is (at least) one exception that could be hit here (EOFException, if the sig is too short)
                // Because I can't verify there aren't more, we use a very generic Exception catch
            }

            if (sigs.size() > pubkeys.size()) {
                valid = false;
                break;
            }
        }

        // We uselessly remove a stack object to emulate a reference client bug.
        byte[] nullDummy = stack.pollLast();
        if (verifyFlags.contains(VerifyFlag.NULLDUMMY) && nullDummy.length > 0)
            throw new ScriptException("OP_CHECKMULTISIG(VERIFY) with non-null nulldummy: " + Arrays.toString(nullDummy));

        if (opcode == OP_CHECKMULTISIG) {
            stack.add(valid ? new byte[] {1} : new byte[] {});
        } else if (opcode == OP_CHECKMULTISIGVERIFY) {
            if (!valid)
                throw new ScriptException("Script failed OP_CHECKMULTISIGVERIFY");
        }
        return opCount;
    }
```

## Script.removeAllInstancesOf 
Remove ECDSA signature bytes from the given input script. The reference implementation runs the same logic.
```
    public static byte[] removeAllInstancesOf(byte[] inputScript, byte[] chunkToRemove) {
        // We usually don't end up removing anything
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(inputScript.length);

        int cursor = 0;
        while (cursor < inputScript.length) {
            boolean skip = equalsRange(inputScript, cursor, chunkToRemove);
            
            int opcode = inputScript[cursor++] & 0xFF;
            int additionalBytes = 0;
            if (opcode >= 0 && opcode < OP_PUSHDATA1) {
                additionalBytes = opcode;
            } else if (opcode == OP_PUSHDATA1) {
                additionalBytes = (0xFF & inputScript[cursor]) + 1;
            } else if (opcode == OP_PUSHDATA2) {
                additionalBytes = ((0xFF & inputScript[cursor]) |
                                  ((0xFF & inputScript[cursor+1]) << 8)) + 2;
            } else if (opcode == OP_PUSHDATA4) {
                additionalBytes = ((0xFF & inputScript[cursor]) |
                                  ((0xFF & inputScript[cursor+1]) << 8) |
                                  ((0xFF & inputScript[cursor+1]) << 16) |
                                  ((0xFF & inputScript[cursor+1]) << 24)) + 4;
            }
            if (!skip) {
                try {
                    bos.write(opcode);
                    bos.write(Arrays.copyOfRange(inputScript, cursor, cursor + additionalBytes));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            cursor += additionalBytes;
        }
        return bos.toByteArray();
    }

```


# OP_CHECKSIG (Bitcoin Core)

## Check signature encoding (interpreter.cpp)
```
bool CheckSignatureEncoding(const vector<unsigned char> &vchSig, unsigned int flags, ScriptError* serror) {
    // Empty signature. Not strictly DER encoded, but allowed to provide a
    // compact way to provide an invalid signature for use with CHECK(MULTI)SIG
    if (vchSig.size() == 0) {
        return true;
    }
    if ((flags & (SCRIPT_VERIFY_DERSIG | SCRIPT_VERIFY_LOW_S | SCRIPT_VERIFY_STRICTENC)) != 0 && !IsValidSignatureEncoding(vchSig)) {
        return set_error(serror, SCRIPT_ERR_SIG_DER);
    } else if ((flags & SCRIPT_VERIFY_LOW_S) != 0 && !IsLowDERSignature(vchSig, serror)) {
        // serror is set
        return false;
    } else if ((flags & SCRIPT_VERIFY_STRICTENC) != 0 && !IsDefinedHashtypeSignature(vchSig)) {
        return set_error(serror, SCRIPT_ERR_SIG_HASHTYPE);
    }
    return true;
}
```

## Check publicic key encoding (interpreter.cpp)
```
bool static IsCompressedOrUncompressedPubKey(const valtype &vchPubKey) {
    if (vchPubKey.size() < 33) {
        //  Non-canonical public key: too short
        return false;
    }
    if (vchPubKey[0] == 0x04) {
        if (vchPubKey.size() != 65) {
            //  Non-canonical public key: invalid length for uncompressed key
            return false;
        }
    } else if (vchPubKey[0] == 0x02 || vchPubKey[0] == 0x03) {
        if (vchPubKey.size() != 33) {
            //  Non-canonical public key: invalid length for compressed key
            return false;
        }
    } else {
          //  Non-canonical public key: neither compressed nor uncompressed
          return false;
    }
    return true;
}
```

## Signature format check (interpreter.cpp)

This is neccesary to avoid Little Bobby Tables attack.
https://bitcointalk.org/index.php?topic=8392.msg127623#msg127623

Little Bobby Tables attack : 
http://xkcd.com/327/

```
/**
 * A canonical signature exists of: <30> <total len> <02> <len R> <R> <02> <len S> <S> <hashtype>
 * Where R and S are not negative (their first byte has its highest bit not set), and not
 * excessively padded (do not start with a 0 byte, unless an otherwise negative number follows,
 * in which case a single 0 byte is necessary and even required).
 * 
 * See https://bitcointalk.org/index.php?topic=8392.msg127623#msg127623
 *
 * This function is consensus-critical since BIP66.
 */
bool static IsValidSignatureEncoding(const std::vector<unsigned char> &sig) {
    // Format: 0x30 [total-length] 0x02 [R-length] [R] 0x02 [S-length] [S] [sighash]
    // * total-length: 1-byte length descriptor of everything that follows,
    //   excluding the sighash byte.
    // * R-length: 1-byte length descriptor of the R value that follows.
    // * R: arbitrary-length big-endian encoded R value. It must use the shortest
    //   possible encoding for a positive integers (which means no null bytes at
    //   the start, except a single one when the next byte has its highest bit set).
    // * S-length: 1-byte length descriptor of the S value that follows.
    // * S: arbitrary-length big-endian encoded S value. The same rules apply.
    // * sighash: 1-byte value indicating what data is hashed (not part of the DER
    //   signature)

    // Minimum and maximum size constraints.
    if (sig.size() < 9) return false;
    if (sig.size() > 73) return false;

    // A signature is of type 0x30 (compound).
    if (sig[0] != 0x30) return false;

    // Make sure the length covers the entire signature.
    if (sig[1] != sig.size() - 3) return false;

    // Extract the length of the R element.
    unsigned int lenR = sig[3];

    // Make sure the length of the S element is still inside the signature.
    if (5 + lenR >= sig.size()) return false;

    // Extract the length of the S element.
    unsigned int lenS = sig[5 + lenR];

    // Verify that the length of the signature matches the sum of the length
    // of the elements.
    if ((size_t)(lenR + lenS + 7) != sig.size()) return false;
 
    // Check whether the R element is an integer.
    if (sig[2] != 0x02) return false;

    // Zero-length integers are not allowed for R.
    if (lenR == 0) return false;

    // Negative numbers are not allowed for R.
    if (sig[4] & 0x80) return false;

    // Null bytes at the start of R are not allowed, unless R would
    // otherwise be interpreted as a negative number.
    if (lenR > 1 && (sig[4] == 0x00) && !(sig[5] & 0x80)) return false;

    // Check whether the S element is an integer.
    if (sig[lenR + 4] != 0x02) return false;

    // Zero-length integers are not allowed for S.
    if (lenS == 0) return false;

    // Negative numbers are not allowed for S.
    if (sig[lenR + 6] & 0x80) return false;

    // Null bytes at the start of S are not allowed, unless S would otherwise be
    // interpreted as a negative number.
    if (lenS > 1 && (sig[lenR + 6] == 0x00) && !(sig[lenR + 7] & 0x80)) return false;

    return true;
}

```

## Check signature (interpreter.cpp)
```
bool TransactionSignatureChecker::CheckSig(const vector<unsigned char>& vchSigIn, const vector<unsigned char>& vchPubKey, const CScript& scriptCode) const
{
    CPubKey pubkey(vchPubKey);
    if (!pubkey.IsValid())
        return false;

    // Hash type is one byte tacked on to the end of the signature
    vector<unsigned char> vchSig(vchSigIn);
    if (vchSig.empty())
        return false;
    int nHashType = vchSig.back();
    vchSig.pop_back();

    uint256 sighash = SignatureHash(scriptCode, *txTo, nIn, nHashType);

    if (!VerifySignature(vchSig, pubkey, sighash))
        return false;

    return true;
}

```

# OP_CHECKMUILTISIG (Bitcoin Core)

OP_CHECKMUILTISIG is basically looping for each (signature, public key) pair and calling 
common functions used by OP_CHECKSIG, so this should not be hard to implement. 

# Test Cases
## P2PKH
Following test cases are from actual transaction data dumped using DumpChain util.
To run it, you can run the following script.
```
dump-txs.sh
```

We will use the second transaction of the following block. The timestamp of the block, 1444341590 indicates it was created at 2015-10-09 06:59:50.
```
bh:BlockHeader(version:3, BlockHash(size:32, 25 77 2f e4 8a 91 1e 64 d2 9f ae 08 1b 8c 6e d2 c5 f9 d1 a1 70 dd 62 04 00 00 00 00 00 00 00 00), MerkleRootHash(size:32, a8 8e 9d a9 fe 0c a2 82 86 3e b1 b4 45 7a c3 5e cc c0 3a 25 0a 4a d1 db 6e 19 7b 69 9e 8e 21 df), Timestamp(1444341590), target:403838066, nonce:-109641929)
tx:Transaction(version:1, [GenerationTransactionInput(TransactionHash(size:32, 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00), outputIndex:-1, CoinbaseData(size:84, 03 c1 c4 05 09 42 69 74 4d 69 6e 74 65 72 06 2f 50 32 53 48 2f 2c fa be 6d 6d 21 45 44 36 d0 54 a1 67 15 dd e6 cb bf 74 ac 41 4a 22 1b 9c c1 9a 6a b8 b4 86 c5 da 67 7c 14 16 01 00 00 00 00 00 00 00 09 75 73 32 3f fa 00 00 00 05 00 69 44 27 1d 3f 02 00), sequenceNumber:-1)], [TransactionOutput(value : 2518427057, LockingScript(size:25, ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes{5c0e4a6830ff6ea9aea773d75bc207299cd50b74}),OpEqualVerify(),OpCheckSig([B@c6f9afb))))], lockTime:0)
tx:Transaction(version:1, [NormalTransactionInput(TransactionHash(size:32, 50 8f 85 39 cb a4 38 4e 9a de 1e 3b e8 b0 86 72 40 06 b3 b7 87 5a 7d 09 08 d3 0d 2d 63 14 1f 6b), outputIndex:0, UnlockingScript(size:140, ScriptOpList(ops:OpPush(73,ScriptBytes{3046022100eaf0618d2c164529b60c113f8bafc4fd6584f6350666a66a43bfb81263674bc10221009ba634faa2a0a9e4367dd23fa6efabfbd45952b105befecaf6454b766812d49401}),OpPush(65,ScriptBytes{040818a9ac5b0b3d793ecccac6e8f03ef3f6753cea4d9e463fff2289f2041cdb8ac604e8fd8033ef29bea21870f2ee407cdd45cc7cc3e093ddb0a7d26662846a14}))), sequenceNumber:-1)], [TransactionOutput(value : 33761500000, LockingScript(size:25, ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes{7f2151902b227372c6ec5acfad76e4b0b1e3ba45}),OpEqualVerify(),OpCheckSig([B@1935fb8c)))),TransactionOutput(value : 16182000000, LockingScript(size:25, ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes{d9eb0011f29a1c40baef4c5b6497f8fe32d79dbe}),OpEqualVerify(),OpCheckSig([B@78a07aa4))))], lockTime:0)
```

The transaction input of the 2nd transaction of the block is as follows.

* Transaction hash : 50 8f 85 39 cb a4 38 4e 9a de 1e 3b e8 b0 86 72 40 06 b3 b7 87 5a 7d 09 08 d3 0d 2d 63 14 1f 6b
* Output index : 0

The unlocking script of the given transaction input is as follows.
```
# This should be the signature.
OpPush(73,ScriptBytes{3046022100eaf0618d2c164529b60c113f8bafc4fd6584f6350666a66a43bfb81263674bc10221009ba634faa2a0a9e4367dd23fa6efabfbd45952b105befecaf6454b766812d49401})
# This should be a compressed public key.
OpPush(65,ScriptBytes{040818a9ac5b0b3d793ecccac6e8f03ef3f6753cea4d9e463fff2289f2041cdb8ac604e8fd8033ef29bea21870f2ee407cdd45cc7cc3e093ddb0a7d26662846a14})
```