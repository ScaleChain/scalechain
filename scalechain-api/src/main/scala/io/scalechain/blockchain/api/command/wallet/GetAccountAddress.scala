package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Get an address for the default account.
    bitcoin-cli -testnet getaccountaddress ""

  CLI output :
    msQyFNYHkFUo4PG3puJBbpesvRCyRQax7r

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaccountaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetAccountAddressResult(
) extends RpcResult


/** GetAccountAddress: returns the current Bitcoin address for receiving payments to this account.
  * If the account doesn’t exist, it creates both the account and a new address for receiving payment.
  * Once a payment has been received to an address, future calls to this RPC for the same account will return a different address.
  *
  * https://bitcoin.org/en/developer-reference#getaccountaddress
  */
object GetAccountAddress extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getaccountaddress "account"
      |
      |DEPRECATED. Returns the current Bitcoin address for receiving payments to this account.
      |
      |Arguments:
      |1. "account"       (string, required) The account name for the address. It can also be set to the empty string "" to represent the default account. The account does not need to exist, it will be created and a new address created  if there is no account by the given name.
      |
      |Result:
      |"bitcoinaddress"   (string) The account bitcoin address
      |
      |Examples:
      |> bitcoin-cli getaccountaddress
      |> bitcoin-cli getaccountaddress ""
      |> bitcoin-cli getaccountaddress "myaccount"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaccountaddress", "params": ["myaccount"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


