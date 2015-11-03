# Introduction

Block file reader reads blocks from blkxxx.dat files. 

# The block format
The following wiki describes the format of a block in blkxxx.dat files.

https://en.bitcoin.it/wiki/Block

# Procedure
1. Read magic value
1. Read block size
1. Read block header
1. Read transactions
1. Read lock value

# Sequence Diagram
The BlockDirectoryReader lists each file with pattern "blk*.dat" on a given path, and then creates BlockFileReader for each of them.
BlockFileReader then fully reads all blocks in a blkNNNNN.dat file. It reads blocks one by one using BlockParser.
BlockParser knows all about the blockchain data format. It reads block size, magic value, block header, transactions, etc.
For each block produced by the parser, we call BlockReadListener's onBlock method. 
```
        ,-.                                                                                                                           
        `-'                                                                                                                           
        /|\                                                                                                                           
         |             ,--------------------.          ,---------------.          ,-----------.          ,-----------------.          
        / \            |BlockDirectoryReader|          |BlockFileReader|          |BlockParser|          |BlockReadListener|          
      Caller           `---------+----------'          `-------+-------'          `-----+-----'          `--------+--------'          
        |        readFrom        |                             |                        |                         |                   
        | ----------------------->                             |                        |                         |                   
        |                        |                             |                        |                         |                   
   ,--------------------------!. |          readFully          |                        |                         |                   
   |iterates each blkNNNNN.dat|_\| --------------------------->|                        |                         |                   
   |file in the path.           ||                             |                        |                         |                   
   `----------------------------'|                             |                        |                         |                   
        |                        |                             |         parse          |                         |                   
        |                        |                             |----------------------->|                         |                   
        |                        |                             |                        |                         |                   
        |                        |                             |                        |                         |                   
        |                        |                             |        _____________________________________________________________ 
        |                        |                             |        ! OPT  /  for each block                  |                  !
        |                        |                             |        !_____/         |                         |                  !
        |                        |                             |        !               |        onBlock          |                  !
        |                        |                             |        !               |------------------------>|                  !
        |                        |                             |        !~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~!
      Caller           ,---------+----------.          ,-------+-------.          ,-----+-----.          ,--------+--------.          
        ,-.            |BlockDirectoryReader|          |BlockFileReader|          |BlockParser|          |BlockReadListener|          
        `-'            `--------------------'          `---------------'          `-----------'          `-----------------'          
        /|\                                                                                                                           
         |                                                                                                                            
        / \                                                                                                                           
```

# The genesis block

The following is the genesis block we actually read.
```
Block
(
  size:285, 
  BlockHeader
  (
    version:1, 
    BlockHash(size:32, 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00), 
    MerkleRootHash(size:32, 3b a3 ed fd 7a 7b 12 b2 7a c7 2c 3e 67 76 8f 61 7f c8 1b c3 88 8a 51 32 3a 9f b8 aa 4b 1e 5e 4a), 
    Timestamp(1231006505), 
    target:486604799, 
    nonce:2083236893
  ), 
  [
    Transaction(
      version:1, 
      [
        GenerationTransactionInput(
          TransactionHash(size:32, 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00), 
          outputIndex:-1, 
          CoinbaseData(size:77, 04 ff ff 00 1d 01 04 45 54 68 65 20 54 69 6d 65 73 20 30 33 2f 4a 61 6e 2f 32 30 30 39 20 43 68 61 6e 63 65 6c 6c 6f 72 20 6f 6e 20 62 72 69 6e 6b 20 6f 66 20 73 65 63 6f 6e 64 20 62 61 69 6c 6f 75 74 20 66 6f 72 20 62 61 6e 6b 73), sequenceNumber:-1)], [TransactionOutput(value : 5000000000, LockingScript(size:67, 41 04 67 8a fd b0 fe 55 48 27 19 67 f1 a6 71 30 b7 10 5c d6 a8 28 e0 39 09 a6 79 62 e0 ea 1f 61 de b6 49 f6 bc 3f 4c ef 38 c4 f3 55 04 e5 1e c1 12 de 5c 38 4d f7 ba 0b 8d 57 8a 4c 70 2b 6b f1 1d 5f ac)
        )
      ], 
      lockTime:0
    )
  ]
)

```

# Code snippets
## Iterating files
We need to list all files matching the pattern blocks/blkNNNNN.dat.

```
import scala.collection.JavaConversions._
for(file <- myDirectory.listFiles if file.getName starts With "blk" && file.getName endsWith ".dat"){
   // process the file
}
```

## Read a binary file
We will use FileInputStream to read a file as a stream of bytes.
```
val bis = new BufferedInputStream(new FileInputStream(fileName))
```

## Read VarInt values
We nned to read VarInt values from disk. Ex> block size.
Use the following source code for reading VarInt values to focus on developing reading blocks.

For reading VarInt values, we can use readUnsignedVarInt(DataInput in). So we have to create a DataInput using DataInputStream(InputStream in) from the input stream for a block file. 
```
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clearspring.analytics.util;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * <p>Encodes signed and unsigned values using a common variable-length
 * scheme, found for example in
 * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
 * Google's Protocol Buffers</a>. It uses fewer bytes to encode smaller values,
 * but will use slightly more bytes to encode large values.</p>
 * <p/>
 * <p>Signed values are further encoded using so-called zig-zag encoding
 * in order to make them "compatible" with variable-length encoding.</p>
 */
public final class Varint {

    private Varint() {
    }

    /**
     * Encodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
     * Google Protocol Buffers</a>. It uses zig-zag encoding to efficiently
     * encode signed values. If values are known to be nonnegative,
     * {@link #writeUnsignedVarLong(long, DataOutput)} should be used.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws IOException if {@link DataOutput} throws {@link IOException}
     */
    public static void writeSignedVarLong(long value, DataOutput out) throws IOException {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarLong((value << 1) ^ (value >> 63), out);
    }

    /**
     * Encodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
     * Google Protocol Buffers</a>. Zig-zag is not used, so input must not be negative.
     * If values can be negative, use {@link #writeSignedVarLong(long, DataOutput)}
     * instead. This method treats negative input as like a large unsigned value.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws IOException if {@link DataOutput} throws {@link IOException}
     */
    public static void writeUnsignedVarLong(long value, DataOutput out) throws IOException {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            out.writeByte(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte((int) value & 0x7F);
    }

    /**
     * @see #writeSignedVarLong(long, DataOutput)
     */
    public static void writeSignedVarInt(int value, DataOutput out) throws IOException {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarInt((value << 1) ^ (value >> 31), out);
    }

    /**
     * @see #writeUnsignedVarLong(long, DataOutput)
     */
    public static void writeUnsignedVarInt(int value, DataOutput out) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    public static byte[] writeSignedVarInt(int value) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        return writeUnsignedVarInt((value << 1) ^ (value >> 31));
    }

    /**
     * @see #writeUnsignedVarLong(long, DataOutput)
     * <p/>
     * This one does not use streams and is much faster.
     * Makes a single object each time, and that object is a primitive array.
     */
    public static byte[] writeUnsignedVarInt(int value) {
        byte[] byteArrayList = new byte[10];
        int i = 0;
        while ((value & 0xFFFFFF80) != 0L) {
            byteArrayList[i++] = ((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        byteArrayList[i] = ((byte) (value & 0x7F));
        byte[] out = new byte[i + 1];
        for (; i >= 0; i--) {
            out[i] = byteArrayList[i];
        }
        return out;
    }

    /**
     * @param in to read bytes from
     * @return decode value
     * @throws IOException              if {@link DataInput} throws {@link IOException}
     * @throws IllegalArgumentException if variable-length value does not terminate
     *                                  after 9 bytes have been read
     * @see #writeSignedVarLong(long, DataOutput)
     */
    public static long readSignedVarLong(DataInput in) throws IOException {
        long raw = readUnsignedVarLong(in);
        // This undoes the trick in writeSignedVarLong()
        long temp = (((raw << 63) >> 63) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1L << 63));
    }

    /**
     * @param in to read bytes from
     * @return decode value
     * @throws IOException              if {@link DataInput} throws {@link IOException}
     * @throws IllegalArgumentException if variable-length value does not terminate
     *                                  after 9 bytes have been read
     * @see #writeUnsignedVarLong(long, DataOutput)
     */
    public static long readUnsignedVarLong(DataInput in) throws IOException {
        long value = 0L;
        int i = 0;
        long b;
        while (((b = in.readByte()) & 0x80L) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 63) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (b << i);
    }

    /**
     * @throws IllegalArgumentException if variable-length value does not terminate
     *                                  after 5 bytes have been read
     * @throws IOException              if {@link DataInput} throws {@link IOException}
     * @see #readSignedVarLong(DataInput)
     */
    public static int readSignedVarInt(DataInput in) throws IOException {
        int raw = readUnsignedVarInt(in);
        // This undoes the trick in writeSignedVarInt()
        int temp = (((raw << 31) >> 31) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values.
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1 << 31));
    }

    /**
     * @throws IllegalArgumentException if variable-length value does not terminate
     *                                  after 5 bytes have been read
     * @throws IOException              if {@link DataInput} throws {@link IOException}
     * @see #readUnsignedVarLong(DataInput)
     */
    public static int readUnsignedVarInt(DataInput in) throws IOException {
        int value = 0;
        int i = 0;
        int b;
        while (((b = in.readByte()) & 0x80) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (b << i);
    }

    public static int readSignedVarInt(byte[] bytes) {
        int raw = readUnsignedVarInt(bytes);
        // This undoes the trick in writeSignedVarInt()
        int temp = (((raw << 31) >> 31) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values.
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1 << 31));
    }

    public static int readUnsignedVarInt(byte[] bytes) {
        int value = 0;
        int i = 0;
        byte rb = Byte.MIN_VALUE;
        for (byte b : bytes) {
            rb = b;
            if ((b & 0x80) == 0) {
                break;
            }
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (rb << i);
    }

}
```

from : https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java

# Live Coding Videos
https://www.livecoding.tv/video/blockchain-scalechainio-7/
https://www.livecoding.tv/video/blockchain-scalechainio-8/
https://www.livecoding.tv/video/blockchain-scalechainio-9/
https://www.livecoding.tv/video/blockchain-scalechainio-10/
https://www.livecoding.tv/video/blockchain-scalechainio-11/
https://www.livecoding.tv/video/blockchain-scalechainio-12/
https://www.livecoding.tv/video/blockchain-scalechainio-15/
https://www.livecoding.tv/video/blockchain-scalechainio-16/
https://www.livecoding.tv/video/blockchain-scalechainio-17/
https://www.livecoding.tv/video/blockchain-scalechainio-19/