package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getwalletinfo

  CLI output :
    {
        "walletversion" : 60000,
        "balance" : 1.45060000,
        "txcount" : 17,
        "keypoololdest" : 1398809500,
        "keypoolsize" : 196,
        "unlocked_until" : 0
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getwalletinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetWalletInfo: provides information about the wallet.
  *
  * Since - New in 0.9.2
  *
  * https://bitcoin.org/en/developer-reference#getwalletinfo
  */
object GetWalletInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


