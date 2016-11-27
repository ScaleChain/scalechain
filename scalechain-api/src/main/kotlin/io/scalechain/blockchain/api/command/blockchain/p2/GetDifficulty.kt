package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet getdifficulty

  CLI output :
    1.00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getdifficulty", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetDifficulty: returns the proof-of-work difficulty as a multiple of the minimum difficulty.
  *
  * https://bitcoin.org/en/developer-reference#getdifficulty
  */
object GetDifficulty : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getdifficulty
      |
      |Returns the proof-of-work difficulty as a multiple of the minimum difficulty.
      |
      |Result:
      |n.nnn       (numeric) the proof-of-work difficulty as a multiple of the minimum difficulty.
      |
      |Examples:
      |> bitcoin-cli getdifficulty
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getdifficulty", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.stripMargin
}


