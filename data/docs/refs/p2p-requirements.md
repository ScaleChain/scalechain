# Introduction
This document summarizes requirements for peer to peer communication. 
Also, for each requirement, we will summarize the result of Bitcoin core source code analysis.
Some of texts in this document are copied from the bitcoin developer's guide.

# Summary of requirements
## Detect hard fork  
detects a hard fork by looking at block chain proof of work. 
If a non-upgraded node receives block chain headers 
demonstrating at least six blocks more proof of work than the best chain it considers valid, 
the node reports an error in the getinfo RPC results and runs the -alertnotify command if set. 

## signature validation based on the block version 

## prefix coinable data with block height

## Implement -alertnotify

## check unsupported block version or transaction version 

## check if a transaction is a standard one 
https://bitcoin.org/en/developer-guide#non-standard-transactions

## implement different hash types
https://bitcoin.org/en/developer-guide#signature-hash-types

## Check transaction fee 
https://bitcoin.org/en/developer-guide#transaction-fees-and-change

## store peer IPs and ports on disk 
https://bitcoin.org/en/developer-guide#peer-discovery

## Do IBD if blocks are 1 day or 144 blocks behind
https://bitcoin.org/en/developer-guide#initial-block-download

## (CUT) get blocks : if no hash matches, send 500 blocks from block1 in Inv.
https://bitcoin.org/en/developer-guide#blocks-first 

## (CUT) get blocks : send blocks from the first matching block header hash in get blocks message
We need to match with blocks in the best blockchain. 
why? fork detection https://bitcoin.org/en/developer-guide#blocks-first

## getheaders : send block headers from the first matching block header hash in getheaders message
We need to match with blocks in the best blockchain. 
why? fork detection

## Start IBD with another sync node if one goes offline 
IBD node should start IBD with another sync node.

## (P2) Add block chain checkpoints
To know if the blocks received by IBD are on best blockchain as early as possible. 
https://bitcoin.org/en/developer-guide#blocks-first

## (CUT) blocks-first : Keep orphan blocks in memory. 
Orphan blocks are stored in memory while they await validation, 
which may lead to high memory use.  https://bitcoin.org/en/developer-guide#blocks-first

## headers-first : Start block download after getting all headers 
Download the headers for the best header chain, partially validate them as best as possible, 
and then download the corresponding blocks in parallel.
https://bitcoin.org/en/developer-guide#headers-first

## Get headers from multiple peers to get the best blockchain 
It sends a getheaders message to each of its outboundpeers 
to get their view of best header chain, after receiving all block headers from a sync node. 
https://bitcoin.org/en/developer-guide#headers-first

## headers-first : parallel block download.
Request up to 16 blocks at a time from a single peer. 
Combined with its maximum of 8 outbound connections.
https://bitcoin.org/en/developer-guide#headers-first

## Parallel download using download window 
Uses a 1,024-block moving download window to maximize download speed

## Disconnect stalling nodes during IBD
Wait a minimum of two more seconds for the stalling node to send the block. 
If the block still hasn’t arrived, Bitcoin Core will disconnect from the stalling node and 
attempt to connect to another node.

## (CUT) blocks-first : connect orphan blocks when a parent was received.
Once the parent of the former orphan block has been validated, 
it will validate the former orphan block. 
https://bitcoin.org/en/developer-guide#orphan-blocks

## (Done) Discard orphan blocks while headers-first IBD was running.
headers-first : each of those headers will point to its parent, 
so when the downloading node receives the block message, 
the block shouldn’t be an orphan block. 
If, despite this, the block received in the block message is an orphan block, 
a headers-first node will discard it immediately.

## Add transactions in stale blocks back to mempool
Transactions which are mined into blocks that 
later become stale blocks may be added back into the memory pool.  
https://bitcoin.org/en/developer-guide#memory-pool

## Ban a peer based on a banscore.
If a peer gets a banscore above the -banscore=<n> threshold, 
he will be banned for the number of seconds defined by -bantime=<n>, 
which is 86,400 by default (24 hours). 

https://bitcoin.org/en/developer-guide#misbehaving-nodes

## Add Getblocktemplate RPC 
Their mining software periodically polls bitcoind for new transactions 
using the getblocktemplate RPC, which provides the list of new transactions plus 
the public key to which the coinbase transaction should be sent.

## Add Getblockhash RPC
Need to get a blockhash by a block height. 
Need to return the block on the best blockchain in case there is a stale block on a fork. 
One way to implement this RPC is to have a key/value map with key=block height, value=blockhash,
but we need to update the mapping whenever the block on the map turned out to be a stale block.

### Summary of Analysis
The chain of block headers is kept in memory. Each block header is abstracted by CBlockIndex, 
and the blockchain is a vector containing CBlockIndex from the genesis block(=height 0).

A CBlockIndex contains information necessary to get the raw block data from the blkNNNNN.dat file,
by having the file number(=NNNNN part of the blkNNNNN.dat file) and offset in the file, 
where the block is written.

### Need investigation 
CBlockIndex.nUndoPos ; block's undo data?

### src/main.cpp
```
CChain chainActive;
```

### src/chain.h
CChain:
```
/** An in-memory indexed chain of blocks. */
class CChain {
private:
    std::vector<CBlockIndex*> vChain;
    ...
```

CBlockIndex:
```
/** The block chain is a tree shaped structure starting with the
 * genesis block at the root, with each block potentially having multiple
 * candidates to be the next block. A blockindex may have multiple pprev pointing
 * to it, but at most one of them can be part of the currently active branch.
 */
class CBlockIndex
{
public:
    //! pointer to the hash of the block, if any. Memory is owned by this CBlockIndex
    const uint256* phashBlock;

    //! pointer to the index of the predecessor of this block
    CBlockIndex* pprev;

    //! pointer to the index of some further predecessor of this block
    CBlockIndex* pskip;

    //! height of the entry in the chain. The genesis block has height 0
    int nHeight;

    //! Which # file this block is stored in (blk?????.dat)
    int nFile;

    //! Byte offset within blk?????.dat where this block's data is stored
    unsigned int nDataPos;

    //! Byte offset within rev?????.dat where this block's undo data is stored
    unsigned int nUndoPos;

    //! (memory only) Total amount of work (expected number of hashes) in the chain 
    // up to and including this block
    arith_uint256 nChainWork;

    //! Number of transactions in this block.
    //! Note: in a potential headers-first mode, this number cannot be relied upon
    unsigned int nTx;

    //! (memory only) Number of transactions in the chain up to and including this block.
    //! This value will be non-zero only if and only if transactions for this block and all its parents are available.
    //! Change to 64-bit type when necessary; won't happen before 2030
    unsigned int nChainTx;

    //! Verification status of this block. See enum BlockStatus
    unsigned int nStatus;

    //! block header
    int nVersion;
    uint256 hashMerkleRoot;
    unsigned int nTime;
    unsigned int nBits;
    unsigned int nNonce;

    //! (memory only) Sequential id assigned to distinguish order in which blocks are received.
    uint32_t nSequenceId;
    ...
}
```
### src/rpc/blockchain.cpp

```
UniValue getblockhash(const UniValue& params, bool fHelp)
{
    ... 
    
    CBlockIndex* pblockindex = chainActive[nHeight];
    return pblockindex->GetBlockHash().GetHex();
}
```