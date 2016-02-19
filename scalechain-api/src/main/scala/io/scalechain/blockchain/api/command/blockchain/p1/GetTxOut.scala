package io.scalechain.blockchain.api.command.blockchain.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*

Json-RPC request : {"jsonrpc": "1.0", "id":"curltest", "method": "gettxout", "params": ["184c6195ddd4204219644e2d4169d22cf264144ef5b6a49a09a571f4639a60b9", 0] }

Json-RPC response :
{
  "result": {
    "bestblock": "000000000000000001ef38012ed2e2f674e59bf2314e55f2b4e6f71a7657df50",
    "confirmations": 5,
    "value": 15,
    "scriptPubKey": {
      "asm": "OP_DUP OP_HASH160 60692856b3121dab03f477f130b9fee7e6e60234 OP_EQUALVERIFY OP_CHECKSIG",
      "hex": "76a91460692856b3121dab03f477f130b9fee7e6e6023488ac",
      "reqSigs": 1,
      "type": "pubkeyhash",
      "addresses": [
        "19nmqtjciexd6NU9VNp5JTu4hht98pULn5"
      ]
    },
    "version": 1,
    "coinbase": false
  },
  "error": null,
  "id": "curltest"
}
*/

case class ScriptPubKey(
                         asm: String,
                         hex: String,
                         reqSigs: Int,
                         `type` : String,
                         addresses : Array[String]
                       )

case class GetTxOutResult(
                           bestblock : String,
                           confirmations: Int,
                           value: Int,
                           scriptPubKey: ScriptPubKey,
                           version: Int,
                           coinbase : Boolean
                         ) extends RpcResult

/** GetTxOut: returns details about a transaction output.
  * Only unspent transaction outputs (UTXOs) are guaranteed to be available.
  *
  * https://bitcoin.org/en/developer-reference#gettxout
  */
object GetTxOut extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    GetTxOutResult(
      bestblock = "000000000000000001ef38012ed2e2f674e59bf2314e55f2b4e6f71a7657df50",
      confirmations = 5,
      value = 15,
      scriptPubKey = ScriptPubKey (
        asm = "OP_DUP OP_HASH160 60692856b3121dab03f477f130b9fee7e6e60234 OP_EQUALVERIFY OP_CHECKSIG",
        hex = "76a91460692856b3121dab03f477f130b9fee7e6e6023488ac",
        reqSigs = 1,
        `type` = "pubkeyhash",
        addresses = Array[String]( "19nmqtjciexd6NU9VNp5JTu4hht98pULn5" )
      ),
      version = 1,
      coinbase = false
    )
  }
}


