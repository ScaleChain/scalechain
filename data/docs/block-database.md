# Introduction
Analyze blockchain database.

# Coins View DB
## Coins ('c')
* key : transaction id (256 bit hash)
* value : pruned unspent coins (CCoins)
```
 * Pruned version of CTransaction: only retains metadata and unspent transaction outputs
 * Example: 0104835800816115944e077fe7c803cfa57f29b36bf87c1d358bb85e
 *          <><><--------------------------------------------><---->
 *          |  \                  |                             /
 *    version   code             vout[1]                  height
 *
 *    - version = 1
 *    - code = 4 (vout[1] is not spent, and 0 non-zero bytes of bitvector follow)
 *    - unspentness bitvector: as 0 non-zero bytes follow, it has length 0
 *    - vout[1]: 835800816115944e077fe7c803cfa57f29b36bf87c1d35
 *               * 8358: compact amount representation for 60000000000 (600 BTC)
 *               * 00: special txout type pay-to-pubkey-hash
 *               * 816115944e077fe7c803cfa57f29b36bf87c1d35: address uint160
 *    - height = 203998
```

## Best block ('B')
* Key : None ( only the prefix 'B' is used for the leveldb key )
* Value : block header hash ( 256 bit hash ) 


# Block Tree DB

## Block files ('f')
* key : file number (int)
* value : CBlockFileInfo
```
    unsigned int nBlocks;      //! number of blocks stored in file
    unsigned int nSize;        //! number of used bytes of block file
    unsigned int nUndoSize;    //! number of used bytes in the undo file
    unsigned int nHeightFirst; //! lowest height of block in file
    unsigned int nHeightLast;  //! highest height of block in file
    uint64_t nTimeFirst;         //! earliest time of block in file
    uint64_t nTimeLast;          //! latest time of block in file
```

## Transaction index ('t')
* key : transaction id (256 bit hash)
* value : the position of the transaction at the block file ( CDiskTxPos )
```
struct CDiskBlockPos {
    int nFile;
    unsigned int nPos;
    ...
};

struct CDiskTxPos : public CDiskBlockPos {
    unsigned int nTxOffset; // after header
    ...
}
```

## Block index ('b')
* key : block header hash ( 256 bit hash )
* value : block header + (optional) block data position + (optional) undo data ( DiskBlockIndex )
What is undo data?
```
class CDiskBlockIndex : public CBlockIndex {
    READWRITE(VARINT(nVersion));

    READWRITE(VARINT(nHeight));
    READWRITE(VARINT(nStatus));
    READWRITE(VARINT(nTx));
    if (nStatus & (BLOCK_HAVE_DATA | BLOCK_HAVE_UNDO))
        READWRITE(VARINT(nFile));
    if (nStatus & BLOCK_HAVE_DATA)
        READWRITE(VARINT(nDataPos));
    if (nStatus & BLOCK_HAVE_UNDO)
        READWRITE(VARINT(nUndoPos));

    // block header
    READWRITE(this->nVersion);
    READWRITE(hashPrev);
    READWRITE(hashMerkleRoot);
    READWRITE(nTime);
    READWRITE(nBits);
    READWRITE(nNonce);
```

## Flag ('F')
* Key : A string value, which is the name of the flag.
* Value : either 0 or 1
* list of flag names 
  * prunedblockfiles
  * txindex

## Reindex flag ('F')
* Key : None ( only the prefix 'F' is used for the leveldb key )
* Value : 1 ( assume : the key/value pair is removed when the reindex is finished )

## Last block ('l')
* Key : None ( only the prefix 'l' is used for the leveldb key )
* Value : The block file number ( int )


# List unspent coins 
Using the coins view database, we can list unspent coins for an address.

## rest.cpp
List UTXOs for a list of transaction hashes.
```
static bool rest_getutxos(HTTPRequest* req, const std::string& strURIPart)
```

## wallet.cpp
List UTXOs for a list of addresses.
```
UniValue listunspent(const UniValue& params, bool fHelp)
{
    int nMinDepth = 1;
    int nMaxDepth = 9999999;

    set<CBitcoinAddress> setAddress;

    vector<COutput> vecOutputs;

    pwalletMain->AvailableCoins(vecOutputs, false, NULL, true);
    
    BOOST_FOREACH(const COutput& out, vecOutputs) {
        if (out.nDepth < nMinDepth || out.nDepth > nMaxDepth)
            continue;

        if (setAddress.size()) {
            CTxDestination address;
            if (!ExtractDestination(out.tx->vout[out.i].scriptPubKey, address))
                continue;

            if (!setAddress.count(address))
                continue;
        }

        ... create an entry from out ...

        results.push_back(entry);
    }

    return results;
}
```