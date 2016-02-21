package io.scalechain.blockchain.api.command.blockchain.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

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
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getrawmempool ( verbose )
      |
      |Returns all transaction ids in memory pool as a json array of string transaction ids.
      |
      |Arguments:
      |1. verbose           (boolean, optional, default=false) true for a json object, false for array of transaction ids
      |
      |Result: (for verbose = false):
      |[                     (json array of string)
      |  "transactionid"     (string) The transaction id
      |  ,...
      |]
      |
      |Result: (for verbose = true):
      |{                           (json object)
      |  "transactionid" : {       (json object)
      |    "size" : n,             (numeric) transaction size in bytes
      |    "fee" : n,              (numeric) transaction fee in BTC
      |    "modifiedfee" : n,      (numeric) transaction fee with fee deltas used for mining priority
      |    "time" : n,             (numeric) local time transaction entered pool in seconds since 1 Jan 1970 GMT
      |    "height" : n,           (numeric) block height when transaction entered pool
      |    "startingpriority" : n, (numeric) priority when transaction entered pool
      |    "currentpriority" : n,  (numeric) transaction priority now
      |    "descendantcount" : n,  (numeric) number of in-mempool descendant transactions (including this one)
      |    "descendantsize" : n,   (numeric) size of in-mempool descendants (including this one)
      |    "descendantfees" : n,   (numeric) modified fees (see above) of in-mempool descendants (including this one)
      |    "depends" : [           (array) unconfirmed transactions used as inputs for this transaction
      |        "transactionid",    (string) parent transaction id
      |       ... ]
      |  }, ...
      |}
      |
      |Examples
      |> bitcoin-cli getrawmempool true
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getrawmempool", "params": [true] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.stripMargin
}


