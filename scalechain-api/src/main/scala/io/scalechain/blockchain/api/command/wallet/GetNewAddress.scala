package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Create a new address in the “doc test” account.
    bitcoin-cli -testnet getnewaddress "doc test"

  CLI output :
    mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnewaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetNewAddressResult(
) extends RpcResult


/** GetNewAddress: returns a new Bitcoin address for receiving payments.
  * If an account is specified, payments received with the address will be credited to that account.
  *
  * https://bitcoin.org/en/developer-reference#getnewaddress
  */
object GetNewAddress extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getnewaddress ( "account" )
      |
      |Returns a new Bitcoin address for receiving payments.
      |If 'account' is specified (DEPRECATED), it is added to the address book
      |so payments received with the address will be credited to 'account'.
      |
      |Arguments:
      |1. "account"        (string, optional) DEPRECATED. The account name for the address to be linked to. If not provided, the default account "" is used. It can also be set to the empty string "" to represent the default account. The account does not need to exist, it will be created if there is no account by the given name.
      |
      |Result:
      |"bitcoinaddress"    (string) The new bitcoin address
      |
      |Examples:
      |> bitcoin-cli getnewaddress
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getnewaddress", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin

}


