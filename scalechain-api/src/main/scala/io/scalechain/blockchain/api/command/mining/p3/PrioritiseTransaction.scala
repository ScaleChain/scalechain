package io.scalechain.blockchain.api.command.mining.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet prioritisetransaction \
        fe0165147da737e16f5096ab6c1709825217377a95a882023ed089a89af4cff9 \
        1234 456789
  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "prioritisetransaction", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** PrioritiseTransaction: adds virtual priority or fee to a transaction,
  * allowing it to be accepted into blocks mined by this node (or miners which use this node)
  * with a lower priority or fee.
  *
  * (It can also remove virtual priority or fee,
  *  requiring the transaction have a higher priority or
  *  fee to be accepted into a locally-mined block.)
  *
  *  Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#prioritisetransaction
  */
object PrioritiseTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


