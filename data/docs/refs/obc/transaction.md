
## Execute ; openchain/chaincode/exectransaction.go
```
//Execute - execute transaction or a query
func Execute(ctxt context.Context, chain *ChaincodeSupport, t *pb.Transaction) ([]byte, error) {
	var err error

	// get a handle to ledger to mark the begin/finish of a tx
	ledger, ledgerErr := ledger.GetLedger()


	if secHelper := chain.getSecHelper(); nil != secHelper {
		var err error
		t, err = secHelper.TransactionPreExecution(t)
	}

	if t.Type == pb.Transaction_CHAINCODE_NEW {
		_, err := chain.DeployChaincode(ctxt, t)

		//launch and wait for ready
		markTxBegin(ledger, t)
		_, _, err = chain.LaunchChaincode(ctxt, t)

		markTxFinish(ledger, t, true)
	} else if t.Type == pb.Transaction_CHAINCODE_EXECUTE || t.Type == pb.Transaction_CHAINCODE_QUERY {
		//will launch if necessary (and wait for ready)
		cID, cMsg, err := chain.LaunchChaincode(ctxt, t)

		//this should work because it worked above...
		chaincode := cID.Name


		// TODO: Need to comment next line and uncomment call to getTimeout, when transaction blocks are being created
		timeout := time.Duration(30000) * time.Millisecond
		//timeout, err := getTimeout(cID)


		var ccMsg *pb.ChaincodeMessage
		if t.Type == pb.Transaction_CHAINCODE_EXECUTE {
			ccMsg, err = createTransactionMessage(t.Uuid, cMsg)
		} else {
			ccMsg, err = createQueryMessage(t.Uuid, cMsg)
		}

		markTxBegin(ledger, t)
		resp, err := chain.Execute(ctxt, chaincode, ccMsg, timeout, t)
		if err != nil {
			// Rollback transaction
			markTxFinish(ledger, t, false)
			//fmt.Printf("Got ERROR inside execute %s\n", err)
			return nil, fmt.Errorf("Failed to execute transaction or query(%s)", err)
		} else {
			if resp.Type == pb.ChaincodeMessage_COMPLETED || resp.Type == pb.ChaincodeMessage_QUERY_COMPLETED {
				// Success
				markTxFinish(ledger, t, true)
				return resp.Payload, nil
			} else if resp.Type == pb.ChaincodeMessage_ERROR || resp.Type == pb.ChaincodeMessage_QUERY_ERROR {
				// Rollback transaction
				markTxFinish(ledger, t, false)
				return nil, fmt.Errorf("Transaction or query returned with failure: %s", string(resp.Payload))
			}
			markTxFinish(ledger, t, false)
			return resp.Payload, fmt.Errorf("receive a response for (%s) but in invalid state(%d)", t.Uuid, resp.Type)
		}

	} else {
		err = fmt.Errorf("Invalid transaction type %s", t.Type.String())
	}
	return nil, err
}

```

## handler.go (openchain/chaincode/shim)

### enterTransactionState

```
// enterTransactionState will execute chaincode's Run if coming from a TRANSACTION event.
func (handler *Handler) enterTransactionState(e *fsm.Event) {
	msg, ok := e.Args[0].(*pb.ChaincodeMessage)
	if !ok {
		e.Cancel(fmt.Errorf("Received unexpected message type"))
		return
	}
	chaincodeLogger.Debug("[%s]Received %s, invoking transaction on chaincode(Src:%s, Dst:%s)", shortuuid(msg.Uuid), msg.Type.String(), e.Src, e.Dst)
	if msg.Type.String() == pb.ChaincodeMessage_TRANSACTION.String() {
		// Call the chaincode's Run function to invoke transaction
		handler.handleTransaction(msg)
	}
}
```

### handleTransaction
```
// handleTransaction Handles request to execute a transaction.
func (handler *Handler) handleTransaction(msg *pb.ChaincodeMessage) {
	// The defer followed by triggering a go routine dance is needed to ensure that the previous state transition
	// is completed before the next one is triggered. The previous state transition is deemed complete only when
	// the beforeInit function is exited. Interesting bug fix!!
	go func() {
		//better not be nil
		var nextStateMsg *pb.ChaincodeMessage

		send := true

		defer func() {
			handler.triggerNextState(nextStateMsg, send)
		}()

		// Get the function and args from Payload
		input := &pb.ChaincodeInput{}
		unmarshalErr := proto.Unmarshal(msg.Payload, input)


		// Mark as a transaction (allow put/del state)
		handler.markIsTransaction(msg.Uuid, true)

		// Call chaincode's Run
		// Create the ChaincodeStub which the chaincode can use to callback
		stub := &ChaincodeStub{UUID: msg.Uuid}
		res, err := handler.cc.Run(stub, input.Function, input.Args)

        //type Chaincode interface {
        //    Run(stub *ChaincodeStub, function string, args []string) ([]byte, error)
        //}

		// delete isTransaction entry
		handler.deleteIsTransaction(msg.Uuid)

		// Send COMPLETED message to chaincode support and change state
		chaincodeLogger.Debug("[%s]Transaction completed. Sending %s", shortuuid(msg.Uuid), pb.ChaincodeMessage_COMPLETED)
		nextStateMsg = &pb.ChaincodeMessage{Type: pb.ChaincodeMessage_COMPLETED, Payload: res, Uuid: msg.Uuid}
	}()
}
```

### 