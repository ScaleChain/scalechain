# Introduction
This document summarizes requirements for peer to peer communication. 
Also, for each requirement, we will summarize the result of Bitcoin core source code analysis.
Some of texts in this document are copied from the bitcoin developer's guide.

# Summary of requirements
## (P0) Need to check the locktime and the sequence number in a transaction.
No need to verify the locktime if the sequeunce number is 0xffffffff.
Need to verify the locktime otherwise.

### src/main.cpp

```

bool IsFinalTx(const CTransaction &tx, int nBlockHeight, int64_t nBlockTime)
{
    if (tx.nLockTime == 0)
        return true;
    if ((int64_t)tx.nLockTime < ((int64_t)tx.nLockTime < LOCKTIME_THRESHOLD ? (int64_t)nBlockHeight : nBlockTime))
        return true;
    BOOST_FOREACH(const CTxIn& txin, tx.vin)
        if (!txin.IsFinal())
            return false;
    return true;
}

bool CheckFinalTx(const CTransaction &tx, int flags)
{
    AssertLockHeld(cs_main);

    // By convention a negative value for flags indicates that the
    // current network-enforced consensus rules should be used. In
    // a future soft-fork scenario that would mean checking which
    // rules would be enforced for the next block and setting the
    // appropriate flags. At the present time no soft-forks are
    // scheduled, so no flags are set.
    flags = std::max(flags, 0);

    // CheckFinalTx() uses chainActive.Height()+1 to evaluate
    // nLockTime because when IsFinalTx() is called within
    // CBlock::AcceptBlock(), the height of the block *being*
    // evaluated is what is used. Thus if we want to know if a
    // transaction can be part of the *next* block, we need to call
    // IsFinalTx() with one more than chainActive.Height().
    const int nBlockHeight = chainActive.Height() + 1;

    // BIP113 will require that time-locked transactions have nLockTime set to
    // less than the median time of the previous block they're contained in.
    // When the next block is created its previous block will be the current
    // chain tip, so we use that to calculate the median time passed to
    // IsFinalTx() if LOCKTIME_MEDIAN_TIME_PAST is set.
    const int64_t nBlockTime = (flags & LOCKTIME_MEDIAN_TIME_PAST)
                             ? chainActive.Tip()->GetMedianTimePast()
                             : GetAdjustedTime();

    return IsFinalTx(tx, nBlockHeight, nBlockTime);
}
```

And whenever we put a transaction into the mempool, we have to check the locktime.
```
bool AcceptToMemoryPoolWorker(CTxMemPool& pool, CValidationState &state, const CTransaction &tx, bool fLimitFree,
                              bool* pfMissingInputs, bool fOverrideMempoolLimit, bool fRejectAbsurdFee,
                              std::vector<uint256>& vHashTxnToUncache)
{
    ...

    // Only accept nLockTime-using transactions that can be mined in the next
    // block; we don't want our mempool filled up with transactions that can't
    // be mined yet.
    if (!CheckFinalTx(tx, STANDARD_LOCKTIME_VERIFY_FLAGS))
        return state.DoS(0, false, REJECT_NONSTANDARD, "non-final");

```

We need check if a transaction in the mempool violates the lock time 
upon the reorg of blockchain. 

Why? Because a block is removed from the tip of the best blockchain, 
the length of the best blockchain is decreased. We need to check the lock time again, 
because the lock time depends of the number of blocks on the best blockchain. 
```
void CTxMemPool::removeForReorg(const CCoinsViewCache *pcoins, unsigned int nMemPoolHeight, int flags)
{
    // Remove transactions spending a coinbase which are now immature and no-longer-final transactions
    LOCK(cs);
    list<CTransaction> transactionsToRemove;
    for (indexed_transaction_set::const_iterator it = mapTx.begin(); it != mapTx.end(); it++) {
        const CTransaction& tx = it->GetTx();
        if (!CheckFinalTx(tx, flags)) {
            transactionsToRemove.push_back(tx);
```

Need investigation : Don't we need to check the locktime for transactions in a block?


## (P0) check unsupported block version or transaction version 

### src/policy/policy.cpp
The IsStandardTx function checks the range of version in a transaction.
```
bool IsStandardTx(const CTransaction& tx, std::string& reason)
{
    if (tx.nVersion > CTransaction::CURRENT_VERSION || tx.nVersion < 1) {

```

### src/main.cpp
Looks like there is no max limit for the block version, but 
checks the version of the last 100 blocks to see if we need to upgrade.
In case we need to upgrade, we notify an alert message.
```

/** Update chainActive and related internal data structures. */
void static UpdateTip(CBlockIndex *pindexNew) {
    const CChainParams& chainParams = Params();
    chainActive.SetTip(pindexNew);

    // New best block
    nTimeBestReceived = GetTime();
    mempool.AddTransactionsUpdated(1);

    LogPrintf("%s: new best=%s  height=%d  log2_work=%.8g  tx=%lu  date=%s progress=%f  cache=%.1fMiB(%utx)\n", __func__,
      chainActive.Tip()->GetBlockHash().ToString(), chainActive.Height(), log(chainActive.Tip()->nChainWork.getdouble())/log(2.0), (unsigned long)chainActive.Tip()->nChainTx,
      DateTimeStrFormat("%Y-%m-%d %H:%M:%S", chainActive.Tip()->GetBlockTime()),
      Checkpoints::GuessVerificationProgress(chainParams.Checkpoints(), chainActive.Tip()), pcoinsTip->DynamicMemoryUsage() * (1.0 / (1<<20)), pcoinsTip->GetCacheSize());

    cvBlockChange.notify_all();

    // Check the version of the last 100 blocks to see if we need to upgrade:
    static bool fWarned = false;
    if (!IsInitialBlockDownload() && !fWarned)
    {
        int nUpgraded = 0;
        const CBlockIndex* pindex = chainActive.Tip();
        for (int i = 0; i < 100 && pindex != NULL; i++)
        {
            if (pindex->nVersion > CBlock::CURRENT_VERSION)
                ++nUpgraded;
            pindex = pindex->pprev;
        }
        if (nUpgraded > 0)
            LogPrintf("%s: %d of last 100 blocks above version %d\n", __func__, nUpgraded, (int)CBlock::CURRENT_VERSION);
        if (nUpgraded > 100/2)
        {
            // strMiscWarning is read by GetWarnings(), called by Qt and the JSON-RPC code to warn the user:
            strMiscWarning = _("Warning: This version is obsolete; upgrade required!");
            CAlert::Notify(strMiscWarning, true);
            fWarned = true;
        }
    }
}

```

## (P0) Disconnect stalling nodes during IBD
Wait a minimum of two more seconds for the stalling node to send the block. 
If the block still hasn’t arrived, Bitcoin Core will disconnect from the stalling node and 
attempt to connect to another node.

Currently, we are doing nothing if the sync node which provides blocks or block headers is disconnected.
We need to detect this case and continue downloading blocks and headers from another sync node.

### src/main.cpp
Need to disconnect a peer that is stalling.
```
bool SendMessages(CNode* pto) {
   ....
        // Detect whether we're stalling
        nNow = GetTimeMicros();
        if (!pto->fDisconnect && state.nStallingSince && state.nStallingSince < nNow - 1000000 * BLOCK_STALLING_TIMEOUT) {
            // Stalling only triggers when the block download window cannot move. During normal steady state,
            // the download window should be much larger than the to-be-downloaded set of blocks, so disconnection
            // should only happen during initial block download.
            LogPrintf("Peer=%d is stalling block download, disconnecting\n", pto->id);
            pto->fDisconnect = true;
        }
        
        
        // In case there is a block that has been in flight from this peer for (2 + 0.5 * N) times the block interval
        // (with N the number of validated blocks that were in flight at the time it was requested), disconnect due to
        // timeout. We compensate for in-flight blocks to prevent killing off peers due to our own downstream link
        // being saturated. We only count validated in-flight blocks so peers can't advertise non-existing block hashes
        // to unreasonably increase our timeout.
        // We also compare the block download timeout originally calculated against the time at which we'd disconnect
        // if we assumed the block were being requested now (ignoring blocks we've requested from this peer, since we're
        // only looking at this peer's oldest request).  This way a large queue in the past doesn't result in a
        // permanently large window for this block to be delivered (ie if the number of blocks in flight is decreasing
        // more quickly than once every 5 minutes, then we'll shorten the download window for this block).
        if (!pto->fDisconnect && state.vBlocksInFlight.size() > 0) {
            QueuedBlock &queuedBlock = state.vBlocksInFlight.front();
            int64_t nTimeoutIfRequestedNow = GetBlockTimeout(nNow, nQueuedValidatedHeaders - state.nBlocksInFlightValidHeaders, consensusParams);
            if (queuedBlock.nTimeDisconnect > nTimeoutIfRequestedNow) {
                LogPrint("net", "Reducing block download timeout for peer=%d block=%s, orig=%d new=%d\n", pto->id, queuedBlock.hash.ToString(), queuedBlock.nTimeDisconnect, nTimeoutIfRequestedNow);
                queuedBlock.nTimeDisconnect = nTimeoutIfRequestedNow;
            }
            if (queuedBlock.nTimeDisconnect < nNow) {
                LogPrintf("Timeout downloading block %s from peer=%d, disconnecting\n", queuedBlock.hash.ToString(), pto->id);
                pto->fDisconnect = true;
            }
        }
        
```


## (P0) Start IBD with another sync node if one goes offline
IBD node should start IBD with another sync node.

### src/main.cpp
SendMessage does PushMessage(NetMsgType::GETHEADERS, ...) for a 'nice' peer.
Assumption : Looks like after disconnecting to a stalling peer, we are choosing a new 'nice' peer to download the headers from it.
```
bool SendMessages(CNode* pto)
{
    ...
        // Start block sync
        if (pindexBestHeader == NULL)
            pindexBestHeader = chainActive.Tip();

        // Download if this is a nice peer, or we have no nice peers and this one might do.
        bool fFetch = state.fPreferredDownload || (nPreferredDownload == 0 && !pto->fClient && !pto->fOneShot); 
        
        if (!state.fSyncStarted && !pto->fClient && !fImporting && !fReindex) {
            // Only actively request headers from a single peer, unless we're close to today.
            if ((nSyncStarted == 0 && fFetch) || pindexBestHeader->GetBlockTime() > GetAdjustedTime() - 24 * 60 * 60) {
                state.fSyncStarted = true;
                nSyncStarted++;
                const CBlockIndex *pindexStart = pindexBestHeader;
                /* If possible, start at the block preceding the currently
                   best known header.  This ensures that we always get a
                   non-empty list of headers back as long as the peer
                   is up-to-date.  With a non-empty response, we can initialise
                   the peer's known best block.  This wouldn't be possible
                   if we requested starting at pindexBestHeader and
                   got back an empty response.  */
                if (pindexStart->pprev)
                    pindexStart = pindexStart->pprev;
                LogPrint("net", "initial getheaders (%d) to peer=%d (startheight:%d)\n", pindexStart->nHeight, pto->id, pto->nStartingHeight);
                pto->PushMessage(NetMsgType::GETHEADERS, chainActive.GetLocator(pindexStart), uint256());
            }
        }
```


## (P0) Add transactions in stale blocks back to mempool
Transactions which are mined into blocks that 
later become stale blocks may be added back into the memory pool.  
https://bitcoin.org/en/developer-guide#memory-pool

### src/main.cpp

Maybe this code is about removing a stale block from the best blockchain? (Not sure, need more investigation)
Looks like this code is about reconstructing the UTXO? (Not sure, need more investigation)
```
/**
 * Apply the undo operation of a CTxInUndo to the given chain state.
 * @param undo The undo object.
 * @param view The coins view to which to apply the changes.
 * @param out The out point that corresponds to the tx input.
 * @return True on success.
 */
static bool ApplyTxInUndo(const CTxInUndo& undo, CCoinsViewCache& view, const COutPoint& out)
{
    bool fClean = true;

    CCoinsModifier coins = view.ModifyCoins(out.hash);
    if (undo.nHeight != 0) {
        // undo data contains height: this is the last output of the prevout tx being spent
        if (!coins->IsPruned())
            fClean = fClean && error("%s: undo data overwriting existing transaction", __func__);
        coins->Clear();
        coins->fCoinBase = undo.fCoinBase;
        coins->nHeight = undo.nHeight;
        coins->nVersion = undo.nVersion;
    } else {
        if (coins->IsPruned())
            fClean = fClean && error("%s: undo data adding output to missing transaction", __func__);
    }
    if (coins->IsAvailable(out.n))
        fClean = fClean && error("%s: undo data overwriting existing output", __func__);
    if (coins->vout.size() < out.n+1)
        coins->vout.resize(out.n+1);
    coins->vout[out.n] = undo.txout;

    return fClean;
}

bool DisconnectBlock(const CBlock& block, CValidationState& state, const CBlockIndex* pindex, CCoinsViewCache& view, bool* pfClean)
{
    assert(pindex->GetBlockHash() == view.GetBestBlock());

    if (pfClean)
        *pfClean = false;

    bool fClean = true;

    CBlockUndo blockUndo;
    CDiskBlockPos pos = pindex->GetUndoPos();
    if (pos.IsNull())
        return error("DisconnectBlock(): no undo data available");
    if (!UndoReadFromDisk(blockUndo, pos, pindex->pprev->GetBlockHash()))
        return error("DisconnectBlock(): failure reading undo data");

    if (blockUndo.vtxundo.size() + 1 != block.vtx.size())
        return error("DisconnectBlock(): block and undo data inconsistent");

    // undo transactions in reverse order
    for (int i = block.vtx.size() - 1; i >= 0; i--) {
        const CTransaction &tx = block.vtx[i];
        uint256 hash = tx.GetHash();

        // Check that all outputs are available and match the outputs in the block itself
        // exactly.
        {
        CCoinsModifier outs = view.ModifyCoins(hash);
        outs->ClearUnspendable();

        CCoins outsBlock(tx, pindex->nHeight);
        // The CCoins serialization does not serialize negative numbers.
        // No network rules currently depend on the version here, so an inconsistency is harmless
        // but it must be corrected before txout nversion ever influences a network rule.
        if (outsBlock.nVersion < 0)
            outs->nVersion = outsBlock.nVersion;
        if (*outs != outsBlock)
            fClean = fClean && error("DisconnectBlock(): added transaction mismatch? database corrupted");

        // remove outputs
        outs->Clear();
        }

        // restore inputs
        if (i > 0) { // not coinbases
            const CTxUndo &txundo = blockUndo.vtxundo[i-1];
            if (txundo.vprevout.size() != tx.vin.size())
                return error("DisconnectBlock(): transaction and undo data inconsistent");
            for (unsigned int j = tx.vin.size(); j-- > 0;) {
                const COutPoint &out = tx.vin[j].prevout;
                const CTxInUndo &undo = txundo.vprevout[j];
                if (!ApplyTxInUndo(undo, view, out))
                    fClean = false;
            }
        }
    }

    // move best block pointer to prevout block
    view.SetBestBlock(pindex->pprev->GetBlockHash());

    if (pfClean) {
        *pfClean = fClean;
        return true;
    }

    return fClean;
}
```

## (P0) Check block header hash & timestamp

### src/main.cpp
We need to check the hash of the block header is less than the target block hash.
We also need to make sure that the timestamp on the block header is less than the current time + two hours.
```
bool CheckBlockHeader(const CBlockHeader& block, CValidationState& state, bool fCheckPOW)
{
    // Check proof of work matches claimed amount
    if (fCheckPOW && !CheckProofOfWork(block.GetHash(), block.nBits, Params().GetConsensus()))
        return state.DoS(50, error("CheckBlockHeader(): proof of work failed"),
                         REJECT_INVALID, "high-hash");

    // Check timestamp
    if (block.GetBlockTime() > GetAdjustedTime() + 2 * 60 * 60)
        return state.Invalid(error("CheckBlockHeader(): block timestamp too far in the future"),
                             REJECT_INVALID, "time-too-new");

    return true;
}
```

## (P1) Need to be able to reorg blocks
### src/main.cpp
To remove a stale block from the best blockchain, first disconnect the block from the best blockchain.
```
bool InvalidateBlock(CValidationState& state, const Consensus::Params& consensusParams, CBlockIndex *pindex)
{
    AssertLockHeld(cs_main);

    // Mark the block itself as invalid.
    pindex->nStatus |= BLOCK_FAILED_VALID;
    setDirtyBlockIndex.insert(pindex);
    setBlockIndexCandidates.erase(pindex);

    while (chainActive.Contains(pindex)) {
        CBlockIndex *pindexWalk = chainActive.Tip();
        pindexWalk->nStatus |= BLOCK_FAILED_CHILD;
        setDirtyBlockIndex.insert(pindexWalk);
        setBlockIndexCandidates.erase(pindexWalk);
        // ActivateBestChain considers blocks already in chainActive
        // unconditionally valid already, so force disconnect away from it.
        if (!DisconnectTip(state, consensusParams)) {
            mempool.removeForReorg(pcoinsTip, chainActive.Tip()->nHeight + 1, STANDARD_LOCKTIME_VERIFY_FLAGS);
            return false;
        }
    }

    LimitMempoolSize(mempool, GetArg("-maxmempool", DEFAULT_MAX_MEMPOOL_SIZE) * 1000000, GetArg("-mempoolexpiry", DEFAULT_MEMPOOL_EXPIRY) * 60 * 60);

    // The resulting new best tip may not be in setBlockIndexCandidates anymore, so
    // add it again.
    BlockMap::iterator it = mapBlockIndex.begin();
    while (it != mapBlockIndex.end()) {
        if (it->second->IsValid(BLOCK_VALID_TRANSACTIONS) && it->second->nChainTx && !setBlockIndexCandidates.value_comp()(it->second, chainActive.Tip())) {
            setBlockIndexCandidates.insert(it->second);
        }
        it++;
    }

    InvalidChainFound(pindex);
    mempool.removeForReorg(pcoinsTip, chainActive.Tip()->nHeight + 1, STANDARD_LOCKTIME_VERIFY_FLAGS);
    return true;
}
```

### src/txmempool.cpp
And move transactions spending coinbase of the stale block from mempool.
```
void CTxMemPool::removeForReorg(const CCoinsViewCache *pcoins, unsigned int nMemPoolHeight, int flags)
{
    // Remove transactions spending a coinbase which are now immature and no-longer-final transactions
    LOCK(cs);
    list<CTransaction> transactionsToRemove;
    for (indexed_transaction_set::const_iterator it = mapTx.begin(); it != mapTx.end(); it++) {
        const CTransaction& tx = it->GetTx();
        if (!CheckFinalTx(tx, flags)) {
            transactionsToRemove.push_back(tx);
        } else if (it->GetSpendsCoinbase()) {
            BOOST_FOREACH(const CTxIn& txin, tx.vin) {
                indexed_transaction_set::const_iterator it2 = mapTx.find(txin.prevout.hash);
                if (it2 != mapTx.end())
                    continue;
                const CCoins *coins = pcoins->AccessCoins(txin.prevout.hash);
		if (nCheckFrequency != 0) assert(coins);
                if (!coins || (coins->IsCoinBase() && ((signed long)nMemPoolHeight) - coins->nHeight < COINBASE_MATURITY)) {
                    transactionsToRemove.push_back(tx);
                    break;
                }
            }
        }
    }
    BOOST_FOREACH(const CTransaction& tx, transactionsToRemove) {
        list<CTransaction> removed;
        remove(tx, removed, true);
    }
}

```

## (P1) Need to verify consistency of block and coin database.
### src/main.h
```
/** RAII wrapper for VerifyDB: Verify consistency of the block and coin databases */
class CVerifyDB {
public:
    CVerifyDB();
    ~CVerifyDB();
    bool VerifyDB(const CChainParams& chainparams, CCoinsView *coinsview, int nCheckLevel, int nCheckDepth);
};
```

### src/main.cpp
```
bool CVerifyDB::VerifyDB(const CChainParams& chainparams, CCoinsView *coinsview, int nCheckLevel, int nCheckDepth)
{
    LOCK(cs_main);
    if (chainActive.Tip() == NULL || chainActive.Tip()->pprev == NULL)
        return true;

    // Verify blocks in the best chain
    if (nCheckDepth <= 0)
        nCheckDepth = 1000000000; // suffices until the year 19000
    if (nCheckDepth > chainActive.Height())
        nCheckDepth = chainActive.Height();
    nCheckLevel = std::max(0, std::min(4, nCheckLevel));
    LogPrintf("Verifying last %i blocks at level %i\n", nCheckDepth, nCheckLevel);
    CCoinsViewCache coins(coinsview);
    CBlockIndex* pindexState = chainActive.Tip();
    CBlockIndex* pindexFailure = NULL;
    int nGoodTransactions = 0;
    CValidationState state;
    for (CBlockIndex* pindex = chainActive.Tip(); pindex && pindex->pprev; pindex = pindex->pprev)
    {
        boost::this_thread::interruption_point();
        uiInterface.ShowProgress(_("Verifying blocks..."), std::max(1, std::min(99, (int)(((double)(chainActive.Height() - pindex->nHeight)) / (double)nCheckDepth * (nCheckLevel >= 4 ? 50 : 100)))));
        if (pindex->nHeight < chainActive.Height()-nCheckDepth)
            break;
        CBlock block;
        // check level 0: read from disk
        if (!ReadBlockFromDisk(block, pindex, chainparams.GetConsensus()))
            return error("VerifyDB(): *** ReadBlockFromDisk failed at %d, hash=%s", pindex->nHeight, pindex->GetBlockHash().ToString());
        // check level 1: verify block validity
        if (nCheckLevel >= 1 && !CheckBlock(block, state))
            return error("VerifyDB(): *** found bad block at %d, hash=%s\n", pindex->nHeight, pindex->GetBlockHash().ToString());
        // check level 2: verify undo validity
        if (nCheckLevel >= 2 && pindex) {
            CBlockUndo undo;
            CDiskBlockPos pos = pindex->GetUndoPos();
            if (!pos.IsNull()) {
                if (!UndoReadFromDisk(undo, pos, pindex->pprev->GetBlockHash()))
                    return error("VerifyDB(): *** found bad undo data at %d, hash=%s\n", pindex->nHeight, pindex->GetBlockHash().ToString());
            }
        }
        // check level 3: check for inconsistencies during memory-only disconnect of tip blocks
        if (nCheckLevel >= 3 && pindex == pindexState && (coins.DynamicMemoryUsage() + pcoinsTip->DynamicMemoryUsage()) <= nCoinCacheUsage) {
            bool fClean = true;
            if (!DisconnectBlock(block, state, pindex, coins, &fClean))
                return error("VerifyDB(): *** irrecoverable inconsistency in block data at %d, hash=%s", pindex->nHeight, pindex->GetBlockHash().ToString());
            pindexState = pindex->pprev;
            if (!fClean) {
                nGoodTransactions = 0;
                pindexFailure = pindex;
            } else
                nGoodTransactions += block.vtx.size();
        }
        if (ShutdownRequested())
            return true;
    }
    if (pindexFailure)
        return error("VerifyDB(): *** coin database inconsistencies found (last %i blocks, %i good transactions before that)\n", chainActive.Height() - pindexFailure->nHeight + 1, nGoodTransactions);

    // check level 4: try reconnecting blocks
    if (nCheckLevel >= 4) {
        CBlockIndex *pindex = pindexState;
        while (pindex != chainActive.Tip()) {
            boost::this_thread::interruption_point();
            uiInterface.ShowProgress(_("Verifying blocks..."), std::max(1, std::min(99, 100 - (int)(((double)(chainActive.Height() - pindex->nHeight)) / (double)nCheckDepth * 50))));
            pindex = chainActive.Next(pindex);
            CBlock block;
            if (!ReadBlockFromDisk(block, pindex, chainparams.GetConsensus()))
                return error("VerifyDB(): *** ReadBlockFromDisk failed at %d, hash=%s", pindex->nHeight, pindex->GetBlockHash().ToString());
            if (!ConnectBlock(block, state, pindex, coins))
                return error("VerifyDB(): *** found unconnectable block at %d, hash=%s", pindex->nHeight, pindex->GetBlockHash().ToString());
        }
    }

    LogPrintf("No coin database inconsistencies in last %i blocks (%i transactions)\n", chainActive.Height() - pindexState->nHeight, nGoodTransactions);

    return true;
}
```

## (P1) Need to manage set of UTXO
To be able to check if a coin pointed by an input is unspent, 
we need to maintain the set of UTXO(Unspent transaction output)s.

### src/main.cpp
```
/** Apply the effects of this transaction on the UTXO set represented by view */
void UpdateCoins(const CTransaction& tx, CValidationState &state, CCoinsViewCache &inputs, int nHeight);
{
}
```

## (P1) Detect hard fork  
detects a hard fork by looking at block chain proof of work. 
If a non-upgraded node receives block chain headers 
demonstrating at least six blocks more proof of work than the best chain it considers valid, 
the node reports an error in the getinfo RPC results and runs the -alertnotify command if set. 

### src/main.cpp
Following code checks if a long fork exists, and notifies an alert if a long fork was found.
```
void CheckForkWarningConditions()
{
    AssertLockHeld(cs_main);
    // Before we get past initial download, we cannot reliably alert about forks
    // (we assume we don't get stuck on a fork before the last checkpoint)
    if (IsInitialBlockDownload())
        return;

    // If our best fork is no longer within 72 blocks (+/- 12 hours if no one mines it)
    // of our head, drop it
    if (pindexBestForkTip && chainActive.Height() - pindexBestForkTip->nHeight >= 72)
        pindexBestForkTip = NULL;

    if (pindexBestForkTip || 
        (pindexBestInvalid && pindexBestInvalid->nChainWork > 
                              chainActive.Tip()->nChainWork + (GetBlockProof(*chainActive.Tip()) * 6)))
    {
        if (!fLargeWorkForkFound && pindexBestForkBase)
        {
            std::string warning = std::string("'Warning: Large-work fork detected, forking after block ") +
                pindexBestForkBase->phashBlock->ToString() + std::string("'");
            CAlert::Notify(warning, true);
        }
        if (pindexBestForkTip && pindexBestForkBase)
        {
            LogPrintf("%s: Warning: Large valid fork found\n  forking the chain at height %d (%s)\n  lasting to height %d (%s).\nChain state database corruption likely.\n", __func__,
                   pindexBestForkBase->nHeight, pindexBestForkBase->phashBlock->ToString(),
                   pindexBestForkTip->nHeight, pindexBestForkTip->phashBlock->ToString());
            fLargeWorkForkFound = true;
        }
        else
        {
            LogPrintf("%s: Warning: Found invalid chain at least ~6 blocks longer than our best chain.\nChain state database corruption likely.\n", __func__);
            fLargeWorkInvalidChainFound = true;
        }
    }
    else
    {
        fLargeWorkForkFound = false;
        fLargeWorkInvalidChainFound = false;
    }
}

void CheckForkWarningConditionsOnNewFork(CBlockIndex* pindexNewForkTip)
{
    AssertLockHeld(cs_main);
    // If we are on a fork that is sufficiently large, set a warning flag
    CBlockIndex* pfork = pindexNewForkTip;
    CBlockIndex* plonger = chainActive.Tip();
    while (pfork && pfork != plonger)
    {
        while (plonger && plonger->nHeight > pfork->nHeight)
            plonger = plonger->pprev;
        if (pfork == plonger)
            break;
        pfork = pfork->pprev;
    }

    // We define a condition where we should warn the user about as a fork of at least 7 blocks
    // with a tip within 72 blocks (+/- 12 hours if no one mines it) of ours
    // We use 7 blocks rather arbitrarily as it represents just under 10% of sustained network
    // hash rate operating on the fork.
    // or a chain that is entirely longer than ours and invalid (note that this should be detected by both)
    // We define it this way because it allows us to only store the highest fork tip (+ base) which meets
    // the 7-block condition and from this always have the most-likely-to-cause-warning fork
    if (pfork && (!pindexBestForkTip || (pindexBestForkTip && pindexNewForkTip->nHeight > pindexBestForkTip->nHeight)) &&
            pindexNewForkTip->nChainWork - pfork->nChainWork > (GetBlockProof(*pfork) * 7) &&
            chainActive.Height() - pindexNewForkTip->nHeight < 72)
    {
        pindexBestForkTip = pindexNewForkTip;
        pindexBestForkBase = pfork;
    }

    CheckForkWarningConditions();
}
```

### src/chain.cpp
Code that finds a fork. Looks like it is not finding a hard fork though.
```
const CBlockIndex *CChain::FindFork(const CBlockIndex *pindex) const {
    if (pindex == NULL) {
        return NULL;
    }
    if (pindex->nHeight > Height())
        pindex = pindex->GetAncestor(Height());
    while (pindex && !Contains(pindex))
        pindex = pindex->pprev;
    return pindex;
}
```

## (P1) prefix coinbase data with block height
### src/miner.cpp
We need to prefix the coinbase data with the block height.
Need investigation : What is extra nonce in the coinbase data?
```
void IncrementExtraNonce(CBlock* pblock, const CBlockIndex* pindexPrev, unsigned int& nExtraNonce)
{
    // Update nExtraNonce
    static uint256 hashPrevBlock;
    if (hashPrevBlock != pblock->hashPrevBlock)
    {
        nExtraNonce = 0;
        hashPrevBlock = pblock->hashPrevBlock;
    }
    ++nExtraNonce;
    unsigned int nHeight = pindexPrev->nHeight+1; // Height first in coinbase required for block.version=2
    CMutableTransaction txCoinbase(pblock->vtx[0]);
    txCoinbase.vin[0].scriptSig = (CScript() << nHeight << CScriptNum(nExtraNonce)) + COINBASE_FLAGS;
    assert(txCoinbase.vin[0].scriptSig.size() <= 100);

    pblock->vtx[0] = txCoinbase;
    pblock->hashMerkleRoot = BlockMerkleRoot(*pblock);
}
```

The IncrementExtraNonce is called from BitcoinMiner.
```
void static BitcoinMiner(const CChainParams& chainparams)
{
    ...

            //
            // Create new block
            //
            unsigned int nTransactionsUpdatedLast = mempool.GetTransactionsUpdated();
            CBlockIndex* pindexPrev = chainActive.Tip();

            auto_ptr<CBlockTemplate> pblocktemplate(CreateNewBlock(chainparams, coinbaseScript->reserveScript));
            if (!pblocktemplate.get())
            {
                LogPrintf("Error in BitcoinMiner: Keypool ran out, please call keypoolrefill before restarting the mining thread\n");
                return;
            }
            CBlock *pblock = &pblocktemplate->block;
            IncrementExtraNonce(pblock, pindexPrev, nExtraNonce);
    ...
}
```

## (P1) check coinbase maturity
Need to check if the coinbase output used by an input of a transaction is confirmed by more than 100 blocks.

### src/main.cpp
```
namespace Consensus {
bool CheckTxInputs(const CTransaction& tx, CValidationState& state, const CCoinsViewCache& inputs, int nSpendHeight)
{
        ...

        CAmount nValueIn = 0;
        CAmount nFees = 0;
        for (unsigned int i = 0; i < tx.vin.size(); i++)
        {
            const COutPoint &prevout = tx.vin[i].prevout;
            const CCoins *coins = inputs.AccessCoins(prevout.hash);
            assert(coins);

            // If prev is coinbase, check that it's matured
            if (coins->IsCoinBase()) {
                if (nSpendHeight - coins->nHeight < COINBASE_MATURITY)
                    return state.Invalid(false,
                        REJECT_INVALID, "bad-txns-premature-spend-of-coinbase",
                        strprintf("tried to spend coinbase at depth %d", nSpendHeight - coins->nHeight));
            }
        ...
```

## (P1) check if a transaction is a standard one 
https://bitcoin.org/en/developer-guide#non-standard-transactions

### src/policy/policy.h
```
/**
 * Standard script verification flags that standard transactions will comply
 * with. However scripts violating these flags may still be present in valid
 * blocks and we must accept those blocks.
 */
static const unsigned int STANDARD_SCRIPT_VERIFY_FLAGS = MANDATORY_SCRIPT_VERIFY_FLAGS |
                                                         SCRIPT_VERIFY_DERSIG |
                                                         SCRIPT_VERIFY_STRICTENC |
                                                         SCRIPT_VERIFY_MINIMALDATA |
                                                         SCRIPT_VERIFY_NULLDUMMY |
                                                         SCRIPT_VERIFY_DISCOURAGE_UPGRADABLE_NOPS |
                                                         SCRIPT_VERIFY_CLEANSTACK |
                                                         SCRIPT_VERIFY_CHECKLOCKTIMEVERIFY |
                                                         SCRIPT_VERIFY_LOW_S;

/** For convenience, standard but not mandatory verify flags. */
static const unsigned int STANDARD_NOT_MANDATORY_VERIFY_FLAGS = STANDARD_SCRIPT_VERIFY_FLAGS & ~MANDATORY_SCRIPT_VERIFY_FLAGS;

/** Used as the flags parameter to CheckFinalTx() in non-consensus code */
static const unsigned int STANDARD_LOCKTIME_VERIFY_FLAGS = LOCKTIME_MEDIAN_TIME_PAST;

bool IsStandard(const CScript& scriptPubKey, txnouttype& whichType);
    /**
     * Check for standard transaction types
     * @return True if all outputs (scriptPubKeys) use only standard transaction forms
     */
bool IsStandardTx(const CTransaction& tx, std::string& reason);
    /**
     * Check for standard transaction types
     * @param[in] mapInputs    Map of previous transactions that have outputs we're spending
     * @return True if all inputs (scriptSigs) use only standard transaction forms
     */
bool AreInputsStandard(const CTransaction& tx, const CCoinsViewCache& mapInputs);
```

## (P1) Check transaction fee 
https://bitcoin.org/en/developer-guide#transaction-fees-and-change

### src/main.cpp
Need to check if the sum(inputs) >= sum(outputs). 
Also need to check if the amount specified by inputs and outputs are in valid range by using MoneyRange.
```
namespace Consensus {
bool CheckTxInputs(const CTransaction& tx, CValidationState& state, const CCoinsViewCache& inputs, int nSpendHeight)
{
        // This doesn't trigger the DoS code on purpose; if it did, it would make it easier
        // for an attacker to attempt to split the network.
        if (!inputs.HaveInputs(tx))
            return state.Invalid(false, 0, "", "Inputs unavailable");

        CAmount nValueIn = 0;
        CAmount nFees = 0;
        for (unsigned int i = 0; i < tx.vin.size(); i++)
        {
            const COutPoint &prevout = tx.vin[i].prevout;
            const CCoins *coins = inputs.AccessCoins(prevout.hash);
            assert(coins);

            // If prev is coinbase, check that it's matured
            if (coins->IsCoinBase()) {
                if (nSpendHeight - coins->nHeight < COINBASE_MATURITY)
                    return state.Invalid(false,
                        REJECT_INVALID, "bad-txns-premature-spend-of-coinbase",
                        strprintf("tried to spend coinbase at depth %d", nSpendHeight - coins->nHeight));
            }

            // Check for negative or overflow input values
            nValueIn += coins->vout[prevout.n].nValue;
            if (!MoneyRange(coins->vout[prevout.n].nValue) || !MoneyRange(nValueIn))
                return state.DoS(100, false, REJECT_INVALID, "bad-txns-inputvalues-outofrange");

        }

        if (nValueIn < tx.GetValueOut())
            return state.DoS(100, false, REJECT_INVALID, "bad-txns-in-belowout", false,
                strprintf("value in (%s) < value out (%s)", FormatMoney(nValueIn), FormatMoney(tx.GetValueOut())));

        // Tally transaction fees
        CAmount nTxFee = nValueIn - tx.GetValueOut();
        if (nTxFee < 0)
            return state.DoS(100, false, REJECT_INVALID, "bad-txns-fee-negative");
        nFees += nTxFee;
        if (!MoneyRange(nFees))
            return state.DoS(100, false, REJECT_INVALID, "bad-txns-fee-outofrange");
    return true;
}
}// namespace Consensus
```

## (P1) Do IBD if blocks are 1 day or 144 blocks behind
https://bitcoin.org/en/developer-guide#initial-block-download

### src/main.cpp
```
/* If the tip is older than this (in seconds), the node is considered to be in initial block download.
 */
int64_t nMaxTipAge = DEFAULT_MAX_TIP_AGE;

...

bool IsInitialBlockDownload()
{
    const CChainParams& chainParams = Params();
    LOCK(cs_main);
    if (fImporting || fReindex)
        return true;
    if (fCheckpointsEnabled && chainActive.Height() < Checkpoints::GetTotalBlocksEstimate(chainParams.Checkpoints()))
        return true;
    static bool lockIBDState = false;
    if (lockIBDState)
        return false;
    bool state = (chainActive.Height() < pindexBestHeader->nHeight - 24 * 6 ||
            pindexBestHeader->GetBlockTime() < GetTime() - nMaxTipAge);
    if (!state)
        lockIBDState = true;
    return state;
}
```

## (P1) getheaders : send block headers from the first matching block header hash in getheaders message
This was not explicitly mentioned in the Bitcoin developer's guide, 
but the getheaders service should respond with the blocks on the best blockchain. 

why? for fork detection.

## (P1) Add Getblocktemplate RPC 
Their mining software periodically polls bitcoind for new transactions 
using the getblocktemplate RPC, which provides the list of new transactions plus 
the public key to which the coinbase transaction should be sent.

## (P1) Add Getblockhash RPC
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

## (P2) Add OP_CHECKLOCKTIMEVERIFY
### src/script/interpreter.cpp
The OP_CHECKLOCKTIMEVERIFY is a newly implemented script operation. We need to implement it as well.
```
bool EvalScript(vector<vector<unsigned char> >& stack, const CScript& script, unsigned int flags, const BaseSignatureChecker& checker, ScriptError* serror)
                case OP_CHECKLOCKTIMEVERIFY:
                {
                    if (!(flags & SCRIPT_VERIFY_CHECKLOCKTIMEVERIFY)) {
                        // not enabled; treat as a NOP2
                        if (flags & SCRIPT_VERIFY_DISCOURAGE_UPGRADABLE_NOPS) {
                            return set_error(serror, SCRIPT_ERR_DISCOURAGE_UPGRADABLE_NOPS);
                        }
                        break;
                    }

                    if (stack.size() < 1)
                        return set_error(serror, SCRIPT_ERR_INVALID_STACK_OPERATION);

                    // Note that elsewhere numeric opcodes are limited to
                    // operands in the range -2**31+1 to 2**31-1, however it is
                    // legal for opcodes to produce results exceeding that
                    // range. This limitation is implemented by CScriptNum's
                    // default 4-byte limit.
                    //
                    // If we kept to that limit we'd have a year 2038 problem,
                    // even though the nLockTime field in transactions
                    // themselves is uint32 which only becomes meaningless
                    // after the year 2106.
                    //
                    // Thus as a special case we tell CScriptNum to accept up
                    // to 5-byte bignums, which are good until 2**39-1, well
                    // beyond the 2**32-1 limit of the nLockTime field itself.
                    const CScriptNum nLockTime(stacktop(-1), fRequireMinimal, 5);

                    // In the rare event that the argument may be < 0 due to
                    // some arithmetic being done first, you can always use
                    // 0 MAX CHECKLOCKTIMEVERIFY.
                    if (nLockTime < 0)
                        return set_error(serror, SCRIPT_ERR_NEGATIVE_LOCKTIME);

                    // Actually compare the specified lock time with the transaction.
                    if (!checker.CheckLockTime(nLockTime))
                        return set_error(serror, SCRIPT_ERR_UNSATISFIED_LOCKTIME);

                    break;
                }

```


## (P2) Ignore getheaders if IBD is in progress.
### src/main.cpp
```
bool static ProcessMessage(CNode* pfrom, string strCommand, CDataStream& vRecv, int64_t nTimeReceived) {
    else if (strCommand == NetMsgType::GETHEADERS) {
        if (IsInitialBlockDownload() && !pfrom->fWhitelisted) {
            LogPrint("net", "Ignoring getheaders from peer=%d because node is in initial block download\n", pfrom->id);
            return true;
        }

```

## (P2) Advertise address upon receival of Version If not in IBD process.

### src/main.cpp
```
bool static ProcessMessage(CNode* pfrom, string strCommand, CDataStream& vRecv, int64_t nTimeReceived) {

    if (strCommand == NetMsgType::VERSION) {
    
        // Advertise our address
        if (fListen && !IsInitialBlockDownload())
        {
            CAddress addr = GetLocalAddress(&pfrom->addr);
            if (addr.IsRoutable())
            {
                LogPrintf("ProcessMessages: advertizing address %s\n", addr.ToString());
                pfrom->PushAddress(addr);
            } else if (IsPeerAddrLocalGood(pfrom)) {
                addr.SetIP(pfrom->addrLocal);
                LogPrintf("ProcessMessages: advertizing address %s\n", addr.ToString());
                pfrom->PushAddress(addr);
            }
        }

```

## (P2) Implement -alertnotify

## (P2) Add block chain checkpoints
To know if the blocks received by IBD are on best blockchain as early as possible. 
https://bitcoin.org/en/developer-guide#blocks-first

## (P2) Get headers from multiple peers to get the best blockchain 
It sends a getheaders message to each of its outboundpeers 
to get their view of best header chain, after receiving all block headers from a sync node. 
https://bitcoin.org/en/developer-guide#headers-first

## (P2) headers-first : parallel block download.
Request up to 16 blocks at a time from a single peer. 
Combined with its maximum of 8 outbound connections.
https://bitcoin.org/en/developer-guide#headers-first

## (P2) Parallel download using download window 
Uses a 1,024-block moving download window to maximize download speed

## (P2) Ban a peer based on a banscore.
If a peer gets a banscore above the -banscore=<n> threshold, 
he will be banned for the number of seconds defined by -bantime=<n>, 
which is 86,400 by default (24 hours). 

https://bitcoin.org/en/developer-guide#misbehaving-nodes

## (CUT) store peer IPs and ports on disk 
Why cut? We will have peer IPs and ports on our configuration file, 
managed by the admin of the private blockchain.

https://bitcoin.org/en/developer-guide#peer-discovery

## (CUT) get blocks : if no hash matches, send 500 blocks from block1 in Inv.
Why cut? We are using headers-first IBD. This is all about blocks-first IBD.
https://bitcoin.org/en/developer-guide#blocks-first 

## (CUT) get blocks : send blocks from the first matching block header hash in get blocks message
Why cut? We are using headers-first IBD. This is all about blocks-first IBD.
We need to match with blocks in the best blockchain. 
why? fork detection https://bitcoin.org/en/developer-guide#blocks-first

## (CUT) blocks-first : connect orphan blocks when a parent was received.
Once the parent of the former orphan block has been validated, 
it will validate the former orphan block. 
https://bitcoin.org/en/developer-guide#orphan-blocks

## (CUT) blocks-first : Keep orphan blocks in memory. 
Orphan blocks are stored in memory while they await validation, 
which may lead to high memory use.  https://bitcoin.org/en/developer-guide#blocks-first

## (Done) Discard orphan blocks while headers-first IBD was running.
headers-first : each of those headers will point to its parent, 
so when the downloading node receives the block message, 
the block shouldn’t be an orphan block. 
If, despite this, the block received in the block message is an orphan block, 
a headers-first node will discard it immediately.
