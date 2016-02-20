## consensus.go ; openchain/consensus
```
// Executor is used to invoke transactions, potentially modifying the backing ledger
type Executor interface {
	BeginTxBatch(id interface{}) error
	ExecTxs(id interface{}, txs []*pb.Transaction) ([]byte, error)
	CommitTxBatch(id interface{}, metadata []byte) (*pb.Block, error)
	RollbackTxBatch(id interface{}) error
	PreviewCommitTxBatch(id interface{}, metadata []byte) (*pb.Block, error)
}

```



## State transfer ; openchain/consensus/statetransfer/statetransfer.go
```
func (sts *StateTransferState) attemptStateTransfer(currentStateBlockNumber *uint64, mark **syncMark, blockHReply **blockHashReply, blocksValid *bool) error {
	if !sts.stateValid {
		*currentStateBlockNumber, err = sts.syncStateSnapshot((*mark).blockNumber, (*mark).peerIDs)
    else 
      	*currentStateBlockNumber, err = sts.ledger.GetBlockchainSize()
   	}

    ...
}


// This function will retrieve the current state from a peer.
// Note that no state verification can occur yet, we must wait for the next checkpoint, so it is important
// not to consider this state as valid
func (sts *StateTransferState) syncStateSnapshot(minBlockNumber uint64, peerIDs []*protos.PeerID) (uint64, error) {
	ok := sts.tryOverPeers(peerIDs, func(peerID *protos.PeerID) error {

        ...

        // Get remote snapshot and apply delta to ledger
		stateChan, err := sts.ledger.GetRemoteStateSnapshot(peerID)

		for {
			select {
			case piece, ok := <-stateChan:
				umDelta := &statemgmt.StateDelta{}

				umDelta.Unmarshal(piece.Delta);
				
				sts.ledger.ApplyStateDelta(piece, umDelta)

				sts.ledger.CommitStateDelta(piece)
			}
		}

	})
}

```

### helper.go; openchain/consensus/helper

```
// Verify that the given signature is valid under the given replicaID's verification key
// If replicaID is nil, use this validator's verification key
// If the signature is valid, the function should return nil
func (h *Helper) Verify(replicaID *pb.PeerID, signature []byte, message []byte) error {
	if !h.secOn {
		logger.Debug("Security is disabled")
		return nil
	}

	logger.Debug("Verify message from: %v", replicaID.Name)
	_, network, err := h.GetNetworkInfo()
	if err != nil {
		return fmt.Errorf("Couldn't retrieve validating network's endpoints: %v", err)
	}

	// check that the sender is a valid replica
	// if so, call crypto verify() with that endpoint's pkiID
	for _, endpoint := range network {
		logger.Debug("Endpoint name: %v", endpoint.ID.Name)
		if *replicaID == *endpoint.ID {
			cryptoID := endpoint.PkiID
			return h.secHelper.Verify(cryptoID, signature, message)
		}
	}
	return fmt.Errorf("Could not verify message from %s (unknown peer)", replicaID.Name)
}
```