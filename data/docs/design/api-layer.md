# Introduction
The API layer opens up an http port to provide RPC(Remote Procedure Call) services, which accept Json-RPC requests that query and update the blockchain, and respond with Json-RPC responses.


# List of supported RPCs
In data/scripts/jsonrpc folder, there are sample shell scripts calling RPCs. The following is the list of supported RPCs.
```
GetBestBlockHash
GetBlock
GetBlockHash
Help
GetPeerInfo
DecodeRawTransaction
GetRawTransaction
SendRawTransaction
ImportAddress
GetAccount
GetAccountAddress
GetNewAddress
GetReceivedByAddress
ListTransactions
ListUnspent
SignRawTransaction
```

# Json-RPC specification
See the following link for the specification of each supported Json-RPC service.

https://bitcoin.org/en/developer-reference#rpc-quick-reference

# Netty : The communication channel.
ScaleChain uses Netty 4 for the communication channel of the RPC services.
It listens to a TCP port to accept connections from clients.
After accepting a connection from the client, it handles requests by dispatching the service based on the RPC command requested.

The netty communication channel for the RPC service is implemented by ApiServer, ApiServerHandler, ApiServerInitializer.
The ApiServer is responsible for setting the port to listen to wait for clients' connections.
It asks ApiServerInitialzer to initialize the RPC server by utilizing ApiServerHandler.
ApiServerHandler redirects the request to RequestHandler.handleRequest to dispatch and process the request.

```
            ,-.                                                                                                                           
            `-'                                                                                                                           
            /|\                                                                                                                           
             |                 ,---------.                 ,--------------------.             ,----------------.          ,--------------.
            / \                |ApiServer|                 |ApiServerInitializer|             |ApiServerHandler|          |RequestHandler|
      ScaleChainPeer           `----+----'                 `---------+----------'             `-------+--------'          `------+-------'
            |     listen(port)      |                                |                                |                          |        
            | --------------------->|                                |                                |                          |        
            |                       |                                |                                |                          |        
            |                       |request to initialize the server|                                |                          |        
            |                       |-------------------------------->                                |                          |        
            |                       |                                |                                |                          |        
            |                       |                                | request to handle each response|                          |        
            |                       |                                | ------------------------------->                          |        
            |                       |                                |                                |                          |        
            |                       |                              ,-------------------------------!. |                          |        
            |                       |                              |The ApiServerHandler redirects |_\|       handleRequest      |        
            |                       |                              |each RPC request it receives to  || ------------------------->        
            |                       |                              |RequestHandler.handleRequest     ||                          |        
      ScaleChainPeer           ,----+----.                 ,-------`---------------------------------'+--------.          ,------+-------.
            ,-.                |ApiServer|                 |ApiServerInitializer|             |ApiServerHandler|          |RequestHandler|
            `-'                `---------'                 `--------------------'             `----------------'          `--------------'
            /|\                                                                                                                           
             |                                                                                                                            
            / \                                                                                                                           

```
# Request Dispatcher

RequestHandler.handleRequest parses the request Json to get the command to execute and arguments to apply.
Based on the command string, it dispatches the request to the handler for the specific command.
For example, if the command was "getblock", it is dispatched to GetBlock object, which is responsible for handling the "getblock" request.

```
            ,-.                                                                                                                                                   
            `-'                                                                                                                                                   
            /|\                                                                                                                                                   
             |                                       ,-----------------.                                         ,-------------------------.          ,----------.
            / \                                      |ServiceDispatcher|                                         |Services.serviceByCommand|          |RpcCommand|
      RequestHandler                                 `--------+--------'                                         `------------+------------'          `----+-----'
            |----.                                            |                                                               |                            |      
            |    | parse request string into RpcRequest       |                                                               |                            |      
            |<---'                                            |                                                               |                            |      
            |                                                 |                                                               |                            |      
            |               dispatch(RpcRequest)              |                                                               |                            |      
            | ------------------------------------------------>                                                               |                            |      
            |                                                 |                                                               |                            |      
            |                                                 |             find RpcCommand by the command string             |                            |      
            |                                                 | -------------------------------------------------------------->                            |      
            |                                                 |                                                               |                            |      
            |                                                 |                                     invoke (RpcRequest)       |                            |      
            |                                                 | ------------------------------------------------------------------------------------------->      
            |                                                 |                                                               |                            |      
            |                                                 |----.                                                                                       |      
            |                                                 |    | convert the result of RpcCommand invocation to RpcResponse                            |      
            |                                                 |<---'                                                                                       |      
            |                                                 |                                                               |                            |      
            |                return RpcResponse               |                                                               |                            |      
            | <------------------------------------------------                                                               |                            |      
            |                                                 |                                                               |                            |      
            |----.                                                                                                            |                            |      
            |    | convert RpcResponse to Json response string.                                                               |                            |      
            |<---'                                                                                                            |                            |      
      RequestHandler                                 ,--------+--------.                                         ,------------+------------.          ,----+-----.
            ,-.                                      |ServiceDispatcher|                                         |Services.serviceByCommand|          |RpcCommand|
            `-'                                      `-----------------'                                         `-------------------------'          `----------'
            /|\                                                                                                                                                   
             |                                                                                                                                                    
            / \                                                                                                                                                   

```

# Request Handling 
Each RPC command is serviced by a command object that extends RpcCommand. For example, "getblock" command is serviced by the GetBlock object which extends RpcCommand.

Here is an example code that implements the "getblock" command.
```
object GetBlock extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      // Convert request.params.paramValues, which List[JsValue] to SignRawTransactionParams instance.
      val headerHashString  : String  = request.params.get[String]("Header Hash", 0)
      val format            : Boolean = request.params.getOption[Boolean]("Format", 1).getOrElse(true)

      val headerHash = Hash( HexUtil.bytes(headerHashString) )

      val blockOption : Option[(BlockInfo, Block)] = RpcSubSystem.get.getBlock(headerHash)

      val resultOption = if (format) {
        blockOption.map{ case (blockInfo, block) => BlockFormatter.getBlockResult(blockInfo, block) }
      } else {
        blockOption.map{ case (blockInfo, block) =>
          StringResult( BlockFormatter.getSerializedBlock( block ) )
        }
      }

      Right(resultOption)
    }
  }
```
1. The handlingException clause handles exceptions raised in the body of the serivce to convert it to an RPC error code to pass via the error field of the Json-RPC specification.
2. The service routine parses RPC parameters from JsValue to Scala types such as String or Boolean.
3. The service routine invokes appropriate service logic implemented by layers below the API layer. For example, the GetBlock service invokes RpcSubSystem.getBlock.
4. The service routine converts the result of service logic to a case class that will be converted to a Json string by using a formatter. For example, the GetBlock service uses BlockFormatter.getBlockResult to convert the BlockInfo and Block into GetBlockResult, which is converted to Json string to send as a response of the RPC.

