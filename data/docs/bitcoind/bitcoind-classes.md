# Classes in main.h

## CWallet
// A CWallet is an extension of a keystore, which also maintains a set of
// transactions and balances, and provides the ability to create new
// transactions
```
bool fFileBacked;
std::string strWalletFile;

std::set<int64> setKeyPool;

typedef std::map<unsigned int, CMasterKey> MasterKeyMap;
MasterKeyMap mapMasterKeys;
unsigned int nMasterKeyMaxID;

std::map<uint256, CWalletTx> mapWallet;
std::vector<uint256> vWalletUpdated;

std::map<uint256, int> mapRequestCount;

std::map<CBitcoinAddress, std::string> mapAddressBook;

std::vector<unsigned char> vchDefaultKey;    

```
## CInPoint
```
CTransaction* ptx;
unsigned int n;
```

## COutPoint
```
uint256 hash;
unsigned int n;
```

## CTxIn
// An input of a transaction.  It contains the location of the previous
// transaction's output that it claims and a signature that matches the
// output's public key.
```
COutPoint prevout;
CScript scriptSig;
unsigned int nSequence;
```

## CTxOut
// An output of a transaction.  It contains the public key that the next input
// must be able to sign with to claim it.
```
int64 nValue;
CScript scriptPubKey;
```


## CTransaction
// The basic transaction that is broadcasted on the network and contained in
// blocks.  A transaction can contain multiple inputs and outputs.
```
int nVersion;
std::vector<CTxIn> vin;
std::vector<CTxOut> vout;
unsigned int nLockTime;

// Denial-of-service detection:
mutable int nDoS;
```

## CMerkleTx 
// A transaction with a merkle branch linking it to the block chain
```
uint256 hashBlock;
std::vector<uint256> vMerkleBranch;
int nIndex;

// memory only
mutable char fMerkleVerified;
```

## CTxIndex
// A txdb record that contains the disk location of a transaction and the
// locations of transactions that spend its outputs.  vSpent is really only
// used as a flag, but having the location is very helpful for debugging.

```
CDiskTxPos pos;
std::vector<CDiskTxPos> vSpent;
```

## CBlock
// Nodes collect new transactions into a block, hash them into a hash tree,
// and scan through nonce values to make the block's hash satisfy proof-of-work
// requirements.  When they solve the proof-of-work, they broadcast the block
// to everyone and the block is added to the block chain.  The first transaction
// in the block is a special one that creates a new coin owned by the creator
// of the block.
//
// Blocks are appended to blk0001.dat files on disk.  Their location on disk
// is indexed by CBlockIndex objects in memory.
```
// header
int nVersion;
uint256 hashPrevBlock;
uint256 hashMerkleRoot;
unsigned int nTime;
unsigned int nBits;
unsigned int nNonce;

// network and disk
std::vector<CTransaction> vtx;

// memory only
mutable std::vector<uint256> vMerkleTree;

// Denial-of-service detection:
mutable int nDoS;
```


## CBlockIndex
// The block chain is a tree shaped structure starting with the
// genesis block at the root, with each block potentially having multiple
// candidates to be the next block.  pprev and pnext link a path through the
// main/longest chain.  A blockindex may have multiple pprev pointing back
// to it, but pnext will only point forward to the longest branch, or will
// be null if the block is not part of the longest chain.
```
// kangmo : comment - this is the hash of the block header.
const uint256* phashBlock;
CBlockIndex* pprev;
CBlockIndex* pnext;
unsigned int nFile;
unsigned int nBlockPos;
int nHeight;

// kangmo - comment : The cumulative amount of hash calculations done from the genesis block to this block.
// CBlock::AddToBlockIndex : pindexNew->bnChainWork = (pindexNew->pprev ? pindexNew->pprev->bnChainWork : 0) + pindexNew->GetBlockWork();
CBigNum bnChainWork;

// block header
int nVersion;
uint256 hashMerkleRoot;
unsigned int nTime;
unsigned int nBits;
unsigned int nNonce;
```

## CDiskBlockIndex
// Used to marshal pointers into hashes for db storage.
```
uint256 hashPrev;
uint256 hashNext;
```

## CBlockLocator

// Describes a place in the block chain to another node such that if the
// other node doesn't have the same branch, it can find a recent common trunk.
// The further back it is, the further before the fork it may be.

std::vector<uint256> vHave;

# Classes in db.h
## CDB
```
Db* pdb;
std::string strFile;
std::vector<DbTxn*> vTxn;
bool fReadOnly;
```
## CTxDB : public CDB
### operations
```
bool ReadTxIndex(uint256 hash, CTxIndex& txindex);
bool UpdateTxIndex(uint256 hash, const CTxIndex& txindex);
bool AddTxIndex(const CTransaction& tx, const CDiskTxPos& pos, int nHeight);
bool EraseTxIndex(const CTransaction& tx);
bool ContainsTx(uint256 hash);
bool ReadOwnerTxes(uint160 hash160, int nHeight, std::vector<CTransaction>& vtx);
bool ReadDiskTx(uint256 hash, CTransaction& tx, CTxIndex& txindex);
bool ReadDiskTx(uint256 hash, CTransaction& tx);
bool ReadDiskTx(COutPoint outpoint, CTransaction& tx, CTxIndex& txindex);
bool ReadDiskTx(COutPoint outpoint, CTransaction& tx);
bool WriteBlockIndex(const CDiskBlockIndex& blockindex);
bool EraseBlockIndex(uint256 hash);
bool ReadHashBestChain(uint256& hashBestChain);
bool WriteHashBestChain(uint256 hashBestChain);
bool ReadBestInvalidWork(CBigNum& bnBestInvalidWork);
bool WriteBestInvalidWork(CBigNum bnBestInvalidWork);
bool LoadBlockIndex();
```

## class CAddrDB : public CDB

## CKeyPool
```
int64 nTime;
std::vector<unsigned char> vchPubKey;
```

## class CWalletDB : public CDB
### Operations
```
bool ReadName(const std::string& strAddress, std::string& strName)
bool WriteName(const std::string& strAddress, const std::string& strName);
bool EraseName(const std::string& strAddress);
bool ReadTx(uint256 hash, CWalletTx& wtx)
bool WriteTx(uint256 hash, const CWalletTx& wtx)
bool EraseTx(uint256 hash)
bool ReadKey(const std::vector<unsigned char>& vchPubKey, CPrivKey& vchPrivKey)
bool WriteKey(const std::vector<unsigned char>& vchPubKey, const CPrivKey& vchPrivKey)
bool WriteCryptedKey(const std::vector<unsigned char>& vchPubKey, const std::vector<unsigned char>& vchCryptedSecret, bool fEraseUnencryptedKey = true)
bool WriteMasterKey(unsigned int nID, const CMasterKey& kMasterKey)
bool WriteBestBlock(const CBlockLocator& locator)
bool ReadBestBlock(CBlockLocator& locator)
bool ReadDefaultKey(std::vector<unsigned char>& vchPubKey)
bool WriteDefaultKey(const std::vector<unsigned char>& vchPubKey)
bool ReadPool(int64 nPool, CKeyPool& keypool)
bool WritePool(int64 nPool, const CKeyPool& keypool)
bool ErasePool(int64 nPool)
bool ReadSetting(const std::string& strKey, T& value)
bool WriteSetting(const std::string& strKey, const T& value)
bool ReadAccount(const std::string& strAccount, CAccount& account);
bool WriteAccount(const std::string& strAccount, const CAccount& account);
bool WriteAccountingEntry(const CAccountingEntry& acentry);
int64 GetAccountCreditDebit(const std::string& strAccount);
void ListAccountCreditDebit(const std::string& strAccount, std::list<CAccountingEntry>& acentries);
int LoadWallet(CWallet* pwallet);
```

# Classes in wallet.h
## CWallet : public CCryptoKeyStore
// A CWallet is an extension of a keystore, which also maintains a set of
// transactions and balances, and provides the ability to create new
// transactions
```
CWalletDB *pwalletdbEncryption;

mutable CCriticalSection cs_wallet;

bool fFileBacked;
std::string strWalletFile;

std::set<int64> setKeyPool;

typedef std::map<unsigned int, CMasterKey> MasterKeyMap;
MasterKeyMap mapMasterKeys;
unsigned int nMasterKeyMaxID;

std::map<uint256, CWalletTx> mapWallet;
std::vector<uint256> vWalletUpdated;
std::map<uint256, int> mapRequestCount;
std::map<CBitcoinAddress, std::string> mapAddressBook;
std::vector<unsigned char> vchDefaultKey;
```
### Operations
```
bool AddKey(const CKey& key);

// Adds a key to the store, without saving it to disk (used by LoadWallet)
bool LoadKey(const CKey& key) { return CCryptoKeyStore::AddKey(key); }

// Adds an encrypted key to the store, and saves it to disk.
bool AddCryptedKey(const std::vector<unsigned char> &vchPubKey, const std::vector<unsigned char> &vchCryptedSecret);
// Adds an encrypted key to the store, without saving it to disk (used by LoadWallet)
bool LoadCryptedKey(const std::vector<unsigned char> &vchPubKey, const std::vector<unsigned char> &vchCryptedSecret) { return CCryptoKeyStore::AddCryptedKey(vchPubKey, vchCryptedSecret); }

bool Unlock(const std::string& strWalletPassphrase);
bool ChangeWalletPassphrase(const std::string& strOldWalletPassphrase, const std::string& strNewWalletPassphrase);
bool EncryptWallet(const std::string& strWalletPassphrase);

bool AddToWallet(const CWalletTx& wtxIn);
bool AddToWalletIfInvolvingMe(const CTransaction& tx, const CBlock* pblock, bool fUpdate = false);
bool EraseFromWallet(uint256 hash);
void WalletUpdateSpent(const CTransaction& prevout);
int ScanForWalletTransactions(CBlockIndex* pindexStart, bool fUpdate = false);
void ReacceptWalletTransactions();
void ResendWalletTransactions();
int64 GetBalance() const;
int64 GetUnconfirmedBalance() const;
bool CreateTransaction(const std::vector<std::pair<CScript, int64> >& vecSend, CWalletTx& wtxNew, CReserveKey& reservekey, int64& nFeeRet);
bool CreateTransaction(CScript scriptPubKey, int64 nValue, CWalletTx& wtxNew, CReserveKey& reservekey, int64& nFeeRet);
bool CommitTransaction(CWalletTx& wtxNew, CReserveKey& reservekey);
bool BroadcastTransaction(CWalletTx& wtxNew);
std::string SendMoney(CScript scriptPubKey, int64 nValue, CWalletTx& wtxNew, bool fAskFee=false);
std::string SendMoneyToBitcoinAddress(const CBitcoinAddress& address, int64 nValue, CWalletTx& wtxNew, bool fAskFee=false);

bool NewKeyPool();
bool TopUpKeyPool();
void ReserveKeyFromKeyPool(int64& nIndex, CKeyPool& keypool);
void KeepKey(int64 nIndex);
void ReturnKey(int64 nIndex);
bool GetKeyFromPool(std::vector<unsigned char> &key, bool fAllowReuse=true);
int64 GetOldestKeyPoolTime();

bool IsMine(const CTxIn& txin) const;
int64 GetDebit(const CTxIn& txin) const;
bool IsMine(const CTxOut& txout) const
int64 GetCredit(const CTxOut& txout) const
bool IsChange(const CTxOut& txout) const
int64 GetChange(const CTxOut& txout) const
bool IsMine(const CTransaction& tx) const
bool IsFromMe(const CTransaction& tx) const
int64 GetDebit(const CTransaction& tx) const
int64 GetCredit(const CTransaction& tx) const
int64 GetChange(const CTransaction& tx) const
void SetBestChain(const CBlockLocator& loc)
int LoadWallet(bool& fFirstRunRet);
bool SetAddressBookName(const CBitcoinAddress& address, const std::string& strName);
bool DelAddressBookName(const CBitcoinAddress& address);
void UpdatedTransaction(const uint256 &hashTx)
void PrintWallet(const CBlock& block);
void Inventory(const uint256 &hash)
int GetKeyPoolSize()
bool GetTransaction(const uint256 &hashTx, CWalletTx& wtx);
bool SetDefaultKey(const std::vector<unsigned char> &vchPubKey);

```

## CReserveKey
```
CWallet* pwallet;
int64 nIndex;
std::vector<unsigned char> vchPubKey;
```

## CWalletTx : public CMerkleTx
// A transaction with a bunch of additional info that only the owner cares
// about.  It includes any unrecorded transactions needed to link it back
// to the block chain.
```
const CWallet* pwallet;

std::vector<CMerkleTx> vtxPrev;
std::map<std::string, std::string> mapValue;
std::vector<std::pair<std::string, std::string> > vOrderForm;
unsigned int fTimeReceivedIsTxTime;
unsigned int nTimeReceived;  // time received by this node
char fFromMe;
std::string strFromAccount;
std::vector<char> vfSpent; // which outputs are already spent

// memory only
mutable char fDebitCached;
mutable char fCreditCached;
mutable char fAvailableCreditCached;
mutable char fChangeCached;
mutable int64 nDebitCached;
mutable int64 nCreditCached;
mutable int64 nAvailableCreditCached;
mutable int64 nChangeCached;

// memory only UI hints
mutable unsigned int nTimeDisplayed;
mutable int nLinesDisplayed;
mutable char fConfirmedDisplayed;
```

### Operations
```
// marks certain txout's as spent
// returns true if any update took place
bool UpdateSpent(const std::vector<char>& vfNewSpent)

// make sure balances are recalculated
void MarkDirty()

void MarkSpent(unsigned int nOut)
bool IsSpent(unsigned int nOut) const
int64 GetDebit() const
int64 GetCredit(bool fUseCache=true) const
int64 GetAvailableCredit(bool fUseCache=true) const
int64 GetChange() const
void GetAmounts(int64& nGeneratedImmature, int64& nGeneratedMature, std::list<std::pair<CBitcoinAddress, int64> >& listReceived,
                std::list<std::pair<CBitcoinAddress, int64> >& listSent, int64& nFee, std::string& strSentAccount) const;
void GetAccountAmounts(const std::string& strAccount, int64& nGenerated, int64& nReceived, 
                       int64& nSent, int64& nFee) const;
bool IsFromMe() const
bool IsConfirmed() const
bool WriteToDisk();
int64 GetTxTime() const;
int GetRequestCount() const;
void AddSupportingTransactions(CTxDB& txdb);
bool AcceptWalletTransaction(CTxDB& txdb, bool fCheckInputs=true);
bool AcceptWalletTransaction();
void RelayWalletTransaction(CTxDB& txdb);
void RelayWalletTransaction();

```

## CWalletKey
// Private key that includes an expiration date in case it never gets used.
```
CPrivKey vchPrivKey;
int64 nTimeCreated;
int64 nTimeExpires;
std::string strComment;
```

## CAccount
// Account information.
// Stored in wallet with key "acc"+string account name
```
std::vector<unsigned char> vchPubKey;
```

## CAccountingEntry
// Internal transfers.
// Database key is acentry<account><counter>
```
std::string strAccount;
int64 nCreditDebit;
int64 nTime;
std::string strOtherAccount;
std::string strComment;
```

# Classess from net.h
## CNode
```
// socket
uint64 nServices;
SOCKET hSocket;
CDataStream vSend;
CDataStream vRecv;
CCriticalSection cs_vSend;
CCriticalSection cs_vRecv;
int64 nLastSend;
int64 nLastRecv;
int64 nLastSendEmpty;
int64 nTimeConnected;
unsigned int nHeaderStart;
unsigned int nMessageStart;
CAddress addr;
int nVersion;
std::string strSubVer;
bool fClient;
bool fInbound;
bool fNetworkNode;
bool fSuccessfullyConnected;
bool fDisconnect;

protected:
int nRefCount;

// Denial-of-service detection/prevention
// Key is ip address, value is banned-until-time
static std::map<unsigned int, int64> setBanned;
static CCriticalSection cs_setBanned;
int nMisbehavior;

public:
int64 nReleaseTime;
std::map<uint256, CRequestTracker> mapRequests;
CCriticalSection cs_mapRequests;
uint256 hashContinue;
CBlockIndex* pindexLastGetBlocksBegin;
uint256 hashLastGetBlocksEnd;
// kangmo : comment - Part of the Version message : 4	start_height	int32_t	The last block received by the emitting node
int nStartingHeight;

// flood relay
std::vector<CAddress> vAddrToSend;
std::set<CAddress> setAddrKnown;
bool fGetAddr;
std::set<uint256> setKnown;

// inventory based relay
std::set<CInv> setInventoryKnown;
std::vector<CInv> vInventoryToSend;
CCriticalSection cs_inventory;
std::multimap<int64, CInv> mapAskFor;

// publish and subscription
std::vector<char> vfSubscribe;

```
### Oprations
```
int GetRefCount()
CNode* AddRef(int64 nTimeout=0)
void Release()
void AddAddressKnown(const CAddress& addr)

// kangmo : comment - Reserve sending an address to the peer later if the peer does not know the address.
void PushAddress(const CAddress& addr)
void AddInventoryKnown(const CInv& inv)
void PushInventory(const CInv& inv)
void AskFor(const CInv& inv)
void BeginMessage(const char* pszCommand)
void AbortMessage()
void EndMessage()
void EndMessageAbortIfEmpty()
void PushVersion()
void PushMessage(const char* pszCommand)
void PushMessage(const char* pszCommand, const T1& a1)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4, const T5& a5)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4, const T5& a5, const T6& a6)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4, const T5& a5, const T6& a6, const T7& a7)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4, const T5& a5, const T6& a6, const T7& a7, const T8& a8)
void PushMessage(const char* pszCommand, const T1& a1, const T2& a2, const T3& a3, const T4& a4, const T5& a5, const T6& a6, const T7& a7, const T8& a8, const T9& a9)
void PushRequest(const char* pszCommand,
                 void (*fn)(void*, CDataStream&), void* param1)
void PushRequest(const char* pszCommand, const T1& a1,
                 void (*fn)(void*, CDataStream&), void* param1)
void PushRequest(const char* pszCommand, const T1& a1, const T2& a2,
                 void (*fn)(void*, CDataStream&), void* param1)
void PushGetBlocks(CBlockIndex* pindexBegin, uint256 hashEnd);
bool IsSubscribed(unsigned int nChannel);
void Subscribe(unsigned int nChannel, unsigned int nHops=0);
void CancelSubscribe(unsigned int nChannel);
void CloseSocketDisconnect();
void Cleanup();
static void ClearBanned(); // needed for unit testing
static bool IsBanned(unsigned int ip);
bool Misbehaving(int howmuch); // 1 == a little, 100 == a lot
```