package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Adding a 2-of-3 P2SH multisig address to the “test account” by mixing two P2PKH addresses and one full public key:
    bitcoin-cli -testnet addmultisigaddress \
      2 \
      '''
        [
          "mjbLRSidW1MY8oubvs4SMEnHNFXxCcoehQ",
          "02ecd2d250a76d204011de6bc365a56033b9b3a149f679bc17205555d3c2b2854f",
          "mt17cV37fBqZsnMmrHnGCm9pM28R1kQdMG"
        ]
      ''' \
      'test account'

  CLI output :
    2MyVxxgNBk5zHRPRY2iVjGRJHYZEp1pMCSq

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "addmultisigaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** AddMultiSigAddress: adds a P2SH multisig address to the wallet.
  *
  * https://bitcoin.org/en/developer-reference#addmultisigaddress
  */
object AddMultiSigAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


