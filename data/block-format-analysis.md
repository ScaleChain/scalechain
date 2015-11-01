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
                            
0000090 69 6d 65 73 20 30 33 2f 4a 61 6e 2f 32 30 30 39
00000a0 20 43 68 61 6e 63 65 6c 6c 6f 72 20 6f 6e 20 62
00000b0 72 69 6e 6b 20 6f 66 20 73 65 63 6f 6e 64 20 62
00000c0 61 69 6c 6f 75 74 20 66 6f 72 20 62 61 6e 6b 73
00000d0 ff ff ff ff 01 00 f2 05 2a 01 00 00 00 43 41 04
00000e0 67 8a fd b0 fe 55 48 27 19 67 f1 a6 71 30 b7 10
00000f0 5c d6 a8 28 e0 39 09 a6 79 62 e0 ea 1f 61 de b6
0000100 49 f6 bc 3f 4c ef 38 c4 f3 55 04 e5 1e c1 12 de
0000110 5c 38 4d f7 ba 0b 8d 57 8a 4c 70 2b 6b f1 1d 5f
0000120 ac 00 00 00 00 f9 be b4 d9 d7 00 00 00 01 00 00
0000130 00 6f e2 8c 0a b6 f1 b3 72 c1 a6 a2 46 ae 63 f7
0000140 4f 93 1e 83 65 e1 5a 08 9c 68 d6 19 00 00 00 00
0000150 00 98 20 51 fd 1e 4b a7 44 bb be 68 0e 1f ee 14
0000160 67 7b a1 a3 c3 54 0b f7 b1 cd b6 06 e8 57 23 3e
0000170 0e 61 bc 66 49 ff ff 00 1d 01 e3 62 99 01 01 00
0000180 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00
0000190 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
00001a0 00 00 00 ff ff ff ff 07 04 ff ff 00 1d 01 04 ff
00001b0 ff ff ff 01 00 f2 05 2a 01 00 00 00 43 41 04 96
00001c0 b5 38 e8 53 51 9c 72 6a 2c 91 e6 1e c1 16 00 ae
00001d0 13 90 81 3a 62 7c 66 fb 8b e7 94 7b e6 3c 52 da
00001e0 75 89 37 95 15 d4 e0 a6 04 f8 14 17 81 e6 22 94
00001f0 72 11 66 bf 62 1e 73 a8 2c bf 23 42 c8 58 ee ac
0000200 00 00 00 00 f9 be b4 d9 d7 00 00 00 01 00 00 00
0000210 48 60 eb 18 bf 1b 16 20 e3 7e 94 90 fc 8a 42 75
0000220 14 41 6f d7 51 59 ab 86 68 8e 9a 83 00 00 00 00
0000230 d5 fd cc 54 1e 25 de 1c 7a 5a dd ed f2 48 58 b8
0000240 bb 66 5c 9f 36 ef 74 4e e4 2c 31 60 22 c9 0f 9b
0000250 b0 bc 66 49 ff ff 00 1d 08 d2 bd 61 01 01 00 00
0000260 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00
0000270 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
0000280 00 00 ff ff ff ff 07 04 ff ff 00 1d 01 0b ff ff
0000290 ff ff 01 00 f2 05 2a 01 00 00 00 43 41 04 72 11
00002a0 a8 24 f5 5b 50 52 28 e4 c3 d5 19 4c 1f cf aa 15
00002b0 a4 56 ab df 37 f9 b9 d9 7a 40 40 af c0 73 de e6
00002c0 c8 90 64 98 4f 03 38 52 37 d9 21 67 c1 3e 23 64
00002d0 46 b4 17 ab 79 a0 fc ae 41 2a e3 31 6b 77 ac 00
00002e0 00 00 00 f9 be b4 d9 d7 00 00 00 01 00 00 00 bd
00002f0 dd 99 cc fd a3 9d a1 b1 08 ce 1a 5d 70 03 8d 0a
0000300 96 7b ac b6 8b 6b 63 06 5f 62 6a 00 00 00 00 44
0000310 f6 72 22 60 90 d8 5d b9 a9 f2 fb fe 5f 0f 96 09
0000320 b3 87 af 7b e5 b7 fb b7 a1 76 7c 83 1c 9e 99 5d
0000330 be 66 49 ff ff 00 1d 05 e0 ed 6d 01 01 00 00 00
0000340 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
0000350 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
0000360 00 ff ff ff ff 07 04 ff ff 00 1d 01 0e ff ff ff
0000370 ff 01 00 f2 05 2a 01 00 00 00 43 41 04 94 b9 d3
0000380 e7 6c 5b 16 29 ec f9 7f ff 95 d7 a4 bb da c8 7c
0000390 c2 60 99 ad a2 80 66 c6 ff 1e b9 19 12 23 cd 89
00003a0 71 94 a0 8d 0c 27 26 c5 74 7f 1d b4 9e 8c f9 0e
00003b0 75 dc 3e 35 50 ae 9b 30 08 6f 3c d5 aa ac 00 00
00003c0 00 00 f9 be b4 d9 d7 00 00 00 01 00 00 00 49 44
00003d0 46 95 62 ae 1c 2c 74 d9 a5 35 e0 0b 6f 3e 40 ff
00003e0 ba d4 f2 fd a3 89 55 01 b5 82 00 00 00 00 7a 06
00003f0 ea 98 cd 40 ba 2e 32 88 26 2b 28 63 8c ec 53 37
0000400 c1 45 6a af 5e ed c8 e9 e5 a2 0f 06 2b df 8c c1
0000410 66 49 ff ff 00 1d 2b fe e0 a9 01 01 00 00 00 01
0000420 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
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

