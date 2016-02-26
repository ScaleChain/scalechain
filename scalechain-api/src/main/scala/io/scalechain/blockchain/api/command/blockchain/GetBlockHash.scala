package io.scalechain.blockchain.api.command.blockchain

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash._
import io.scalechain.blockchain.api.domain.{StringResult, RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.{HashFormat, Hash}
import io.scalechain.util.ByteArray
import spray.json.DefaultJsonProtocol._

/*
  CLI command :
    bitcoin-cli -testnet getblockhash 240886

  CLI output :
    00000000a0faf83ab5799354ae9c11da2a2bd6db44058e03c528851dee0a3fff

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblockhash", "params": [240886] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/


/** GetBlockHash: returns the header hash of a block at the given height in the local best block chain.
  *
  * Parameter #1 : Block Height (Number;int, Required)
  *   The height of the block whose header hash should be returned.
  *   The height of the hardcoded genesis block is 0.
  *
  * Result: (String;hex)
  *   The hash of the block at the requested height, encoded as hex in RPC byte order, or JSON null if an error occurred.
  *
  * https://bitcoin.org/en/developer-reference#getblockhash
  */
object GetBlockHash extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {

      // Convert request.params.paramValues, which List[JsValue] to SignRawTransactionParams instance.
      val blockHeight : Long = request.params.get[Long]("Block Height", 0)

      // TODO : Implement
      val blockHash = Hash("0000000000075c58ed39c3e50f99b32183d090aefa0cf8c324a82eea9b01a887")
      val hashString = ByteArray.byteArrayToString(blockHash.value)
      Right(Some(StringResult(hashString)))
    }
  }
  def help() : String =
    """getblockhash index
      |
      |Returns hash of block in best-block-chain at index provided.
      |
      |Arguments:
      |1. index         (numeric, required) The block index
      |
      |Result:
      |"hash"         (string) The block hash
      |
      |Examples:
      |> bitcoin-cli getblockhash 1000
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockhash", "params": [1000] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


