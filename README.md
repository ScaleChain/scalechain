Introduction
============
- Goal: Minimize disk usage of blockchain data from 45G to half of it.
- How: Compress blockchain data, but still provide fast lookup on the blockchain.

Current status
==============
- Investigation done for the blkxxx.dat file format.
- Write a Scala program that reads the blkxxx.dat file. Write a block reader for it.

TODO
====
- Run the block reader to get statistics on blockchain data. Ex> What percentage of transaction hashes are written more than once? (to replace redundant transaction hash to a shorter one) 
- Write a prototype that writes blockchain data in columnar layout to compress data using delta encoding, bitmaps, etc.


References 
==========
- Mastering Bitcoin boot. We also copied some text from the book to write comments on methods and classes.

Video
=====
Followings are videos taken while the development of this project.


Project setup, tools setup:

https://www.livecoding.tv/video/blockchain-scalechainio-2/

https://www.livecoding.tv/video/blockchain-scalechainio-3/


Analysis of the genesis block on blk00000.dat : 

A: https://www.livecoding.tv/video/blockchain-scalechainio-5/

B: https://www.livecoding.tv/video/blockchain-scalechainio-5/

See from 58:30 of A to save your time. Nothing much before the time. 

Live stream available on this URL when I write code.

https://www.livecoding.tv/kangmo/  
