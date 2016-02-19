package io.scalechain.blockchain.api.command.rawtx.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet createrawtransaction '''
      [
        {
          "txid": "1eb590cd06127f78bf38ab4140c4cdce56ad9eb8886999eb898ddf4d3b28a91d",
          "vout" : 0
        }
      ]''' '{ "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe": 0.13 }'

  CLI output(wrapped) :
    01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f12\
    06cd90b51e0000000000ffffffff01405dc600000000001976a9140dfc8bafc8\
    419853b34d5e072ad37d1a5159f58488ac00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "createrawtransaction", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** CreateRawTransaction: creates an unsigned serialized transaction that spends a previous output
  * to a new output with a P2PKH or P2SH address.
  *
  * The transaction is not stored in the wallet or transmitted to the network.
  *
  * https://bitcoin.org/en/developer-reference#createrawtransaction
  */
object CreateRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


