package io.scalechain.blockchain.api.command.blockchain

import io.scalechain.blockchain.api.RpcSubSystem
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.rawtx.SignRawTransaction
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right
import io.scalechain.util.HexUtil


/*
  CLI command :
    bitcoin-cli -testnet getbestblockhash

  CLI output :
    0000000000075c58ed39c3e50f99b32183d090aefa0cf8c324a82eea9b01a887

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getbestblockhash", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }

*/

/** GetBestBlockHash: returns the header hash of the most recent block on the best block chain.
  *
  * Since - New in 0.9.0
  *
  * Parameters: none
  *
  * Result : (String;hex)
  *   The hash of the block header from the most recent block on the best block chain, encoded as hex in RPC byte order
  *
  * https://bitcoin.org/en/developer-reference#getbestblockhash
  */
object GetBestBlockHash : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return return handlingException {
      val hashOption : Hash? = RpcSubSystem.get().getBestBlockHash()
      Right(
        if (hashOption == null) null
        else StringResult(HexUtil.hex(hashOption.value))
      )
    }
  }
  override fun help() : String =
    """getbestblockhash
      |
      |Returns the hash of the best (tip) block in the longest block chain.
      |
      |Result
      |"hex"      (string) the block hash hex encoded
      |
      |Examples
      |> bitcoin-cli getbestblockhash
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getbestblockhash", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


