# Introduction
Analyze IBM openchain chaincode.

## Where it runs?
Chain code runs on a separate docker container.

```
**Note:** When you deploy a chaincode, each peer will create another Docker container.

Unless otherwise noted, you are at $GOPATH/src/github.com/openchain/obc-peer

```

## chaincodeInvokeOrQuery; main.go 
```

// chaincodeInvokeOrQuery invokes or queries the chaincode. If successful, the
// INVOKE form prints the transaction ID on STDOUT, and the QUERY form prints
// the query result on STDOUT. A command-line flag (-r, --raw) determines
// whether the query result is output as raw bytes, or as a printable string.
// The printable form is optionally (-x, --hex) a hexadecimal representation
// of the query response. If the query response is NIL, nothing is output.
func chaincodeInvokeOrQuery(cmd *cobra.Command, args []string, invoke bool) (err error) {



	devopsClient, err := getDevopsClient(cmd)

	// Build the spec
	input := &pb.ChaincodeInput{}

	spec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG,
		ChaincodeID: &pb.ChaincodeID{Name: chaincodeName}, CtorMsg: input}

	// If security is enabled, add client login token
	if viper.GetBool("security.enabled") {

		// Retrieve the CLI data storage path
		// Returns /var/openchain/production/client/
		localStore := getCliFilePath()

		// Check if the user is logged in before sending transaction
		if _, err = os.Stat(localStore + "loginToken_" + chaincodeUsr); err == nil {
			logger.Info("Local user '%s' is already logged in. Retrieving login token.\n", chaincodeUsr)

			// Read in the login token
			token, err := ioutil.ReadFile(localStore + "loginToken_" + chaincodeUsr)


			// Add the login token to the chaincodeSpec
			spec.SecureContext = string(token)

			// If privacy is enabled, mark chaincode as confidential
			if viper.GetBool("security.privacy") {
				logger.Info("Set confidentiality level to CONFIDENTIAL.\n")
				spec.ConfidentialityLevel = pb.ConfidentialityLevel_CONFIDENTIAL
			}
		} 
    }

	// Build the ChaincodeInvocationSpec message
	invocation := &pb.ChaincodeInvocationSpec{ChaincodeSpec: spec}

	var resp *pb.Response
	if invoke {
		resp, err = devopsClient.Invoke(context.Background(), invocation)
	} else {
		resp, err = devopsClient.Query(context.Background(), invocation)
	}

}

```

## chaincode_support.go ; openchain/chaincode

### LaunchChaincode
```
// LaunchChaincode will launch the chaincode if not running (if running return nil) and will wait for handler of the chaincode to get into FSM ready state.
func (chaincodeSupport *ChaincodeSupport) LaunchChaincode(context context.Context, t *pb.Transaction) (*pb.ChaincodeID, *pb.ChaincodeInput, error) {

    // case 1 : CHAINCODE_NEW
	if t.Type == pb.Transaction_CHAINCODE_NEW {
		cds := &pb.ChaincodeDeploymentSpec{}
		err := proto.Unmarshal(t.Payload, cds)
		if err != nil {
			return nil, nil, err
		}
		cID = cds.ChaincodeSpec.ChaincodeID
		cMsg = cds.ChaincodeSpec.CtorMsg
		f = &cMsg.Function
		initargs = cMsg.Args
   // case 2 : CHAINCODE_EXECUTE, CHAINCODE_QUERY
	} else if t.Type == pb.Transaction_CHAINCODE_EXECUTE || t.Type == pb.Transaction_CHAINCODE_QUERY {
		ci := &pb.ChaincodeInvocationSpec{}
		err := proto.Unmarshal(t.Payload, ci)

		cID = ci.ChaincodeSpec.ChaincodeID
		cMsg = ci.ChaincodeSpec.CtorMsg
	}
	

    // This line launches chaincode!
	//from here on : if we launch the container and get an error, we need to stop the container
	if !chaincodeSupport.userRunsCC && handler == nil {
		_, err = chaincodeSupport.launchAndWaitForRegister(context, cID, t.Uuid)
		if err != nil {
			chaincodeLog.Debug("launchAndWaitForRegister failed %s", err)
			return cID, cMsg, err
		}
	}

	if err == nil {
		//send init (if (f,args)) and wait for ready state
		err = chaincodeSupport.sendInitOrReady(context, t.Uuid, chaincode, f, initargs, chaincodeSupport.ccStartupTimeout, t)
		if err != nil {
			chaincodeLog.Debug("sending init failed(%s)", err)
			err = fmt.Errorf("Failed to init chaincode(%s)", err)
			errIgnore := chaincodeSupport.stopChaincode(context, cID)
			if errIgnore != nil {
				chaincodeLog.Debug("stop failed %s(%s)", errIgnore, err)
			}
		}
		chaincodeLog.Debug("sending init completed")
	}

	chaincodeLog.Debug("LaunchChaincode complete")

	return cID, cMsg, err
}

```

### stopChaincode
```
func (chaincodeSupport *ChaincodeSupport) stopChaincode(context context.Context, cID *pb.ChaincodeID) error {
	chaincode := cID.Name


	vmname := container.GetVMFromName(chaincode)

	//stop the chaincode
	sir := container.StopImageReq{ID: vmname, Timeout: 0}

	_, err := container.VMCProcess(context, "Docker", sir)


	chaincodeSupport.handlerMap.Lock()
	ok := chaincodeSupport.chaincodeHasBeenLaunched(chaincode);

	delete(chaincodeSupport.handlerMap.chaincodeMap, chaincode)

	chaincodeSupport.handlerMap.Unlock()

}

```
## chaincode.go; opencode/chaincode/shim

### Start
```
// Start entry point for chaincodes bootstrap.
func Start(cc Chaincode) error {
	// Establish connection with validating peer
	clientConn, err := newPeerClientConnection()

	chaincodeSupportClient := pb.NewChaincodeSupportClient(clientConn)

	err = chatWithPeer(chaincodeSupportClient, cc)
}

```

### newPeerClientConnection

```
func newPeerClientConnection() (*grpc.ClientConn, error) {
	var opts []grpc.DialOption
	opts = append(opts, grpc.WithTimeout(1*time.Second))
	opts = append(opts, grpc.WithBlock())
	opts = append(opts, grpc.WithInsecure())
	conn, err := grpc.Dial(getPeerAddress(), opts...)
}

```

### chatWithPeer
```
func chatWithPeer(chaincodeSupportClient pb.ChaincodeSupportClient, cc Chaincode) error {

	// Establish stream with validating peer
	stream, err := chaincodeSupportClient.Register(context.Background())


	// Create the chaincode stub which will be passed to the chaincode
	//stub := &ChaincodeStub{}

	// Create the shim handler responsible for all control logic
	handler = newChaincodeHandler(getPeerAddress(), stream, cc)

	defer stream.CloseSend()
	// Send the ChaincodeID during register.
	chaincodeID := &pb.ChaincodeID{Name: viper.GetString("chaincode.id.name")}
	payload, err := proto.Marshal(chaincodeID)

	// Register on the stream
	chaincodeLogger.Debug("Registering.. sending %s", pb.ChaincodeMessage_REGISTER)
	handler.serialSend(&pb.ChaincodeMessage{Type: pb.ChaincodeMessage_REGISTER, Payload: payload})

			// Call FSM.handleMessage()
			err = handler.handleMessage(in)

			if nsInfo != nil && nsInfo.sendToCC {
				chaincodeLogger.Debug("[%s]send state message %s", shortuuid(in.Uuid), in.Type.String())
				
				handler.serialSend(in)

			}
		}
	}()
	<-waitc
	return err
}
```

## opencode/chaincode/shim/handler.go
### newChaincodeHandler 

```
// NewChaincodeHandler returns a new instance of the shim side handler.
func newChaincodeHandler(to string, peerChatStream PeerChaincodeStream, chaincode Chaincode) *Handler {
	v := &Handler{
		To:         to,
		ChatStream: peerChatStream,
		cc:         chaincode,
	}
	v.responseChannel = make(map[string]chan pb.ChaincodeMessage)
	v.isTransaction = make(map[string]bool)
	v.nextState = make(chan *nextStateInfo)

	// Create the shim side FSM
	v.FSM = fsm.NewFSM(
		"created",
		fsm.Events{
			{Name: pb.ChaincodeMessage_REGISTERED.String(), Src: []string{"created"}, Dst: "established"},
			{Name: pb.ChaincodeMessage_INIT.String(), Src: []string{"established"}, Dst: "init"},
			{Name: pb.ChaincodeMessage_READY.String(), Src: []string{"established"}, Dst: "ready"},
			{Name: pb.ChaincodeMessage_ERROR.String(), Src: []string{"init"}, Dst: "established"},
			{Name: pb.ChaincodeMessage_RESPONSE.String(), Src: []string{"init"}, Dst: "init"},
			{Name: pb.ChaincodeMessage_COMPLETED.String(), Src: []string{"init"}, Dst: "ready"},
			{Name: pb.ChaincodeMessage_TRANSACTION.String(), Src: []string{"ready"}, Dst: "transaction"},
			{Name: pb.ChaincodeMessage_COMPLETED.String(), Src: []string{"transaction"}, Dst: "ready"},
			{Name: pb.ChaincodeMessage_ERROR.String(), Src: []string{"transaction"}, Dst: "ready"},
			{Name: pb.ChaincodeMessage_RESPONSE.String(), Src: []string{"transaction"}, Dst: "transaction"},
			{Name: pb.ChaincodeMessage_QUERY.String(), Src: []string{"transaction"}, Dst: "transaction"},
			{Name: pb.ChaincodeMessage_QUERY.String(), Src: []string{"ready"}, Dst: "ready"},
			{Name: pb.ChaincodeMessage_RESPONSE.String(), Src: []string{"ready"}, Dst: "ready"},
		},
		fsm.Callbacks{
			"before_" + pb.ChaincodeMessage_REGISTERED.String(): func(e *fsm.Event) { v.beforeRegistered(e) },
			//"after_" + pb.ChaincodeMessage_INIT.String(): func(e *fsm.Event) { v.beforeInit(e) },
			//"after_" + pb.ChaincodeMessage_TRANSACTION.String(): func(e *fsm.Event) { v.beforeTransaction(e) },
			"after_" + pb.ChaincodeMessage_RESPONSE.String(): func(e *fsm.Event) { v.afterResponse(e) },
			"after_" + pb.ChaincodeMessage_ERROR.String():    func(e *fsm.Event) { v.afterError(e) },
			"enter_init":                                     func(e *fsm.Event) { v.enterInitState(e) },
			"enter_transaction":                              func(e *fsm.Event) { v.enterTransactionState(e) },
			//"enter_ready":                                     func(e *fsm.Event) { v.enterReadyState(e) },
			"before_" + pb.ChaincodeMessage_QUERY.String(): func(e *fsm.Event) { v.beforeQuery(e) }, //only checks for QUERY
		},
	)
	return v
}


```


### createTransactionMessage
```
// createTransactionMessage creates a transaction message.
func createTransactionMessage(uuid string, cMsg *pb.ChaincodeInput) (*pb.ChaincodeMessage, error) {
	payload, err := proto.Marshal(cMsg)

	return &pb.ChaincodeMessage{Type: pb.ChaincodeMessage_TRANSACTION, Payload: payload, Uuid: uuid}, nil
}
```


### example
```

package main

import (
	"errors"
	"fmt"
	"strconv"

	"github.com/openblockchain/obc-peer/openchain/chaincode/shim"
)

// SimpleChaincode example simple Chaincode implementation
type SimpleChaincode struct {
}

func (t *SimpleChaincode) init(stub *shim.ChaincodeStub, args []string) ([]byte, error) {
	var A, B string    // Entities
	var Aval, Bval int // Asset holdings
	var err error

	if len(args) != 4 {
		return nil, errors.New("Incorrect number of arguments. Expecting 4")
	}

	// Initialize the chaincode
	A = args[0]
	Aval, err = strconv.Atoi(args[1])

	B = args[2]
	Bval, err = strconv.Atoi(args[3])

	fmt.Printf("Aval = %d, Bval = %d\n", Aval, Bval)

	// Write the state to the ledger
	err = stub.PutState(A, []byte(strconv.Itoa(Aval)))
	if err != nil {
		return nil, err
	}

	err = stub.PutState(B, []byte(strconv.Itoa(Bval)))
	if err != nil {
		return nil, err
	}

	return nil, nil
}

// Transaction makes payment of X units from A to B
func (t *SimpleChaincode) invoke(stub *shim.ChaincodeStub, args []string) ([]byte, error) {
	var A, B string    // Entities
	var Aval, Bval int // Asset holdings
	var X int          // Transaction value
	var err error

	if len(args) != 3 {
		return nil, errors.New("Incorrect number of arguments. Expecting 3")
	}

	A = args[0]
	B = args[1]

	// Get the state from the ledger
	// TODO: will be nice to have a GetAllState call to ledger
	Avalbytes, err := stub.GetState(A)

	Aval, _ = strconv.Atoi(string(Avalbytes))

	Bvalbytes, err := stub.GetState(B)

	Bval, _ = strconv.Atoi(string(Bvalbytes))

	// Perform the execution
	X, err = strconv.Atoi(args[2])
	Aval = Aval - X
	Bval = Bval + X
	fmt.Printf("Aval = %d, Bval = %d\n", Aval, Bval)

	// Write the state back to the ledger
	err = stub.PutState(A, []byte(strconv.Itoa(Aval)))

	err = stub.PutState(B, []byte(strconv.Itoa(Bval)))

	return nil, nil
}

// Deletes an entity from state
func (t *SimpleChaincode) delete(stub *shim.ChaincodeStub, args []string) ([]byte, error) {
	if len(args) != 1 {
		return nil, errors.New("Incorrect number of arguments. Expecting 3")
	}

	A := args[0]

	// Delete the key from the state in ledger
	err := stub.DelState(A)
	if err != nil {
		return nil, errors.New("Failed to delete state")
	}

	return nil, nil
}

// Run callback representing the invocation of a chaincode
// This chaincode will manage two accounts A and B and will transfer X units from A to B upon invoke
func (t *SimpleChaincode) Run(stub *shim.ChaincodeStub, function string, args []string) ([]byte, error) {

	// Handle different functions
	if function == "init" {
		// Initialize the entities and their asset holdings
		return t.init(stub, args)
	} else if function == "invoke" {
		// Transaction makes payment of X units from A to B
		return t.invoke(stub, args)
	} else if function == "delete" {
		// Deletes an entity from its state
		return t.delete(stub, args)
	}

	return nil, errors.New("Received unknown function invocation")
}

// Query callback representing the query of a chaincode
func (t *SimpleChaincode) Query(stub *shim.ChaincodeStub, function string, args []string) ([]byte, error) {
	if function != "query" {
		return nil, errors.New("Invalid query function name. Expecting \"query\"")
	}
	var A string // Entities
	var err error

	if len(args) != 1 {
		return nil, errors.New("Incorrect number of arguments. Expecting name of the person to query")
	}

	A = args[0]

	// Get the state from the ledger
	Avalbytes, err := stub.GetState(A)
	if err != nil {
		jsonResp := "{\"Error\":\"Failed to get state for " + A + "\"}"
		return nil, errors.New(jsonResp)
	}

	if Avalbytes == nil {
		jsonResp := "{\"Error\":\"Nil amount for " + A + "\"}"
		return nil, errors.New(jsonResp)
	}

	jsonResp := "{\"Name\":\"" + A + "\",\"Amount\":\"" + string(Avalbytes) + "\"}"
	fmt.Printf("Query Response:%s\n", jsonResp)
	return Avalbytes, nil
}

func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}

```