package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # From the account test1, send 0.1 coins to the first address
    # and 0.2 coins to the second address, with a comment of “Example Transaction”.
    bitcoin-cli -testnet sendmany \
      "test1" \
      '''
        {
          "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN": 0.1,
          "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe": 0.2
        } ''' \
      6       \
      "Example Transaction"

  CLI output :
    ec259ab74ddff199e61caa67a26e29b13b5688dc60f509ce0df4d044e8f4d63d

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "sendmany", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SendMany: creates and broadcasts a transaction which sends outputs to multiple addresses.
  *
  * https://bitcoin.org/en/developer-reference#sendmany
  */
object SendMany extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


