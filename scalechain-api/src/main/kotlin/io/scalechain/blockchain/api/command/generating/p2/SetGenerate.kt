package io.scalechain.blockchain.api.command.generating.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Using regtest mode, generate 2 blocks:
    bitcoin-cli -regtest setgenerate true 101

  CLI output :
    [
        "7e38de938d0dcbb41be63d78a8353e77e9d1b3ef82e0368eda051d4926eef915",
        "61d6e5f1a64d009659f45ef1c614e57f4aa0501708641212be236dc56d726da8"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "setgenerate", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SetGenerate: enables or disables hashing to attempt to find the next block.
  *
  * Since - Updated in master
  *
  * https://bitcoin.org/en/developer-reference#setgenerate
  */
object SetGenerate : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """setgenerate generate ( genproclimit )
      |
      |Set 'generate' true or false to turn generation on or off.
      |Generation is limited to 'genproclimit' processors, -1 is unlimited.
      |See the getgenerate call for the current setting.
      |
      |Arguments:
      |1. generate         (boolean, required) Set to true to turn on generation, off to turn off.
      |2. genproclimit     (numeric, optional) Set the processor limit for when generation is on. Can be -1 for unlimited.
      |
      |Examples:
      |
      |Set the generation on with a limit of one processor
      |> bitcoin-cli setgenerate true 1
      |
      |Check the setting
      |> bitcoin-cli getgenerate
      |
      |Turn off generation
      |> bitcoin-cli setgenerate false
      |
      |Using json rpc
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "setgenerate", "params": [true, 1] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


