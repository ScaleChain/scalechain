# Introduction
Compress Blockchain supporting minimal cost of decompression. 
Ex> Use VarInt, replace redundant data such as transaction id to a shorter one.

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

## Blockchain data analysis
### Redundant data
- NormalTransactionInput.transactionHash( can come multiple times if a transaction has multiple outputs. Each of the output is spent, we have the same tranasction hash ) 

### Unnecessary data
- GenerationTransactionInput.transactionHash( all bits are zero )
- GenerationTransactionInput.outputIndex( all bits are one )
- GenerationTransactionInput.sequenceNumber( all bits are one )
- BlockHeader.MerkleRootHash ( We can calculate the hash by using list of transactions in the block. )

### Data that can be compressed by writing in columnar layout.
- BlockHeader.version
- BlockHeader.timestamp (delta encoding; timestamp monotonously increases.)
- BlockHeader.target (delta encoding; difficulty changes for every 2016 blocks)
- Transaction.locktime ( in most cases this value is 0 )

### Data that can not be compressed any more.
- BlockHeader.prevBlockHash ( need it as an index key to look up a block by block hash )
- BlockHeader.nonce

### Need investigation
- Locking script, unlocking script.

# Plan
## Replace redudant data with a shorter symbols.
There are some redundant data in Bitcoin blockchain. 

## Write block data in columnar layout.
Currently, blockchain data is written in row wise layout. 
Ex> A block comes and another block comes. A transaction comes, and then another transaction comes.

I am thinking about writing data in columnar layout to compress data. 

For example, timestamps of all blocks will be written continuously. We don't need to write full 4 byte timestamp.
But we can write the delta value(the seconds elapsed since the previous block timestamp). 
Because bitcoin blocks are mined approximately every 10 minutes, in most cases, we need to write an integer around 60 seconds * 10 min = 600 seconds.

Compare 600 with 1446513430, which is current timestamp. We can compress data by writing a smaller value.

# FAQ
## Does this replace the blockchain implementation by Bitcoin reference client?
No, this is an experimental project to give light to Bitcoin developers.


