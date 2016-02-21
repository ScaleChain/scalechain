package io.scalechain.blockchain.api.command.blockchain.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet gettxout \
      184c6195ddd4204219644e2d4169d22cf264144ef5b6a49a09a571f4639a60b9 \
      0

  CLI output :
    {
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
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "gettxout", "params": ["184c6195ddd4204219644e2d4169d22cf264144ef5b6a49a09a571f4639a60b9", 0] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
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
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    Right(
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
    )
  }
  def help() : String =
    """gettxout "txid" n ( includemempool )
      |
      |Returns details about an unspent transaction output.
      |
      |Arguments:
      |1. "txid"       (string, required) The transaction id
      |2. n              (numeric, required) vout number
      |3. includemempool  (boolean, optional) Whether to include the mem pool
      |
      |Result:
      |{
      |  "bestblock" : "hash",    (string) the block hash
      |  "confirmations" : n,       (numeric) The number of confirmations
      |  "value" : x.xxx,           (numeric) The transaction value in BTC
      |  "scriptPubKey" : {         (json object)
      |     "asm" : "code",       (string)
      |     "hex" : "hex",        (string)
      |     "reqSigs" : n,          (numeric) Number of required signatures
      |     "type" : "pubkeyhash", (string) The type, eg pubkeyhash
      |     "addresses" : [          (array of string) array of bitcoin addresses
      |        "bitcoinaddress"     (string) bitcoin address
      |        ,...
      |     ]
      |  },
      |  "version" : n,            (numeric) The version
      |  "coinbase" : true|false   (boolean) Coinbase or not
      |}
      |
      |Examples:
      |
      |Get unspent transactions
      |> bitcoin-cli listunspent
      |
      |View the details
      |> bitcoin-cli gettxout "txid" 1
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "gettxout", "params": ["txid", 1] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.stripMargin
}


