# Data structure of Bitcoind

Come texts are copied from comments in the bitcoin core source code.
These are global variables in main.cpp.

# set<CWallet*> setpwalletRegistered;
## Functions or Event notifications from the blockchain
```
void RegisterWallet(CWallet* pwalletIn)
void UnregisterWallet(CWallet* pwalletIn)

// check whether the passed transaction is from us
bool static IsFromMe(CTransaction& tx)

// get the wallet transaction with the given hash (if it exists)
bool static GetTransaction(const uint256& hashTx, CWalletTx& wtx)

// erases transaction with the given hash from all wallets
void static EraseFromWallets(uint256 hash)

// make sure all wallets know about the given transaction, in the given block
void static SyncWithWallets(const CTransaction& tx, const CBlock* pblock = NULL, bool fUpdate = false)

// notify wallets about a new best chain
void static SetBestChain(const CBlockLocator& loc)

// notify wallets about an updated transaction
void static UpdatedTransaction(const uint256& hashTx)

// dump all wallets
void static PrintWallets(const CBlock& block)

// notify wallets about an incoming inventory (for request counts)
void static Inventory(const uint256& hash)

// ask wallets to resend their transactions
void static ResendWalletTransactions()

```

# map<uint256, CTransaction> mapTransactions;
The mempool that keeps transactions in memory.
## Examples
### mapTransactions.count(hash)
See if a transaction is in the mempool.

## Used by 
###
###

# map<COutPoint, CInPoint> mapNextTx;
## Used by 
###
###

# map<uint256, CBlockIndex*> mapBlockIndex;
## Used by 
###
###

# CMedianFilter<int> cPeerBlockCounts(5, 0);
## Used by 
###
###

# map<uint256, CBlock*> mapOrphanBlocks;
## Used by 
###
###

# multimap<uint256, CBlock*> mapOrphanBlocksByPrev;
orphan blocks by the previous hash of it. 
Because multiple orphan blocks may have the same 'previous hash', we need to use multimap.
## Used by 
###
###

# map<uint256, CDataStream*> mapOrphanTransactions;
## Used by 
###
###

# multimap<uint256, CDataStream*> mapOrphanTransactionsByPrev;
## Used by 
###
###



# Examples
## Get tx from mempool
```
mapTransactions[txHash]
```         
## Get tx from disk
```
TxDB.ReadTxIndex(txHash, CTxIndex);
CTransaction.ReadFromDisk(CTxIndex.CDiskTxPos)
```
            