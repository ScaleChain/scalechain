package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getrawchangeaddress

  CLI output :
    mnycUc8FRjJodfKhaj9QBZs2PwxxYoWqaK

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getrawchangeaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetRawChangeAddress: returns a new Bitcoin address for receiving change.
  * This is for use with raw transactions, not normal use.
  *
  * https://bitcoin.org/en/developer-reference#getrawchangeaddress
  */
object GetRawChangeAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


