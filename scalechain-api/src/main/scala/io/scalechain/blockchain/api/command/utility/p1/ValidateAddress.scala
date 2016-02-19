package io.scalechain.blockchain.api.command.utility.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet validateaddress mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe

  CLI output :
    {
        "isvalid" : true,
        "address" : "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe",
        "ismine" : true,
        "iswatchonly" : false,
        "isscript" : false,
        "pubkey" : "03bacb84c6464a58b3e0a53cc0ba4cb3b82848cd7bed25a7724b3cc75d76c9c1ba",
        "iscompressed" : true,
        "account" : "test label"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "validateaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ValidateAddress: returns information about the given Bitcoin address.
  *
  * https://bitcoin.org/en/developer-reference#validateaddress
  */
object ValidateAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


