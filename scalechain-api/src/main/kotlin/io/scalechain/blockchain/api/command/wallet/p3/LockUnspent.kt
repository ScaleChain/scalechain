package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    # Lock two outputs.
    bitcoin-cli -testnet lockunspent false '''
      [
        {
          "txid": "5a7d24cd665108c66b2d56146f244932edae4e2376b561b3d396d5ae017b9589",
          "vout": 0
        },
        {
          "txid": "6c5edd41a33f9839257358ba6ddece67df9db7f09c0db6bbea00d0372e8fe5cd",
          "vout": 0
        }
      ]
    '''

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "lockunspent", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** LockUnspent: temporarily locks or unlocks specified transaction outputs.
  * A locked transaction output will not be chosen by automatic coin selection when spending bitcoins.
  *
  * Locks are stored in memory only, so nodes start with zero locked outputs and
  * the locked output list is always cleared when a node stops or fails.
  *
  * https://bitcoin.org/en/developer-reference#lockunspent
  */
object LockUnspent : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
"""

"""
}


