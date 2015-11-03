# Introduction
Compress Blockchain data using columnar layout with delta encoding and bitmaps to minimize the cost of decompression. We can use VarInt also like Bitcoin reference client does.

We can replace redundant data such as transaction id to a shorter one. If a transaction has 100 outputs, we need to write the same 32 byte transaction hash of the transaction, whenever we want to spend each of the outputs. We can try replacing the long transcation hash to a shorter string maintaining a map from the short string to the actual transaction hash value.

# Goal
## Blockchain compression
Compress blockchain data to 50% of current size.
Hopefully to 25%.

## Increase of block size
Increase block size, because we compressed the on-disk blockchain files.
If we can decrease the blockchain size to 50%, we can increase the block size from 1M to 2MB.
If we can decrease the blockchain size to 25%, we can increase the block size from 1M to 4MB.



# Investigations
The genesis block looks like as follows. Let's find out which item can be compressed by writing programs.
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
          CoinbaseData(size:77, 04 ff ff 00 1d 01 04 45 54 68 65 20 54 69 6d 65 73 20 30 33 2f 4a 61 6e 2f 32 30 30 39 20 43 68 61 6e 63 65 6c 6c 6f 72 20 6f 6e 20 62 72 69 6e 6b 20 6f 66 20 73 65 63 6f 6e 64 20 62 61 69 6c 6f 75 74 20 66 6f 72 20 62 61 6e 6b 73), sequenceNumber:-1)], [TransactionOutput(value : 5000000000, LockingScript(size:67, 41 04 67 8a fd b0 fe 55 48 27 19 67 f1 a6 71 30 b7 10 5c d6 a8 28 e0 39 09 a6 79 62 e0 ea 1f 61 de b6 49 f6 bc 3f 4c ef 38 c4 f3 55 04 e5 1e c1 12 de 5c 38 4d f7 ba 0b 8d 57 8a 4c 70 2b 6b f1 1d 5f ac),
          sequenceNumber:-1
        )
      ], 
      [
        TransactionOutput(
          value : 5000000000, 
          LockingScript(size:67, 41 04 67 8a fd b0 fe 55 48 27 19 67 f1 a6 71 30 b7 10 5c d6 a8 28 e0 39 09 a6 79 62 e0 ea 1f 61 de b6 49 f6 bc 3f 4c ef 38 c4 f3 55 04 e5 1e c1 12 de 5c 38 4d f7 ba 0b 8d 57 8a 4c 70 2b 6b f1 1d 5f ac)
        )
      ]      
      lockTime:0
    )
  ]
)

```
## Current blockchain size
With a full node running at Nov 1 2015, the size is as follows.
### blocks/blkNNNNN.dat : 47,791,336
### blocks/revNNNNN.dat :  6,376,748
### blocks/index        :     60,516
### chainstate/         :  1,172,796

## Compression using tar cvfz
Tried to compress 10 files from blk00350.dat to blk00359.dat.
### Original size   : 1,306,716,000
### Compressed size : 1,055,568,201
                       
## Blockchain data analysis
### Redundant data
- NormalTransactionInput.transactionHash( can come multiple times if a transaction has multiple outputs. Each of the output is spent, we have the same tranasction hash ) 

### Unnecessary data
- GenerationTransactionInput.transactionHash( all bits are zero )
- GenerationTransactionInput.outputIndex( all bits are one )
- GenerationTransactionInput.sequenceNumber( all bits are one )
- BlockHeader.MerkleRootHash ( We can calculate the hash by using list of transactions in the block. )

### Data that can be compressed by writing in columnar layout.
- BlockHeader.version ( delta encoding; Does not change for a long time with lots of blocks. )
- BlockHeader.timestamp (delta encoding; timestamp monotonously increases. )
- BlockHeader.target (delta encoding; difficulty changes for every 2016 blocks. )
- Transaction.locktime ( in most cases this value is 0. )

### Data that can not be compressed any more.
These are quite random. Might be hard to compress anymore.
- BlockHeader.prevBlockHash ( need it as an index key to look up a block by block hash )
- BlockHeader.nonce

### Data that can be compressed using cryptography
We can think about writing only X value of public key and a flag indicating if Y value is above the X axis or not. 
On disk, we can write the compact format, and we will have to reconstruct the full public key in memory whenever it is necessary. Ex> To calculate script hash. A public key is in unlocking scripts of P2PKH. Also multiple public keys can be in a redeem script of P2SH.

From Andreas's Mastering Bitcoin:
```
Compressed public keys

Compressed public keys were introduced to bitcoin to reduce the size of transactions and conserve disk space on nodes that store the bitcoin blockchain database. Most transactions include the public key, required to validate the owner’s credentials and spend the bitcoin. Each public key requires 520 bits (prefix \+ x \+ y), which when multiplied by several hundred transactions per block, or tens of thousands of transactions per day, adds a significant amount of data to the blockchain.

As we saw in the section Public Keys, a public key is a point (x,y) on an elliptic curve. Because the curve expresses a mathematical function, a point on the curve represents a solution to the equation and, therefore, if we know the x coordinate we can calculate the y coordinate by solving the equation y2 mod p = (x3 + 7) mod p. That allows us to store only the x coordinate of the public key point, omitting the y coordinate and reducing the size of the key and the space required to store it by 256 bits. An almost 50% reduction in size in every transaction adds up to a lot of data saved over time!
```

### Extract common data patterns
- LockingScript, UnlockingScript ( these have similar patterns. Most of them are P2PKH. Some of them are P2SH )

# Extract Script Patterns
I believe that most bitcoin locking scripts are P2SH or P2PKH. Obviously, these transactions have patterns.
We can think about creating templates for P2SH, P2PKH, and any other common type of transactions.
Then, what we have to write for each transaction is the part that is different from the P2SH or P2PKH Script template.

Following examples (from Andreas's Mastering Bitcoin book) shows patterns on P2SH and P2PKH scripts.
For these examples, only values in enclosed with < and > are changing. 
But these values are quite long, so extracting patterns might not be very helpful for compressing blockchain data.
## P2PKH patterns
### Locking Script
```
DUP
HASH160
<hash of public key of bitcoin address>
EQUALVERIFY
CHECKSIG
```
### Unlocking Script
```
<signiture> 
<public key of bitcoin address>
```

## P2SH patterns
### Redeem Script
```
<2> <PubKey1> <PubKey2> <PubKey3> <PubKey4> <PubKey5> <5> OP_CHECKMULTISIG
```
### Locking Script
```
OP_HASH160 <20-byte hash of redeem script> OP_EQUAL
```
### Unlocking Script
```
<Sig1> <Sig2> <redeem script>
```

# Plans
## Replace redundant data with a shorter symbols.
There are redundant data in Bitcoin blockchain such as transaction hash. Find out how many redundant data we have today, and see if there is any feasible solution to reduce blockchain size. 

## Write block data in columnar layout.
Currently, blockchain data is written in row wise layout. 
Ex> A block come and another block comes on a block file. A transaction comes, and then another transaction comes in a block.

I am thinking about writing data in columnar layout to compress data. 

For example, timestamps of all blocks will be written continuously. We don't need to write full 4 byte timestamp.
But we can write the delta value(the seconds elapsed since the previous block timestamp). 
Because bitcoin blocks are mined approximately every 10 minutes, in most cases, we need to write an integer around 60 seconds * 10 min = 600 seconds.

Compare 600 with 1446513430, which is current timestamp. We can compress data by writing a smaller value.

# References
## C-Store : A Column-oriented DBMS 
http://db.csail.mit.edu/projects/cstore/vldb.pdf

## monetdb : An open source columnar database engine.
https://www.monetdb.org/Home

# FAQ
## Does this replace the blockchain implementation by Bitcoin reference client?
No, this is an experimental project to find out an optimal storage size using columnar layout for blockchain storage.

# Live Coding Videos
## List of videos
1. Writing and describing this document
2. Debugging the block reader for the analysis of block chain.
3. Testing compression of blk00350.dat ~ blk00359.dat

https://www.livecoding.tv/video/blockchain-scalechainio-50/

And more videos. Just change the number at the end of url from 29 to 50.




