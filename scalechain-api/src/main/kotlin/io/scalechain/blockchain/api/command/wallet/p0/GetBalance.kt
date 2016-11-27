package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet getbalance "test1" 1 true

  CLI output :
    1.99900000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getbalance", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBalance: gets the balance in decimal bitcoins across all accounts or for a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getbalance
  */
object GetBalance : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getbalance ( "account" minconf includeWatchonly )
      |
      |If account is not specified, returns the server's total available balance.
      |If account is specified (DEPRECATED), returns the balance in the account.
      |Note that the account "" is not the same as leaving the parameter out.
      |The server total may be different to the balance in the default "" account.
      |
      |Arguments:
      |1. "account"      (string, optional) DEPRECATED. The selected account, or "*" for entire wallet. It may be the default account using "".
      |2. minconf          (numeric, optional, default=1) Only include transactions confirmed at least this many times.
      |3. includeWatchonly (bool, optional, default=false) Also include balance in watchonly addresses (see 'importaddress')
      |
      |Result:
      |amount              (numeric) The total amount in BTC received for this account.
      |
      |Examples:
      |
      |The total amount in the wallet
      |> bitcoin-cli getbalance
      |
      |The total amount in the wallet at least 5 blocks confirmed
      |> bitcoin-cli getbalance "*" 6
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getbalance", "params": ["*", 6] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


