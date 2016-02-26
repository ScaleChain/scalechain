package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.rawtx.GetRawTransaction._
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.{HashFormat, Hash}
import spray.json.DefaultJsonProtocol._

/*
  CLI command :
    # Get all outputs confirmed at least 6 times for a particular address.
    bitcoin-cli -testnet listunspent 6 99999999 '''
      [
        "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"
      ]
    '''

  CLI output :
    [
        {
            "txid" : "d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a",
            "vout" : 1,
            "address" : "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe",
            "account" : "test label",
            "scriptPubKey" : "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
            "amount" : 0.00010000,
            "confirmations" : 6210,
            "spendable" : true
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listunspent", "params": [6, 99999999, ["mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"] ] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class UnspentCoin(
  // The TXID of the transaction containing the output, encoded as hex in RPC byte order
  txid          : Hash,                  // "d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a",
  // The output index number (vout) of the output within its containing transaction
  vout          : Int,                   // 1,
  // The P2PKH or P2SH address the output paid. Only returned for P2PKH or P2SH output scripts
  address       : Option[String],        // "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe",
  // If the address returned belongs to an account, this is the account. Otherwise not returned
  account       : Option[String],        // "test label",
  // The output script paid, encoded as hex
  scriptPubKey  : String,                // "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
  // If the output is a P2SH whose script belongs to this wallet, this is the redeem script
  redeemScript  : Option[String],
  // The amount paid to the output in bitcoins
  amount        : scala.math.BigDecimal, // 0.00010000,
  // The number of confirmations received for the transaction containing this output
  confirmations : Long,                  // 6210,
  // ( Since : 0.10.0 )
  // Set to true if the private key or keys needed to spend this output are part of the wallet.
  // Set to false if not (such as for watch-only addresses)
  spendable     : Boolean                // true
)

case class ListUnspentResult( unspentCoins : List[UnspentCoin] )  extends RpcResult

/** ListUnspent: returns an array of unspent transaction outputs belonging to this wallet.
  *
  * Updated in 0.10.0
  *
  * Parameter #1 : Minimum Confirmations (Number;int, Optional)
  *   The minimum number of confirmations the transaction containing an output must have in order to be returned.
  *   Use 0 to return outputs from unconfirmed transactions. Default is 1.
  *
  * Parameter #2 : Maximum Confirmations (Number;int, Optional)
  *   The maximum number of confirmations the transaction containing an output may have in order to be returned.
  *   Default is 9999999 (~10 million)
  *
  * Parameter #3 : Addresses (Array, Optional)
  *   If present, only outputs which pay an address in this array will be returned.
  *
  *   Array item : (String;base58)
  *     	A P2PKH or P2SH address.
  *
  * Result: (Array)
  *   An array of objects each describing an unspent output. May be empty.
  *
  * https://bitcoin.org/en/developer-reference#listunspent
  */
object ListUnspent extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {

    handlingException {
      val minimumConfirmations: Long                 = request.params.getOption[Long]("Minimum Confirmations", 0).getOrElse(1L)
      val maximumConfirmations: Long                 = request.params.getOption[Long]("Maximum Confirmations", 1).getOrElse(9999999L)
      val addressesOption     : Option[List[String]] = request.params.getListOption[String]("Addresses", 2)

      // TODO : Implement

      // A list of objects each describing an unspent output. May be empty
      // item of the list : An object describing a particular unspent output belonging to this wallet
      val unspentCoins =
        List(
          UnspentCoin(
            txid = Hash("d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a"),
            vout = 1,
            address = Some("mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"),
            account = Some("test label"),
            scriptPubKey = "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
            redeemScript = None,
            amount = 0.00010000,
            confirmations = 6210,
            spendable = true
          )
        )

      Right(Some(ListUnspentResult(unspentCoins)))
    }
  }
  def help() : String =
    """listunspent ( minconf maxconf  ["address",...] )
      |
      |Returns array of unspent transaction outputs
      |with between minconf and maxconf (inclusive) confirmations.
      |Optionally filter to only include txouts paid to specified addresses.
      |Results are an array of Objects, each of which has:
      |{txid, vout, scriptPubKey, amount, confirmations}
      |
      |Arguments:
      |1. minconf          (numeric, optional, default=1) The minimum confirmations to filter
      |2. maxconf          (numeric, optional, default=9999999) The maximum confirmations to filter
      |3. "addresses"    (string) A json array of bitcoin addresses to filter
      |    [
      |      "address"   (string) bitcoin address
      |      ,...
      |    ]
      |
      |Result
      |[                   (array of json object)
      |  {
      |    "txid" : "txid",        (string) the transaction id
      |    "vout" : n,               (numeric) the vout value
      |    "address" : "address",  (string) the bitcoin address
      |    "account" : "account",  (string) DEPRECATED. The associated account, or "" for the default account
      |    "scriptPubKey" : "key", (string) the script key
      |    "amount" : x.xxx,         (numeric) the transaction amount in BTC
      |    "confirmations" : n       (numeric) The number of confirmations
      |  }
      |  ,...
      |]
      |
      |Examples
      |> bitcoin-cli listunspent
      |> bitcoin-cli listunspent 6 9999999 "[\"1PGFqEzfmQch1gKD3ra4k18PNj3tTUUSqg\",\"1LtvqCaApEdUGFkpKMM4MstjcaL4dKg8SP\"]"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "listunspent", "params": [6, 9999999 "[\"1PGFqEzfmQch1gKD3ra4k18PNj3tTUUSqg\",\"1LtvqCaApEdUGFkpKMM4MstjcaL4dKg8SP\"]"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


