# Introduction
Let's try to analyze the first block file.

# Source codes 

## Entry point of bitcoind 
```
bool AppInit(int argc, char* argv[])
```
## Initialization

Setting up signal handlers, parsing parameters, reading config files, etc.
```
AppInit2 in init.cpp
```
## Block file header?

Following wiki describes the block file header. The magic number matches the one on the block file in little endian.
https://en.bitcoin.it/wiki/Block

## VarInt
Int fields are encoded as VarInt.
CVarInt (serialize.h)

To see the format of VarInt, see WriteVarInt  

protobuf has an example on VarInt but not sure if Bitcoin uses the same format.
https://developers.google.com/protocol-buffers/docs/encoding?hl=en

# References
## The genesis block
```
$ bitcoind getblock 000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f
```

```
{
    "hash" : "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
    "confirmations" : 308321,
    "size" : 285,
    "height" : 0,
    "version" : 1,
    "merkleroot" : "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
    "tx" : [
        "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"
    ],
    "time" : 1231006505,
    "nonce" : 2083236893,
    "bits" : "1d00ffff",
    "difficulty" : 1.00000000,
    "nextblockhash" : "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048"
}
```
# Code snippets
## convert unix timestamp to date
```
import datetime
print(
    datetime.datetime.fromtimestamp(
        int("1231006505")
    ).strftime('%Y-%m-%d %H:%M:%S')
)

```

# Files
This file is the first block of Bitcoin blockchain.
copied it from ~/Library/Application Support/Bitcoin/ after installing and synching full blocks w/ the Bitcoin core reference implementation.

```
data/blk00001-128k.dat
```
# The size of genesis block
0xe35(3637)


# The data from the first block file, blk00000.dat
```
0000000 f9 be b4 d9 1d 01 00 00 01 00 00 00 00 00 00 00
        |---------|             |==> block header(80 bytes) starts from here.
        magic value(LE)                     |========== continued
                    |---------| |---------|
                    block size  Block version number
                    (285)       (1)
        
0000010 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        =============================================== continued
0000020 00 00 00 00 00 00 00 00 00 00 00 00 3b a3 ed fd
        ===================================| hashPrevBlock(32 bytes) => 0, because the genesis block does not have a previous block
                                            |==========

0000030 7a 7b 12 b2 7a c7 2c 3e 67 76 8f 61 7f c8 1b c3
        =============================================== continued
0000040 88 8a 51 32 3a 9f b8 aa 4b 1e 5e 4a 29 ab 5f 49
        ===================================| hashMerkleRoot(32 bytes) => This should be containing only the 50 BTC coinbase tx.
                                            |=========| unix timestamp (1231006505, 2009-01-04 03:15:05)

0000050 ff ff 00 1d 1d ac 2b 7c 01 01 00 00 00 01 00 00
        |---------|                |===> transaction starts from here.
         Bits, see https://en.bitcoin.it/wiki/Target. Current target in compact format
                    |---------|
                     Nonce(2083236893)
Block header until here <=====|        /-> transaction version(1, in little endian)       
                                |-||---------|
                                 \-> transaction count(VarInt, 1) => Only the 50 BTC coinbase transaction.
                                               |-| in-counter(VarInt, 1)
                                                  |==== generation transaction starts from here. Continued.
0000060 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        =============================================== continued
0000070 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff
        =========================================| The first field is Tx Hash(32 bytes). All bits are zero: Not a transaction hash reference.
                                                  |====
0000080 ff ff 4d 04 ff ff 00 1d 01 04 45 54 68 65 20 54
        ====| Output Index ( 4 bytes) . All bits are ones: 0xFFFFFFFF
              |-| coinbase data size. (4d => 77 bytes) Length of the coinbase data, from 2 to 100 bytes
                 -- -- -- -- -- -- -- -- -- -- -- -- --
                 01 02 03 04 05 06 07 08 09 10 11 12 13 => 13 bytes.
                            
0000090 69 6d 65 73 20 30 33 2f 4a 61 6e 2f 32 30 30 39
        ----------------------------------------------- line 1 ( 16 bytes for each)
00000a0 20 43 68 61 6e 63 65 6c 6c 6f 72 20 6f 6e 20 62
        ----------------------------------------------- line 2
00000b0 72 69 6e 6b 20 6f 66 20 73 65 63 6f 6e 64 20 62
        ----------------------------------------------- line 3
00000c0 61 69 6c 6f 75 74 20 66 6f 72 20 62 61 6e 6b 73
        ----------------------------------------------| line 4. until here. 16 * 4 + 13 => 77
        The data is as follows.
        ^D��<GS>^A^DEThe Times 03/Jan/2009 Chancellor on brink of second bailout for banks
        
00000d0 ff ff ff ff 01 00 f2 05 2a 01 00 00 00 43 41 04
        |---------| Sequence Number ( 4 bytes. Set to 0xFFFFFFFF )
                  |
                   \->the generation transaction ends here
                    |-| Out-counter (VarInt, 1)
                       |----------------------| value(8 bytes, 5000000000 => 5BTC) 
                                               |-| tx-out script length. (0x43 => 67) 
                                                  |==== tx-out script. (cont.)
                                               
00000e0 67 8a fd b0 fe 55 48 27 19 67 f1 a6 71 30 b7 10
        |=============================================|
00000f0 5c d6 a8 28 e0 39 09 a6 79 62 e0 ea 1f 61 de b6
        |=============================================|
0000100 49 f6 bc 3f 4c ef 38 c4 f3 55 04 e5 1e c1 12 de
        |=============================================|
0000110 5c 38 4d f7 ba 0b 8d 57 8a 4c 70 2b 6b f1 1d 5f
        |=============================================|
0000120 ac 00 00 00 00 f9 be b4 d9 d7 00 00 00 01 00 00
        ==| until here. 67 bytes of tx-out script.
           |---------| lock time. (4 bytes). 0 means no lock time.
                       |----------|
                        magic value(LE)
                       |-------------------> 2nd block starts from here.
0000130 00 6f e2 8c 0a b6 f1 b3 72 c1 a6 a2 46 ae 63 f7
0000140 4f 93 1e 83 65 e1 5a 08 9c 68 d6 19 00 00 00 00
0000150 00 98 20 51 fd 1e 4b a7 44 bb be 68 0e 1f ee 14
0000160 67 7b a1 a3 c3 54 0b f7 b1 cd b6 06 e8 57 23 3e
```

# The data from the second block file, blk00001.dat
```
0000000 f9 be b4 d9 35 0e 00 00 01 00 00 00 d8 be bc a4
        |---------|             |--> block header starts from here.
        magic value(LE)                     |========== continued
                    |---------| |---------|
                    block size  Block version number(1)
                                                                           
0000010 8e 6c 45 8a 6e c2 fc 4a dc 74 96 4b d8 6c 6a 35
        =============================================== continued
0000020 cb c3 15 88 c5 39 00 00 00 00 00 00 a2 b9 59 41
        ===================================| hashPrevBlock(32 bytes)
                                            |==========
0000030 96 b2 f1 54 10 a8 a7 16 e1 a5 b0 ce 7f 48 4e 69
        =============================================== continued
0000040 35 ce 98 74 d2 73 04 05 e5 15 ee f2 87 b5 b4 4d
        ===================================| hashMerkleRoot(32 bytes)
                                            |=========| unix timestamp (1303688583, 2011-04-25 08:43:03)
0000050 ac b5 00 1b ef 81 d2 21 0c 01 00 00 00 01 00 00
```

# Tools
## CLion
For analysing Bitcoin reference source code written in C++.
This tool has a nice feature that allows me to follow function definitions from the source code.
But it is not free, so I am going to use Eclipse CDT.

## Eclipse CDT
Let's use it!!
To go to the definition of a function, you can press cmd + left click on Mac OS. 

