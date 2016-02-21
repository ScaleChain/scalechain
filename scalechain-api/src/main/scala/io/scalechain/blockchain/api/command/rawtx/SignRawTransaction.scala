package io.scalechain.blockchain.api.command.rawtx

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet signrawtransaction 01000000011da9283b4ddf8d\
      89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e0000000000ffff\
      ffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a51\
      59f58488ac00000000

  CLI output :
    {
        "hex" : "01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a47304402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000",
        "complete" : true
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "signrawtransaction", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class SignRawTransactionResult(
) extends RpcResult


/** SignRawTransaction: signs a transaction in the serialized transaction format
  * using private keys stored in the wallet or provided in the call.
  *
  * https://bitcoin.org/en/developer-reference#signrawtransaction
  */
object SignRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """signrawtransaction "hexstring" ( [{"txid":"id","vout":n,"scriptPubKey":"hex","redeemScript":"hex"},...] ["privatekey1",...] sighashtype )
      |
      |Sign inputs for raw transaction (serialized, hex-encoded).
      |The second optional argument (may be null) is an array of previous transaction outputs that
      |this transaction depends on but may not yet be in the block chain.
      |The third optional argument (may be null) is an array of base58-encoded private
      |keys that, if given, will be the only keys used to sign the transaction.
      |
      |
      |Arguments:
      |1. "hexstring"     (string, required) The transaction hex string
      |2. "prevtxs"       (string, optional) An json array of previous dependent transaction outputs
      |     [               (json array of json objects, or 'null' if none provided)
      |       {
      |         "txid":"id",             (string, required) The transaction id
      |         "vout":n,                  (numeric, required) The output number
      |         "scriptPubKey": "hex",   (string, required) script key
      |         "redeemScript": "hex"    (string, required for P2SH) redeem script
      |       }
      |       ,...
      |    ]
      |3. "privatekeys"     (string, optional) A json array of base58-encoded private keys for signing
      |    [                  (json array of strings, or 'null' if none provided)
      |      "privatekey"   (string) private key in base58-encoding
      |      ,...
      |    ]
      |4. "sighashtype"     (string, optional, default=ALL) The signature hash type. Must be one of
      |       "ALL"
      |       "NONE"
      |       "SINGLE"
      |       "ALL|ANYONECANPAY"
      |       "NONE|ANYONECANPAY"
      |       "SINGLE|ANYONECANPAY"
      |
      |Result:
      |{
      |  "hex" : "value",           (string) The hex-encoded raw transaction with signature(s)
      |  "complete" : true|false,   (boolean) If the transaction has a complete set of signatures
      |  "errors" : [                 (json array of objects) Script verification errors (if there are any)
      |    {
      |      "txid" : "hash",           (string) The hash of the referenced, previous transaction
      |      "vout" : n,                (numeric) The index of the output to spent and used as input
      |      "scriptSig" : "hex",       (string) The hex-encoded signature script
      |      "sequence" : n,            (numeric) Script sequence number
      |      "error" : "text"           (string) Verification or signing error related to the input
      |    }
      |    ,...
      |  ]
      |}
      |
      |Examples:
      |> bitcoin-cli signrawtransaction "myhex"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "signrawtransaction", "params": ["myhex"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


