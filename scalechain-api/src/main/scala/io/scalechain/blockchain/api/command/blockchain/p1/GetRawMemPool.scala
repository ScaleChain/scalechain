package io.scalechain.blockchain.api.command.blockchain.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getrawmempool

  CLI output :
    [
        "2b1f41d6f1837e164d6d6811d3d8dad2e66effbd1058cd9ed7bdbe1cab20ae03",
        "2baa1f49ac9b951fa781c4c95814333a2f3eda71ed3d0245cd76c2829b3ce354"
    ]

  CLI command :
    bitcoin-cli -testnet getrawmempool true

  CLI output :
    {
        "2baa1f49ac9b951fa781c4c95814333a2f3eda71ed3d0245cd76c2829b3ce354" : {
            "size" : 191,
            "fee" : 0.00020000,
            "time" : 1398867772,
            "height" : 227310,
            "startingpriority" : 54545454.54545455,
            "currentpriority" : 54545454.54545455,
            "depends" : [
            ]
        }
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getrawmempool", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetRawMemPool: returns all transaction identifiers (TXIDs) in the memory pool as a JSON array,
  * or detailed information about each transaction in the memory pool as a JSON object.
  *
  * https://bitcoin.org/en/developer-reference#getrawmempool
  */
object GetRawMemPool extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


