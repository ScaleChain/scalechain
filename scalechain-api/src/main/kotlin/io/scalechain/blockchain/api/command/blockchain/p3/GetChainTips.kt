package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    bitcoin-cli -testnet getchaintips

  CLI output :
    <
        {
            "height" : 312647,
            "hash" : "000000000b1be96f87b31485f62c1361193304a5ad78acf47f9164ea4773a843",
            "branchlen" : 0,
            "status" : "active"
        },
        {
            "height" : 282072,
            "hash" : "00000000712340a499b185080f94b28c365d8adb9fc95bca541ea5e708f31028",
            "branchlen" : 5,
            "status" : "valid-fork"
        },
        {
            "height" : 281721,
            "hash" : "000000006e1f2a32199629c6c1fbd37766f5ce7e8c42bab0c6e1ae42b88ffe12",
            "branchlen" : 1,
            "status" : "valid-headers"
        }
    >

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getchaintips", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetChainTips: returns information about the highest-height block (tip) of each local block chain.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getchaintips
  */
object GetChainTips : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """getchaintips
      |Return information about all known tips in the block tree, including the main chain as well as orphaned branches.
      |
      |Result:
      |[
      |  {
      |    "height": xxxx,         (numeric) height of the chain tip
      |    "hash": "xxxx",         (string) block hash of the tip
      |    "branchlen": 0          (numeric) zero for main chain
      |    "status": "active"      (string) "active" for the main chain
      |  },
      |  {
      |    "height": xxxx,
      |    "hash": "xxxx",
      |    "branchlen": 1          (numeric) length of branch connecting the tip to the main chain
      |    "status": "xxxx"        (string) status of the chain (active, valid-fork, valid-headers, headers-only, invalid)
      |  }
      |]
      |Possible values for status:
      |1.  "invalid"               This branch contains at least one invalid block
      |2.  "headers-only"          Not all blocks for this branch are available, but the headers are valid
      |3.  "valid-headers"         All blocks are available for this branch, but they were never fully validated
      |4.  "valid-fork"            This branch is not part of the active chain, but is fully validated
      |5.  "active"                This is the tip of the active main chain, which is certainly valid
      |
      |Examples:
      |> bitcoin-cli getchaintips
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getchaintips", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


