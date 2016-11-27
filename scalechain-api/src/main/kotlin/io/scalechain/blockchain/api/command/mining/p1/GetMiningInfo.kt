package io.scalechain.blockchain.api.command.mining.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet getmininginfo

  CLI output :
    {
        "blocks" : 313168,
        "currentblocksize" : 1819,
        "currentblocktx" : 3,
        "difficulty" : 1.00000000,
        "errors" : "",
        "genproclimit" : 1,
        "networkhashps" : 5699977416637,
        "pooledtx" : 8,
        "testnet" : true,
        "chain" : "test",
        "generate" : true,
        "hashespersec" : 921200
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getmininginfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetMiningInfo: returns various mining-related information.
  *
  * Updated in master
  *
  * https://bitcoin.org/en/developer-reference#getmininginfo
  */
object GetMiningInfo : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getmininginfo
      |
      |Returns a json object containing mining-related information.
      |Result:
      |{
      |  "blocks": nnn,             (numeric) The current block
      |  "currentblocksize": nnn,   (numeric) The last block size
      |  "currentblocktx": nnn,     (numeric) The last block transaction
      |  "difficulty": xxx.xxxxx    (numeric) The current difficulty
      |  "errors": "..."          (string) Current errors
      |  "generate": true|false     (boolean) If the generation is on or off (see getgenerate or setgenerate calls)
      |  "genproclimit": n          (numeric) The processor limit for generation. -1 if no generation. (see getgenerate or setgenerate calls)
      |  "pooledtx": n              (numeric) The size of the mem pool
      |  "testnet": true|false      (boolean) If using testnet or not
      |  "chain": "xxxx",         (string) current network name as defined in BIP70 (main, test, regtest)
      |}
      |
      |Examples:
      |> bitcoin-cli getmininginfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getmininginfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


