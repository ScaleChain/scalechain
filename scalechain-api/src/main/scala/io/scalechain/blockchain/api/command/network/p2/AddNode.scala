package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Try connecting to the following node.
    bitcoin-cli -testnet addnode 192.0.2.113:18333 onetry

  CLI output :
    (no output from bitcoin-cli because result is set to null)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "addnode", "params": [] }

  Json-RPC response :
    {
      "result": null ,
      "error": null,
      "id": "curltest"
    }
*/

/** AddNode: attempts to add or remove a node from the addnode list, or to try a connection to a node once.
  *
  * https://bitcoin.org/en/developer-reference#addnode
  */
object AddNode extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


