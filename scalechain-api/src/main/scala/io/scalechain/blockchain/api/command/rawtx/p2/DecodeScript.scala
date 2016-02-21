package io.scalechain.blockchain.api.command.rawtx.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet decodescript 522103ede722780d27b05f0b1169ef\
      c90fa15a601a32fc6c3295114500c586831b6aaf2102ecd2d250a76d204011de\
      6bc365a56033b9b3a149f679bc17205555d3c2b2854f21022d609d2f0d359e5b\
      c0e5d0ea20ff9f5d3396cb5b1906aa9c56a0e7b5edc0c5d553ae

  CLI output :
    {
        "asm" : "2 03ede722780d27b05f0b1169efc90fa15a601a32fc6c3295114500c586831b6aaf 02ecd2d250a76d204011de6bc365a56033b9b3a149f679bc17205555d3c2b2854f 022d609d2f0d359e5bc0e5d0ea20ff9f5d3396cb5b1906aa9c56a0e7b5edc0c5d5 3 OP_CHECKMULTISIG",
        "reqSigs" : 2,
        "type" : "multisig",
        "addresses" : [
            "mjbLRSidW1MY8oubvs4SMEnHNFXxCcoehQ",
            "mo1vzGwCzWqteip29vGWWW6MsEBREuzW94",
            "mt17cV37fBqZsnMmrHnGCm9pM28R1kQdMG"
        ],
        "p2sh" : "2MyVxxgNBk5zHRPRY2iVjGRJHYZEp1pMCSq"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "decodescript", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** DecodeScript: decodes a hex-encoded P2SH redeem script.
  *
  * https://bitcoin.org/en/developer-reference#decodescript
  */
object DecodeScript extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """decodescript "hex"
      |
      |Decode a hex-encoded script.
      |
      |Arguments:
      |1. "hex"     (string) the hex encoded script
      |
      |Result:
      |{
      |  "asm":"asm",   (string) Script public key
      |  "hex":"hex",   (string) hex encoded public key
      |  "type":"type", (string) The output type
      |  "reqSigs": n,    (numeric) The required signatures
      |  "addresses": [   (json array of string)
      |     "address"     (string) bitcoin address
      |     ,...
      |  ],
      |  "p2sh","address" (string) script address
      |}
      |
      |Examples:
      |> bitcoin-cli decodescript "hexstring"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "decodescript", "params": ["hexstring"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


