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

For reading VarInt values, we can use following srouce code. Varint.readUnsignedVarInt(DataInput in). So we have to create a DataInput using DataInputStream(InputStream in) from the input stream for a block file. 

https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java

# Live Coding Videos
## BlockDirectoryReader implemented, but not tested.
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

## BlockDirectoryReader tested and documented.
https://www.livecoding.tv/video/blockchain-scalechainio-20/

https://www.livecoding.tv/video/blockchain-scalechainio-21/

https://www.livecoding.tv/video/blockchain-scalechainio-22/

https://www.livecoding.tv/video/blockchain-scalechainio-23/

https://www.livecoding.tv/video/blockchain-scalechainio-24/

https://www.livecoding.tv/video/blockchain-scalechainio-25/

https://www.livecoding.tv/video/blockchain-scalechainio-26/

https://www.livecoding.tv/video/blockchain-scalechainio-27/

https://www.livecoding.tv/video/blockchain-scalechainio-28/

