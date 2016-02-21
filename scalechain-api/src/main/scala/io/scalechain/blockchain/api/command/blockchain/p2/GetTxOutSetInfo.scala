package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet gettxoutsetinfo

  CLI output :
    {
        "height" : 315293,
        "bestblock" : "00000000c92356f7030b1deeab54b3b02885711320b4c48523be9daa3e0ace5d",
        "transactions" : 771920,
        "txouts" : 2734587,
        "bytes_serialized" : 102629817,
        "hash_serialized" : "4753470fda0145760109e79b8c218a1331e84bb4269d116857b8a4597f109905",
        "total_amount" : 13131746.33839451
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "gettxoutsetinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetTxOutSetInfo: returns statistics about the confirmed unspent transaction output (UTXO) set.
  * Note that this call may take some time and that it only counts outputs from confirmed transactions.
  * it does not count outputs from the memory pool.
  *
  * https://bitcoin.org/en/developer-reference#gettxoutsetinfo
  */
object GetTxOutSetInfo extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
  """gettxoutsetinfo
    |
    |Returns statistics about the unspent transaction output set.
    |Note this call may take some time.
    |
    |Result:
    |{
    |  "height":n,     (numeric) The current block height (index)
    |  "bestblock": "hex",   (string) the best block hash hex
    |  "transactions": n,      (numeric) The number of transactions
    |  "txouts": n,            (numeric) The number of output transactions
    |  "bytes_serialized": n,  (numeric) The serialized size
    |  "hash_serialized": "hash",   (string) The serialized hash
    |  "total_amount": x.xxx          (numeric) The total amount
    |}
    |
    |Examples:
    |> bitcoin-cli gettxoutsetinfo
    |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "gettxoutsetinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    |
  """.stripMargin
}


