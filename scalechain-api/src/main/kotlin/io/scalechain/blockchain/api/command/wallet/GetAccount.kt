package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.UnsupportedFeature
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.rawtx.GetRawTransaction
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.proto.HashFormat
import io.scalechain.wallet.Wallet

/*
  CLI command :
    bitcoin-cli -testnet getaccount mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN

  CLI output :
    doc test

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": ["mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAccount: returns the name of the account associated with the given address.
  *
  * Parameter #1 : Address (String;base58, Required)
  *   A P2PKH or P2SH address belonging either to a specific account or the default account (“”).
  *
  * Result: (String)
  *   The name of an account, or an empty string (“”, the default account).
  *   Also return "" if the address was not found in the wallet database.
  *
  *   See the getaccount function of the rpcwallet.cpp of the core implementation for the details.
  *
  * https://bitcoin.org/en/developer-reference#getaccount
  */
object GetAccount : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    handlingException {
      val address: String = request.params.get<String>("Address", 0)

      val coinAddress = CoinAddress.from(address)


      val accountNameOption : Option<String> = Wallet.get.getAccount(coinAddress)(Blockchain.get.db)
      Right(Some(StringResult(accountNameOption.getOrElse(""))))
    }
  }
  fun help() : String =
    """getaccount "bitcoinaddress"
      |
      |DEPRECATED. Returns the account associated with the given address.
      |
      |Arguments:
      |1. "bitcoinaddress"  (string, required) The bitcoin address for account lookup.
      |
      |Result:
      |"accountname"        (string) the account address
      |
      |Examples:
      |> bitcoin-cli getaccount "1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": ["1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


