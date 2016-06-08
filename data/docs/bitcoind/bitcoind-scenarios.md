#Introduction
Analyze how bitcoind works in different events such as finding out an orphan block, 
finding out an orphan transaction, reorganizing blocks upon a longer chain than the node has. 

# Overall flow of message
## (req) Version -> (rsp) Verack

## (req) Ping -> (rsp) Pong

## (req) GetHeaders -> (rsp) Headers
 
## (req) GetBlocks, Mempool -> (rsp) Inv -> (req) getdata -> (rsp) tx, block, merkleblock, notfound 

# Upon the arrival of "tx" message (in reply to "getdata")
## Data structure
```
// The mempool where transactions are stored in memory, searchable by the hash of it.
static map<uint256, CTransaction> mapTransactions;

// kangmo : comment - The list of transaction inputs spending a specific output. Searchable by an out point in an input of a transaction to check double-spends.
map<COutPoint, CInPoint> mapNextTx;

// kangmo : comment - orphan transactions by the hash of the orphan transaction.
map<uint256, CDataStream*> mapOrphanTransactions;

// kangmo : comment - orphan transactions by the transaction hash in an outpoint of it. Because multiple orphan transactions may exists for the same transaction hash of an outpoint, we need to use multimap.
multimap<uint256, CDataStream*> mapOrphanTransactionsByPrev;
```
## strCommand == "tx" in ProcessMessage in main.cpp
```
else if (strCommand == "tx") {
    // Step 0 : Add the inventory as a known inventory to the node that sent the "tx" message.
    pfrom->AddInventoryKnown(tx inventory)

    if (tx.AcceptToMemoryPool(true, &fMissingInputs)) // Not an orphan 
    {
        // Step 1 : Notify the transaction, check if the transaction should be stored in the wallet.
        SyncWithWallets
        
        // Step 2 : Relay the transaction as an inventory 
        RelayMessage 
          - RelayInventory // For each connected node, relay the transaction inventory.
          
        // Step 3 : Recursively check if any orphan transaction depends on this transaction.
        Loop newTx := each transaction newly added 
          Loop orphanTx := for each transaction that depends on the newTx
            if (orphanTx.AcceptToMemoryPool(true)) { // Not an orphan anymore
              add the tx to the newly added transactions list.
        
        // Step 4 : For each orphan transaction that has all inputs connected, remove from the orphan transaction.
        Loop newTx := each transaction newly added 
          EraseOrphanTx(hash);
            - Remove the orphan transaction both from mapOrphanTransactions and mapOrphanTransactionsByPrev.
    }
    else if (fMissingInputs) // An orphan
    {                
        // Add the transaction as an orphan transaction.
        AddOrphanTx(vMsg);
        - Add the orphan transaction to mapOrphanTransactions and mapOrphanTransactionsByPrev.
    }
}
```
## CTransaction::AcceptToMemoryPool(CTxDB& txdb, bool fCheckInputs, bool* pfMissingInputs)
```
    // Step 01 : CheckTransaction - check values in the transaction.
    
    // Step 02 : IsCoinBase - the transaction should not be a coinbase transaction. No coinbase transaction is put into the mempool.
    
    // Step 03 : GetSerializeSize - Check the serialized size 
    
    // Step 04 : GetSigOpCount - Check the script operation count.
    
    // Step 05 : IsStandard - Check if the transaction is a standard one.
    
    // Step 06 : mapTransactions.count(hash) - check if the transaction exists in the mempool.
    
    // Step 07 : txdb.ContainsTx(hash) - check if the transaction exists in the persistent transaction database.
    
    // Step 08 : Check for conflicts with in-memory transactions.
    LOOP in := for each input
        Using mapNextTx, check if any other transaction in the mempool spends the same output pointed by the outpoint of the input.
        If there is any, do not accept the transaction into the mempool. The function returns.
    
    // Step 09 : Check against previous transactions.
    - ConnectInputs - Check if there is any missing input.
    - Check the transaction fee.
    - Check if we should allow the transaction.
    
    // Step 10 : DEAD CODE : ptxOld->RemoveFromMemoryPool() - Remove a conflicting transaction with the same hash if exists.
    // c.f. ptxOld is always NULL, as the replacement feature is disabled.
    
    // Step 11 : Add to the mempool.
    AddToMemoryPoolUnchecked()
        - mapTransactions[txHash] = transaction
        - LOOP in := for each input 
            // Keep this transaction as a spending transaction of the specific output pointed by the outpoint.
            mapNextTx[in.outPoint] := CInPoint(transaction, inputIndex)
            
    // Step 12 : EraseFromWallets(ptxOld->GetHash()) - Erase the transaction from the mempool.
    
```


# Upon the arrival of the "block" message (in reply to "getdata" or block submission by a miner)
## Data structure
```
// kangmo : comment - The in-memory structure of the double linked blocks, searchable by the block hash.
map<uint256, CBlockIndex*> mapBlockIndex;

// kangmo : comment - orphan blocks by the hash of the orphan block.
map<uint256, CBlock*> mapOrphanBlocks;

// kangmo : comment - orphan blocks by the previous hash of it. Because multiple orphan blocks may have the same 'previous hash', we need to use multimap.
multimap<uint256, CBlock*> mapOrphanBlocksByPrev;
```

## strCommand == "block" in ProcessMessage in main.cpp
```
else if (strCommand == "block") {
    // Step 1 : Add the block as a known inventory of the node that sent it. 
    pfrom->AddInventoryKnown(inv);
    
    // Step 2 : Process the block
    ProcessBlock(pfrom, &block)
        - Step 1 : check if we already have the block by looking up mapBlockIndex and mapOrphanBlocks
        - Step 2 : Do preliminary checks for a block
        pblock->CheckBlock()
            - 1. check the serialized block size.
            - 2. check the proof of work - block hash vs target hash
            - 3. check the block timestamp.
            - 4. check the first transaction is coinbase, and others are not.
            - 5. check each transaction in a block.
            - 6. check the number of script operations on the locking/unlocking script.
            - 7. Calculate the merkle root hash, compare it with the one in the block header.
        
        - Step 3 : Keep the orphan block in memory if the previous block does not exist.
        if (!mapBlockIndex.count(pblock->hashPrevBlock))
            - Insert the block into mapOrphanBlocks, mapOrphanBlocksByPrev
            - pfrom->PushGetBlocks(pindexBest, GetOrphanRoot(pblock2)); // Ask for the missing parent of the orphan block.
        
        - Step 4 : Store the block to the blockchain if the previous block exists.
        pblock->AcceptBlock()
            - 1. Need to check if the same blockheader hash exists by looking up mapBlockIndex
            - 2. Need to increase DoS score if an orphan block was received.
            - 3. Need to increase DoS score if the block hash does not meet the required difficulty.
            - 4. Need to get the median timestamp for the past N blocks.
            - 5. Need to check the lock time of all transactions.
            - 6. Need to check block hashes for checkpoint blocks.
            - 7. Write the block on the block database as well as in-memory index for blocks.
            WriteToDisk(nFile, nBlockPos)
            AddToBlockIndex(nFile, nBlockPos) // Block reorganization happens here.
            - 8. relay the new block to peers as an inventory if it is the tip of the longest block chain.
        
        - Step 5 : Recursively bring any orphan blocks to blockchain, if the new block is the previous block of any orphan block.
        newly_added_blocks = List(block hash)
        LOOP newBlock := For each newly_added_blocks
            LOOP orphanBlock := For each orphan block which depends on the new Block as the parent of it
                // Store the block into the blockchain database.
                if (orphanBlock->AcceptBlock()) 
                    newly_added_blocks += orphanBlock.hash              
                remove the orphanBlock from mapOrphanBlocks
            remove all orphan blocks depending on newBlock from mapOrphanBlocksByPrev
}
```

# Block Reorganization (while processing "block" message)
## Data structure
```
// check if a transaction output is spent
class CTxIndex
{
public:
    CDiskTxPos pos; // the disk location of a transaction
    std::vector<CDiskTxPos> vSpent; // the locations of transactions that spend its outputs
    ...
}

// uint256 : The hash of the transaction to track whether the outputs are spent.
// CTxIndex : keeps the transaction location on disk and transaction locations that spends the outputs.
map<uint256, CTxIndex> mapQueuedChanges;

```
## start point : CBlock::AddToBlockIndex
```
bool CBlock::AddToBlockIndex(unsigned int nFile, unsigned int nBlockPos) {
	// Step 1 : Check if the block already exists in the in-memory block index, mapBlockIndex

	// Step 2 : Put into the the in-memory block index, mapBlockIndex

	// Step 3 : Set the height and chain work of the new block

	// Step 4 : Put the in-memory block index into disk. 

    // Step 5 : based on the chain work, set the best blockchain. Reorganize blocks if necessary.
    if (pindexNew->bnChainWork > bnBestChainWork)
        if (!SetBestChain(txdb, pindexNew))
            // 1. If it is the genesis block, set the genesis block as the current best block.
            // 2. If the previous block is the current best block, add a new block on top of the current best block.
            // 3. Otherwise, Reorganize the blockchain if the best block has to be changed.
            Reorganize(txdb, pindexNew)
                // Step 1 : Find the common block(pfork) between the current blockchain(pindexBest) and the new longer blockchain.
            
                // Step 2 : Get the list of blocks to disconnect from the common block to the tip of the current blockchain
            
                // Step 3 : Get the list of blocks to connect from the common block to the longer blockchain.
            
                // Step 4 : Reorder the list of blocks to connect so that the blocks with lower height come first.
            
                // Step 5 : Disconnect blocks from the current (shorter) blockchain. (order : newest to oldest)
                LOOP block := For each block to disconnect
                    // Step 5.1 : Read each block, and disconnect each block.
                    block.ReadFromDisk(pindex)
                    block.DisconnectBlock(txdb, pindex)
                        1. Mark all outputs spent by all inputs of all transactions in the block as unspent.
                        LOOP tx := For each transaction in the block
                            1.1. Mark outputs pointed by the inputs of the transaction unspent.
                            tx.DisconnectInputs
                                - LOOP input := For each input in the transaction
                                    - Get the transaction pointed by the input
                                    - On disk, Mark the output point by the input as spent                                    
                        2. On disk, disconnect from the previous block   
                            (previous block.next = null)
            
                    // Step 5.2 : Prepare transactions to add back to the mempool.
                }
            
                // Step 6 : Connect blocks from the longer blockchain to the current blockchain. (order : oldest to newest)
                LOOP block := For each block to connect
                    // kangmo : comment - Step 6.1 : Read block, connect the block to the current blockchain, which does not have the disconnected blocks.
                    block.ReadFromDisk(pindex)
                    block.ConnectBlock(txdb, pindex)
                        - 1. Do preliminary checks for a block
                        pblock->CheckBlock()
                        
                        - 2. Prepare a queue for database changes marking outputs spent by all inputs of all transactions in the block. 
                        map<uint256, CTxIndex> mapQueuedChanges;
                        
                        - 3. Populate mapQueuedChanges with transaction outputs marking which transactions are spending each of the outputs.
                        LOOP tx := For each transaction in the block
                            - 3.1 Mark transaction outputs pointed by inputs of this transaction spent.
                            IF not coinbase transaction
                                CTransaction::ConnectInputs( .. & mapQueuedChanges .. )
                                    LOOP input := for each input in the transaction
                                        // 1. read CTxIndex from disk if not read yet.
                                        // 2. read the transaction that the outpoint points from disk if not read yet.
                                        // 3. Increase DoS score if an invalid output index was found in a transaction input.
                                        // 4. check coinbase maturity for outpoints spent by a transaction.
                                        // 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
                                        // 6. check double spends for each OutPoint of each transaction input in a transaction.
                                        // 7. check value range of each input and sum of inputs.
                                        // 8. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output.

                                    // check if the sum of input values is greater than or equal to the sum of outputs.
                                    // make sure if the fee is not negative.
                                    // check the minimum transaction fee for each transaction.
                            // Add UTXO : set all outputs are unspent for the newly connected transaction.

                        - 4. For each items in mapQueuedChanges, write to disk.
                        - 5. Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.
                        - 6. On disk, connect the block from the previous block. (previous block.next = this block)
                    
                        - 7. For each transaction, sync with wallet.
                        LOOP tx := For each transaction in the block
                            SyncWithWallets
                                - For each registered wallet
                                     pwallet->AddToWalletIfInvolvingMe
                    // Step 6.2 : Prepare transactions to remove from the mempool
                }
            
                // Step 7 : Write the hash of the tip block on the best blockchain, commit the db transaction.
                        
                // Step 8 : Set the next block pointer for each connected block. Also, set next block pointer to null for each disconnected block.
                // Note : next pointers of in-memory block index nodes are modified after the on-disk transaction commits the on-disk version of the next pointers. 
            
                // Step 9 : add transactions in the disconnected blocks to the mempool.
                tx.AcceptToMemoryPool(txdb, false);
            
                // Step 10 : Remove transactions in the connected blocks from the mempool.
                tx.RemoveFromMemoryPool();
                               
            // 4. Update best block in wallet (so we can detect restored wallets)
            // 5. set the block as the tip of the best chain.
}
```

# Upon the arrival of "inv" message ( in reply to "getblocks" or "mempool"  )
```
else if (strCommand == "inv") {
    // Step 1 : Return an error if the number of inventories is more than 50,000
    if (vInv.size() > 50000) ...

    // Step 2 : Add the inventory as a known inventory to the node that sent the "inv" message.
    LOOP inv := for each inventory in the "inv" message
        CNode::AddInventoryKnown
            - Put to inventory to std::set<CInv> setInventoryKnown

    // Step 3 : Check if we already have it
    bool fAlreadyHave = AlreadyHave(txdb, inv);
        - Transaction : Check the transaction database, orphan transactions, and mempool
        - Block : Check the block database, orphan blocks,
         
    // Step 4 : If we don't have it yet, send "getdata" message to the peer that sent the "inv" message
    pfrom->AskFor(inv)
    
    // Step 5 : If we already have it and it is an orphan block, send request to get the root of the orphan block. 
    pfrom->PushGetBlocks(
        pindexBest, 
        GetOrphanRoot(mapOrphanBlocks[inv.hash])
            // Work back to the first block in the orphan chain
            - while (mapOrphanBlocks.count(pblock->hashPrevBlock))
                  pblock = mapOrphanBlocks[pblock->hashPrevBlock];
            - return pblock->GetHash(); // This is the block the node does not have yet.
    );
}

```

# Upon the arrival of "getdata" message
## Data structure
```
// In net.cpp ; a message to relay for an inventory.
map<CInv, CDataStream> mapRelay;
// In net.cpp ; to keep when an Inv first relayed. Bitcoind keeps them only for 15 minutes in mapRelay.
deque<pair<int64, CInv> > vRelayExpiration;
```
## strCommand == "getdata" in ProcessMessage in main.cpp
```
else if (strCommand == "getdata") {
    // Step 1 : Return an error if the number of inventories is greater than 50,000.
    // Step 2 : For each inventory, send data for it.
    LOOP inv := For each inventory in the "getdata" message 
        // 1. For a block hash : 
            1.1 read the block from disk to send the block message.
            1.2 Using hashContinue set by 'getblocks' when it reached at the count limit, let the peer learn about our best block hash if it requested the hashConitnue block hash.
        // 2. For a transaction hash : 
            send tx message only if it is in the relay memory. A 'tx' is put into the relay memory by sendfrom, sendtoaddress, sendmany RPC.        
            
}
```

# Upon the arrival of "getblocks" message
## Data structure
```
```
## strCommand == "getblocks" in ProcessMessage in main.cpp
```
else if (strCommand == "getblocks") {
    // Step 1 : Get the latest common block with the caller in the best blockchain.
    // Step 2 : Skip the common block, start sending Inv from the next block of the common block.
    // Step 3 : Send Inv until we hit the hashStop. The hashStop is not sent as an Inv.
    //          Stop sending Inv if we hit the count limit. 
    //          Investigate : Need to understand : GetDistanceBack returns the depth(in terms of the sender's blockchain) of the block that is in our main chain. It returns 0 if the tip of sender's branch is in our main chain. We will send up to 500 more blocks from the tip height of the sender's chain.

}
```

# Upon the arrival of "getheaders" message
"getheaders" is nearly identical to "getblocks", but it is different in three ways.
1. getheaders sends up to 2000 headers, whereas getblocks sends up to 500 invs
2. getheaders responds with "headers" message, whereas getblocks responds with "inv" message.
3. The "headers" message contains a list of (block header and a byte zero indicating 0 transactions), whereas "inv" message has the hash of the block header.
4. getheaders sends the hashStop, whereas getblocks does not send the hashStop.

## Data structure
```
```
## strCommand == "getheaders" in ProcessMessage in main.cpp
```
else if (strCommand == "getheaders") {
    // Step 1 : Get the latest common block with the caller in the best blockchain.
    // Step 2 : If no common block was found, send the hashStop
    // Step 3 : If any common block was found, send up to 2000 headers including the hashStop.
    //          Investigate : Need to understand : GetDistanceBack  
}
```


# Upon the arrival of "headers" message ( in reply to "getheaders" )
## Data structure
```
```
## strCommand == "headers" in ProcessMessage in main.cpp
```
// Bitcoin 0.5 did not have the handler of the "headers" message, but 0.10 had.
else if (strCommand == "headers") {
    // Step 1 : read block headers
    // Step 2 : Accept block headers.
    AcceptBlockHeader(header, state, &pindexLast)
        - Step 1 : Check if the block header already exists, return the block index of it if it already exists.
        - Step 2 : Check the proof of work and block timestamp.
        CheckBlockHeader(block, state)
        - Step 3 : Get the block index of the previous block.
        - Step 4 : Check proof of work, block timestamp, block checkpoint, block version based on majority of recent block versions.
        ContextualCheckBlockHeader(block, state, pindexPrev)
        - Step 5 : Add the new block as a block index.
        AddToBlockIndex(block)
        
    // Step 3 : Request next block headers using "getheaders" message
    //    TODO : Continue Investigation
}
```



# Upon the arrival of "version" message
## Data structure
```
```
## strCommand == "version" in ProcessMessage in main.cpp
```
else if (strCommand == "version") {
}
```

# Upon the arrival of "verack" message
## Data structure
```
```
## strCommand == "verack" in ProcessMessage in main.cpp
```
else if (strCommand == "verack") {
}
```


# Upon the arrival of "ping" message
## Data structure
```
```
## strCommand == "ping" in ProcessMessage in main.cpp
```
else if (strCommand == "ping") {
}
```



# Upon the arrival of "pong" message
## Data structure
```
```
## strCommand == "pong" in ProcessMessage in main.cpp
```
else if (strCommand == "pong") {
}
```

