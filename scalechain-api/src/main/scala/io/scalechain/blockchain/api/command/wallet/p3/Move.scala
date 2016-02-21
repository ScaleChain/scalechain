package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Move 0.1 bitcoins from “doc test” to “test1”, giving the transaction the comment “Example move”.
    bitcoin-cli -testnet move "doc test" "test1" 0.1 0 "Example move"

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "move", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Move: moves a specified amount from one account in your wallet to another using an off-block-chain transaction.
  *
  * https://bitcoin.org/en/developer-reference#move
  */
object Move extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """move "fromaccount" "toaccount" amount ( minconf "comment" )
      |
      |DEPRECATED. Move a specified amount from one account in your wallet to another.
      |
      |Arguments:
      |1. "fromaccount"   (string, required) The name of the account to move funds from. May be the default account using "".
      |2. "toaccount"     (string, required) The name of the account to move funds to. May be the default account using "".
      |3. amount            (numeric) Quantity of BTC to move between accounts.
      |4. minconf           (numeric, optional, default=1) Only use funds with at least this many confirmations.
      |5. "comment"       (string, optional) An optional comment, stored in the wallet only.
      |
      |Result:
      |true|false           (boolean) true if successful.
      |
      |Examples:
      |
      |Move 0.01 BTC from the default account to the account named tabby
      |> bitcoin-cli move "" "tabby" 0.01
      |
      |Move 0.01 BTC timotei to akiko with a comment and funds have 6 confirmations
      |> bitcoin-cli move "timotei" "akiko" 0.01 6 "happy birthday!"
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "move", "params": ["timotei", "akiko", 0.01, 6, "happy birthday!"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


