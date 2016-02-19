package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Put the address indicated below in the “doc test” account.
    bitcoin-cli -testnet setaccount \
      mmXgiR6KAhZCyQ8ndr2BCfEq1wNG2UnyG6 "doc test"

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "setacount", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** SetAccount: puts the specified address in the given account.
  *
  * https://bitcoin.org/en/developer-reference#setaccount
  */
object SetAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


