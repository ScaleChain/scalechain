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


