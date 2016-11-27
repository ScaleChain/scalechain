package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet getmempoolinfo

  CLI output :
    {
      "size" : 37,
      "bytes" : 9423
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getmempoolinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetMemPoolInfo: returns information about the node’s current transaction memory pool.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getmempoolinfo
  */
object GetMemPoolInfo : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getmempoolinfo
      |
      |Returns details on the active state of the TX memory pool.
      |
      |Result:
      |{
      |  "size": xxxxx,               (numeric) Current tx count
      |  "bytes": xxxxx,              (numeric) Sum of all tx sizes
      |  "usage": xxxxx,              (numeric) Total memory usage for the mempool
      |  "maxmempool": xxxxx,         (numeric) Maximum memory usage for the mempool
      |  "mempoolminfee": xxxxx       (numeric) Minimum fee for tx to be accepted
      |}
      |
      |Examples:
      |> bitcoin-cli getmempoolinfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getmempoolinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.stripMargin
}


