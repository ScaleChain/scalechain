
#Introduction
Let's try to analyze the wallet.dat file

#References
wallet.dat file has Berkeley DB header format: http://www.gnu.org/software/mifluz/doc/doxydoc/db__page_8h-source.html#l00078

#Files
This file is the wallet of Bitcoin blockchain. copied it from ~/Library/Application Support/Bitcoin/ after installing the Bitcoin core.

```
wallet.dat
```

#The data from the wallet file
```
00000 00 00 00 00 01 00 00 00 00 00 00 00 62 31 05 00
      |---------------------| |---------| 
               LSN              Current Page Number
                                          |---------|
                                            Magic Number
00010 09 00 00 00 00 10 00 00 00 09 00 00 00 00 00 00
      |---------| |---------|    ||
        Version     Pagesize     DB Type
        
===================================================== >> a pair of public and private keys
04570 6B 65 79 21 02 37 D6 42 75 86 7B 51 7E C9 78 8B
      |------|
         key
04580 EE A9 8A 8C 84 46 75 0D 56 43 DD 48 47 62 E0 62
04590 DF 78 72 2E D6 00 00 00 F7 00 01 D6 30 81 D3 02
045a0 01 01 04 20 D8 B7 EE 13 19 BE 14 A5 E2 B0 B8 9D
                  |----------------------------------
                            private key
045b0 5C 3B A9 D3 DF C8 20 E5 94 4A E7 30 E9 F1 87 5F
      -----------------------------------------------
045c0 A2 33 55 F9 A0 81 85 30 81 82 02 01 01 30 2C 06
      ----------|
045d0 07 2A 86 48 CE 3D 01 01 02 21 00 FF FF FF FF FF
045e0 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF
045f0 FF FF FF FF FF FF FE FF FF FC 2F 30 06 04 01 00
04600 04 01 07 04 21 02 79 BE 66 7E F9 DC BB AC 55 A0
04610 62 95 CE 87 0B 07 02 9B FC DB 2D CE 28 D9 59 F2
04620 81 5B 16 F8 17 98 02 21 00 FF FF FF FF FF FF FF
04630 FF FF FF FF FF FF FF FF FE BA AE DC E6 AF 48 A0
04640 3B BF D2 5E 8C D0 36 41 41 02 01 01 A1 24 03 22
04650 00 02 10 A8 16 7C 75 7B 3F 62 77 50 6E D0 16 EB
         |-------------------------------------------
                         public key
04660 DB CC 10 03 EB 62 F7 2B 37 8E 05 77 62 87 86 3D 
      -----------------------------------------------
04670 B2 D7 4C F6 EF 66 42 BD 5C F9 A9 7E E2 48 F6 72 
      ----|

===================================================== >> a keypool entry
07170 26 0C 72 35 01 00 00 00 0D 00 01 04 70 6F 6F 6C
                                          |---------|
                                              pool
07180 3A 00 00 00 00 00 00 00 2E 00 01 6C FB 01 00 05
07190 25 DD 56 00 00 00 00 21 02 10 A8 16 7C 75 7B 3F
                              |----------------------
                                    public key
071a0 62 77 50 6E D0 16 EB DB CC 10 03 EB 62 F7 2B 37
      -----------------------------------------------
071b0 8E 05 77 62 87 86 3D B2 D7 24 03 22 0D 00 01 04
      -------------------------|
      
===================================================== >> an address book name
15a50 59 68 66 79 06 00 01 05 74 65 73 74 32 65 73 74
                              |------------|
                                 test2(account name)
15a60 28 00 01 04 6E 61 6D 65 22 31 43 6E 54 72 4A 35
                                 |-------------------
                                      address
15a70 47 57 31 35 50 63 64 51 46 57 6B 58 6F 50 57 35
      -----------------------------------------------
15a80 57 74 76 53 31 31 64 79 36 41 44 00 05 00 01 04
      -------------------------------|
      
```
