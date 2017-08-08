package io.scalechain.blockchain.api.command.rawtx.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

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
  * to a output with a P2PKH or P2SH address.
  *
  * The transaction is not stored in the wallet or transmitted to the network.
  *
  * https://bitcoin.org/en/developer-reference#createrawtransaction
  */
object CreateRawTransaction : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """createrawtransaction [{"txid":"id","vout":n},...] {"address":amount,"data":"hex",...} ( locktime )
      |
      |Create a transaction spending the given inputs and creating outputs.
      |Outputs can be addresses or data.
      |Returns hex-encoded raw transaction.
      |Note that the transaction's inputs are not signed, and
      |it is not stored in the wallet or transmitted to the network.
      |
      |Arguments:
      |1. "transactions"        (string, required) A json array of json objects
      |     [
      |       {
      |         "txid":"id",    (string, required) The transaction id
      |         "vout":n        (numeric, required) The output number
      |       }
      |       ,...
      |     ]
      |2. "outputs"             (string, required) a json object with outputs
      |    {
      |      "address": x.xxx   (numeric or string, required) The key is the bitcoin address, the numeric value (can be string) is the BTC amount
      |      "data": "hex",     (string, required) The key is "data", the value is hex encoded data
      |      ...
      |    }
      |3. locktime                (numeric, optional, default=0) Raw locktime. Non-0 value also locktime-activates inputs
      |
      |Result:
      |"transaction"            (string) hex string of the transaction
      |
      |Examples
      |> bitcoin-cli createrawtransaction "[{\"txid\":\"myid\",\"vout\":0}]" "{\"address\":0.01}"
      |> bitcoin-cli createrawtransaction "[{\"txid\":\"myid\",\"vout\":0}]" "{\"data\":\"00010203\"}"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "createrawtransaction", "params": ["[{\"txid\":\"myid\",\"vout\":0}]", "{\"address\":0.01}"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "createrawtransaction", "params": ["[{\"txid\":\"myid\",\"vout\":0}]", "{\"data\":\"00010203\"}"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


