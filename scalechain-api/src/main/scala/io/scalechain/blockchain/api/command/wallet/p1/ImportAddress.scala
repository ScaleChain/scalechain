package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet importaddress \
      muhtvdmsnbQEPFuEmxcChX58fGvXaaUoVt "watch-only test" true

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "importaddress", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** ImportAddress: adds an address or pubkey script to the wallet without the associated private key,
  * allowing you to watch for transactions affecting that address or
  * pubkey script without being able to spend any of its outputs.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#importaddress
  */
object ImportAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


