package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Create a new address in the “doc test” account.
    bitcoin-cli -testnet getnewaddress "doc test"

  CLI output :
    mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnewaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNewAddress: returns a new Bitcoin address for receiving payments.
  * If an account is specified, payments received with the address will be credited to that account.
  *
  * https://bitcoin.org/en/developer-reference#getnewaddress
  */
object GetNewAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


