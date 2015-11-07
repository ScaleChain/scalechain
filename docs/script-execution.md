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