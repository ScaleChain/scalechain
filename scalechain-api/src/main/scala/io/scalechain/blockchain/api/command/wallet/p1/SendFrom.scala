package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Spend 0.1 coins from the account “test” to the address indicated below
    # using only UTXOs with at least six confirmations,
    # giving the transaction the comment “Example spend”
    # and labeling the spender “Example.com”.
    bitcoin-cli -testnet sendfrom "test" \
      mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe \
      0.1 \
      6 \
      "Example spend" \
      "Example.com"

  CLI output :
    f14ee5368c339644d3037d929bbe1f1544a532f8826c7b7288cb994b0b0ff5d8

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "sendfrom", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SendFrom: spends an amount from a local account to a bitcoin address.
  *
  * https://bitcoin.org/en/developer-reference#sendfrom
  */
object SendFrom extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


