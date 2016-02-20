# Introduction

## Ledger ; openchain/ledger/ledger.go

### GetStateDelta

// GetStateDelta will return the state delta for the specified block if
// available.
func (ledger *Ledger) GetStateDelta(blockNumber uint64) (*statemgmt.StateDelta, error) {
	if blockNumber >= ledger.GetBlockchainSize() {
		return nil, ErrOutOfBounds
	}
	return ledger.state.FetchStateDeltaFromDB(blockNumber)
}

### ApplyStateDelta  
```
// ApplyStateDelta applies a state delta to the current state. This is an
// in memory change only. You must call ledger.CommitStateDelta to persist
// the change to the DB.
// This should only be used as part of state synchronization. State deltas
// can be retrieved from another peer though the Ledger.GetStateDelta function
// or by creating state deltas with keys retrieved from
// Ledger.GetStateSnapshot(). For an example, see TestSetRawState in
// ledger_test.go
// Note that there is no order checking in this function and it is up to
// the caller to ensure that deltas are applied in the correct order.
// For example, if you are currently at block 8 and call this function
// with a delta retrieved from Ledger.GetStateDelta(10), you would now
// be in a bad state because you did not apply the delta for block 9.
// It's possible to roll the state forwards or backwards using
// stateDelta.RollBackwards. By default, a delta retrieved for block 3 can
// be used to roll forwards from state at block 2 to state at block 3. If
// stateDelta.RollBackwards=false, the delta retrived for block 3 can be
// used to roll backwards from the state at block 3 to the state at block 2.
func (ledger *Ledger) ApplyStateDelta(id interface{}, delta *statemgmt.StateDelta) error {
	err := ledger.checkValidIDBegin()
	if err != nil {
		return err
	}
	ledger.currentID = id
	ledger.state.ApplyStateDelta(delta)
	return nil
}

```


### CommitStateDelta
```
// CommitStateDelta will commit the state delta passed to ledger.ApplyStateDelta
// to the DB
func (ledger *Ledger) CommitStateDelta(id interface{}) error {
	err := ledger.checkValidIDCommitORRollback(id)
	if err != nil {
		return err
	}
	defer ledger.resetForNextTxGroup(true)
	return ledger.state.CommitStateDelta()
}
```

### RollbackStateDelta

```
// RollbackStateDelta will discard the state delta passed
// to ledger.ApplyStateDelta
func (ledger *Ledger) RollbackStateDelta(id interface{}) error {
	err := ledger.checkValidIDCommitORRollback(id)
	if err != nil {
		return err
	}
	ledger.resetForNextTxGroup(false)
	return nil
}
```


### GetRemoteStateSnapshot
```

```

### GetStateSnapshot
```
// GetStateSnapshot returns a point-in-time view of the global state for the current block. This
// should be used when transfering the state from one peer to another peer. You must call
// stateSnapshot.Release() once you are done with the snapsnot to free up resources.
func (ledger *Ledger) GetStateSnapshot() (*state.StateSnapshot, error) {
	dbSnapshot := db.GetDBHandle().GetSnapshot()
	blockNumber, err := fetchBlockchainSizeFromSnapshot(dbSnapshot)
	if err != nil {
		dbSnapshot.Release()
		return nil, err
	}
	return ledger.state.GetSnapshot(blockNumber, dbSnapshot)
}
```




## State ; openchain/ledger/statemgmt/state/state.go
### GetSnapshot
```
// GetSnapshot returns a snapshot of the global state for the current block. stateSnapshot.Release()
// must be called once you are done.
func (state *State) GetSnapshot(blockNumber uint64, dbSnapshot *gorocksdb.Snapshot) (*StateSnapshot, error) {
	return newStateSnapshot(blockNumber, dbSnapshot)
}

```
### GetStateDelta
```
// getStateDelta get changes in state after most recent call to method clearInMemoryChanges
func (state *State) getStateDelta() *statemgmt.StateDelta {
	return state.stateDelta
}
```

## StateSnapshot ; openchain/ledger/statemgmt/state/state_snapshot.go
### StateSnapshot
```
type StateSnapshot struct {
	blockNumber  uint64
	stateImplItr statemgmt.StateSnapshotIterator
	dbSnapshot   *gorocksdb.Snapshot
}
```

### newStateSnapshot
// newStateSnapshot creates a new snapshot of the global state for the current block.
func newStateSnapshot(blockNumber uint64, dbSnapshot *gorocksdb.Snapshot) (*StateSnapshot, error) {
	itr, err := stateImpl.GetStateSnapshotIterator(dbSnapshot)
	if err != nil {
		return nil, err
	}
	snapshot := &StateSnapshot{blockNumber, itr, dbSnapshot}
	return snapshot, nil
}


## StateTrie ; openchain/ledger/statemgmt/trie/state_trie.go
### Get
```
func (stateTrie *StateTrie) Get(chaincodeID string, key string) ([]byte, error) {
	trieNode, err := fetchTrieNodeFromDB(newTrieKey(chaincodeID, key))
	if err != nil {
		return nil, err
	}
	if trieNode == nil {
		return nil, nil
	}
	return trieNode.value, nil
}
```

## Database ; openchain/db/db.go
### database access functions
```
// GetFromBlockchainCFSnapshot get value for given key from column family in a DB snapshot - blockchainCF
func (openchainDB *OpenchainDB) GetFromBlockchainCFSnapshot(snapshot *gorocksdb.Snapshot, key []byte) ([]byte, error) {
	return openchainDB.getFromSnapshot(snapshot, openchainDB.BlockchainCF, key)
}


// GetSnapshot returns a point-in-time view of the DB. You MUST call snapshot.Release()
// when you are done with the snapshot.
func (openchainDB *OpenchainDB) GetSnapshot() *gorocksdb.Snapshot {
	return openchainDB.DB.NewSnapshot()
}

func (openchainDB *OpenchainDB) getSnapshotIterator(snapshot *gorocksdb.Snapshot, cfHandler *gorocksdb.ColumnFamilyHandle) *gorocksdb.Iterator {
	opt := gorocksdb.NewDefaultReadOptions()
	defer opt.Destroy()
	opt.SetSnapshot(snapshot)
	iter := openchainDB.DB.NewIteratorCF(opt, cfHandler)
	return iter
}
```

## Handler ; openchain/peer/handler.go
### sendBlock
```
// sendBlocks sends the blocks based upon the supplied SyncBlockRange over the stream.
func (d *Handler) sendBlocks(syncBlockRange *pb.SyncBlockRange) {
	peerLogger.Debug("Sending blocks %d-%d", syncBlockRange.Start, syncBlockRange.End)
	
	var blockNums []uint64
	
    for i := syncBlockRange.Start; i <= syncBlockRange.End; i++ {
        peerLogger.Debug("Appending to blockNums: %d", i)

        blockNums = append(blockNums, i)
    }

	for _, currBlockNum := range blockNums {
		// Get the Block from
		block, err := d.Coordinator.GetBlockByNumber(currBlockNum)

		// Encode a SyncBlocks into the payload
		syncBlocks := &pb.SyncBlocks{Range: &pb.SyncBlockRange{Start: currBlockNum, End: currBlockNum}, Blocks: []*pb.Block{block}}

		syncBlocksBytes, err := proto.Marshal(syncBlocks)
	}
}
```

### sendStateSnapshot
```
// sendBlocks sends the blocks based upon the supplied SyncBlockRange over the stream.
func (d *Handler) sendStateSnapshot(syncStateSnapshotRequest *pb.SyncStateSnapshotRequest) {
	peerLogger.Debug("Sending state snapshot with correlationId = %d", syncStateSnapshotRequest.CorrelationId)

	snapshot, err := d.Coordinator.GetStateSnapshot()

	defer snapshot.Release()

	// Iterate over the state deltas and send to requestor
	delta := statemgmt.NewStateDelta()
	
	currBlockNumber := snapshot.GetBlockNumber()
	
	var sequence uint64
	
	// Loop through and send the Deltas
	// 1, +1 => 2
	// 2, -1 => 1
	// 1, +10 => 11
	for i := 0; snapshot.Next(); i++ {
		k, v := snapshot.GetRawKeyValue()
		cID, kID := statemgmt.DecodeCompositeKey(k)
		delta.Set(cID, kID, v, nil)

		deltaAsBytes := delta.Marshal()
		// Encode a SyncStateSnapsot into the payload
		sequence = uint64(i)
		syncStateSnapshot := &pb.SyncStateSnapshot{Delta: deltaAsBytes, Sequence: sequence, BlockNumber: currBlockNumber, Request: syncStateSnapshotRequest}

		syncStateSnapshotBytes, err := proto.Marshal(syncStateSnapshot)
	}

	// Now send the terminating message
	syncStateSnapshot := &pb.SyncStateSnapshot{Delta: []byte{}, Sequence: sequence + 1, BlockNumber: currBlockNumber, Request: syncStateSnapshotRequest}

	syncStateSnapshotBytes, err := proto.Marshal(syncStateSnapshot)
}

```

## RocksDB - Snapshot

https://github.com/facebook/rocksdb/wiki/RocksDB-Basics



