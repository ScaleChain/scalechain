package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{StringResult, RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.ByteArray

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
    {"jsonrpc": "1.0", "id":" ", "method": "sendfrom", "params": ["mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe", 0.1, 6, "Example spend", "Example.com"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/


/** SendFrom: spends an amount from a local account to a bitcoin address.
  *
  * Parameter #1 : From Account (String, Required)
  *   The name of the account from which the coins should be spent.
  *   Use an empty string (“”) for the default account.
  *
  * Parameter #2 : To Address (String , Required)
  *   A P2PKH or P2SH address to which the coins should be sent.
  *
  * Parameter #3 : Amount (Number;coins, Required)
  *   The amount to spend in coins. We will ensure the account has sufficient coins to pay this amount
  *   (but the transaction fee paid is not included in the calculation,
  *    so an account can spend a total of its balance plus the transaction fee)
  *
  * Parameter #4 : Confirmations (Number;int, Optional)
  *   The minimum number of confirmations an incoming transaction must have
  *   for its outputs to be credited to this account’s balance.
  *
  *   Outgoing transactions are always counted, as are move transactions made with the move RPC.
  *   If an account doesn’t have a balance high enough to pay for this transaction,
  *   the payment will be rejected. Use 0 to spend unconfirmed incoming payments. Default is 1.
  *
  *   Warning : if account1 receives an unconfirmed payment and transfers it to account2 with the move RPC,
  *   account2 will be able to spend those bitcoins even if this parameter is set to 1 or higher.
  *
  * Parameter #5 : Comment (String, Optional)
  *   A locally-stored (not broadcast) comment assigned to this transaction. Default is no comment.
  *
  * Parameter #6 : Comment To (String, Optional)
  *   A locally-stored (not broadcast) comment assigned to this transaction.
  *   Meant to be used for describing who the payment was sent to. Default is no comment.
  *
  * Result: (String)
  *   The TXID of the sent transaction, encoded as hex in RPC byte order.
  *
  * https://bitcoin.org/en/developer-reference#sendfrom
  */
object SendFrom extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    val transactionHash = Hash("f14ee5368c339644d3037d929bbe1f1544a532f8826c7b7288cb994b0b0ff5d8")

    Right(Some(StringResult(ByteArray.byteArrayToString(transactionHash.value))))
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


