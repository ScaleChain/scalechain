package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getblockchaininfo

  CLI output :
    {
        "chain" : "test",
        "blocks" : 315280,
        "headers" : 315280,
        "bestblockhash" : "000000000ebb17fb455e897b8f3e343eea1b07d926476d00bc66e2c0342ed50f",
        "difficulty" : 1.00000000,
        "verificationprogress" : 1.00000778,
        "chainwork" : "0000000000000000000000000000000000000000000000015e984b4fb9f9b350"
    }
  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblockchaininfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }

*/

/** GetBlockChainInfo: provides information about the current state of the block chain.
  *
  * Since - New in 0.9.2, Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getblockchaininfo
  */
object GetBlockChainInfo extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getblockchaininfo
      |Returns an object containing various state info regarding block chain processing.
      |
      |Result:
      |{
      |  "chain": "xxxx",        (string) current network name as defined in BIP70 (main, test, regtest)
      |  "blocks": xxxxxx,         (numeric) the current number of blocks processed in the server
      |  "headers": xxxxxx,        (numeric) the current number of headers we have validated
      |  "bestblockhash": "...", (string) the hash of the currently best block
      |  "difficulty": xxxxxx,     (numeric) the current difficulty
      |  "mediantime": xxxxxx,     (numeric) median time for the current best block
      |  "verificationprogress": xxxx, (numeric) estimate of verification progress [0..1]
      |  "chainwork": "xxxx"     (string) total amount of work in active chain, in hexadecimal
      |  "pruned": xx,             (boolean) if the blocks are subject to pruning
      |  "pruneheight": xxxxxx,    (numeric) heighest block available
      |  "softforks": [            (array) status of softforks in progress
      |     {
      |        "id": "xxxx",        (string) name of softfork
      |        "version": xx,         (numeric) block version
      |        "enforce": {           (object) progress toward enforcing the softfork rules for new-version blocks
      |           "status": xx,       (boolean) true if threshold reached
      |           "found": xx,        (numeric) number of blocks with the new version found
      |           "required": xx,     (numeric) number of blocks required to trigger
      |           "window": xx,       (numeric) maximum size of examined window of recent blocks
      |        },
      |        "reject": { ... }      (object) progress toward rejecting pre-softfork blocks (same fields as "enforce")
      |     }, ...
      |  ]
      |}
      |
      |Examples:
      |> bitcoin-cli getblockchaininfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockchaininfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.stripMargin
}


