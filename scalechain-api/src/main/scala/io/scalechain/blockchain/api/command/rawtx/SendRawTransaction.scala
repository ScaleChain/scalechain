package io.scalechain.blockchain.api.command.rawtx

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Broadcast a signed transaction
    bitcoin-cli -testnet sendrawtransaction 01000000011da9283b4ddf8d\
      89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a4730\
      4402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a\
      0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405\
      ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a\
      2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc84198\
      53b34d5e072ad37d1a5159f58488ac0000000

  CLI output :
    f5a5ce5988cc72b9b90e8d1d6c910cda53c88d2175177357cc2f2cf0899fbaad

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "sendrawtransaction", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SendRawTransaction: validates a transaction and broadcasts it to the peer-to-peer network.
  *
  * https://bitcoin.org/en/developer-reference#sendrawtransaction
  */
object SendRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


