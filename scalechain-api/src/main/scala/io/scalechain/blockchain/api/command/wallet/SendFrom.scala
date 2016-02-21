package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Spend 0.1 coins from the account “test” to the address indicated below
    # using only UTXOs with at least six confirmations,
    # giving the transaction the comment “Example spend”
    # and labeling the spender “Example.com”.
    bitcoin-cli -testnet sendfrom "test" \
      mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe \
      0.1 \
      6 \
      "Example spend" \
      "Example.com"

  CLI output :
    f14ee5368c339644d3037d929bbe1f1544a532f8826c7b7288cb994b0b0ff5d8

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "sendfrom", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class SendFromResult(
) extends RpcResult

/** SendFrom: spends an amount from a local account to a bitcoin address.
  *
  * https://bitcoin.org/en/developer-reference#sendfrom
  */
object SendFrom extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """sendfrom "fromaccount" "tobitcoinaddress" amount ( minconf "comment" "comment-to" )
      |
      |DEPRECATED (use sendtoaddress). Sent an amount from an account to a bitcoin address.
      |
      |Arguments:
      |1. "fromaccount"       (string, required) The name of the account to send funds from. May be the default account using "".
      |2. "tobitcoinaddress"  (string, required) The bitcoin address to send funds to.
      |3. amount                (numeric or string, required) The amount in BTC (transaction fee is added on top).
      |4. minconf               (numeric, optional, default=1) Only use funds with at least this many confirmations.
      |5. "comment"           (string, optional) A comment used to store what the transaction is for.
      |                                     This is not part of the transaction, just kept in your wallet.
      |6. "comment-to"        (string, optional) An optional comment to store the name of the person or organization
      |                                     to which you're sending the transaction. This is not part of the transaction,
      |                                     it is just kept in your wallet.
      |
      |Result:
      |"transactionid"        (string) The transaction id.
      |
      |Examples:
      |
      |Send 0.01 BTC from the default account to the address, must have at least 1 confirmation
      |> bitcoin-cli sendfrom "" "1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd" 0.01
      |
      |Send 0.01 from the tabby account to the given address, funds must have at least 6 confirmations
      |> bitcoin-cli sendfrom "tabby" "1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd" 0.01 6 "donation" "seans outpost"
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "sendfrom", "params": ["tabby", "1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd", 0.01, 6, "donation", "seans outpost"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


