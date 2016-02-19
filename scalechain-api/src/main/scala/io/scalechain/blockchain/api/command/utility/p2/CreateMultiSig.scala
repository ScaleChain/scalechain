package io.scalechain.blockchain.api.command.utility.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet createmultisig 2 '''
      [
        "mjbLRSidW1MY8oubvs4SMEnHNFXxCcoehQ",
        "02ecd2d250a76d204011de6bc365a56033b9b3a149f679bc17205555d3c2b2854f",
        "mt17cV37fBqZsnMmrHnGCm9pM28R1kQdMG"
      ]
    '''

  CLI output :
    {
      "address" : "2MyVxxgNBk5zHRPRY2iVjGRJHYZEp1pMCSq",
      "redeemScript" : "522103ede722780d27b05f0b1169efc90fa15a601a32fc6c3295114500c586831b6aaf2102ecd2d250a76d204011de6bc365a56033b9b3a149f679bc17205555d3c2b2854f21022d609d2f0d359e5bc0e5d0ea20ff9f5d3396cb5b1906aa9c56a0e7b5edc0c5d553ae"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "createmultisig", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** CreateMultiSig: creates a P2SH multi-signature address.
  *
  * https://bitcoin.org/en/developer-reference#createmultisig
  */
object CreateMultiSig extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


