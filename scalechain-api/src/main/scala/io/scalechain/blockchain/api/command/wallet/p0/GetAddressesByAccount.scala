package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getaddressesbyaccount "doc test"

  CLI output :
    [
        "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN",
        "mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma",
        "mmXgiR6KAhZCyQ8ndr2BCfEq1wNG2UnyG6"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaddressbyaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAddressesByAccount: returns a list of every address assigned to a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getaddressesbyaccount
  */
object GetAddressesByAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


