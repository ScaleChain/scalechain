package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet dumpprivkey moQR7i8XM4rSGoNwEsw3h4YEuduuP6mxw7

  CLI output :
    cTVNtBK7mBi2yc9syEnwbiUpnpGJKohDWzXMeF4tGKAQ7wvomr95

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "dumprivkey", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** DumpPrivKey: returns the wallet-import-format (WIP) private key corresponding to an address.
  * (But does not remove it from the wallet.)
  *
  * https://bitcoin.org/en/developer-reference#dumpprivkey
  */
object DumpPrivKey extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


