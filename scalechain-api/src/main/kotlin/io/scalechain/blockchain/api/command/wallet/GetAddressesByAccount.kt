package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.api.domain.StringListResult
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.transaction.OutputOwnership
import io.scalechain.util.Either
import io.scalechain.util.Either.Right
import io.scalechain.wallet.Wallet

/*
  CLI command :
    bitcoin-cli -testnet getaddressesbyaccount "doc test"

  CLI output :
    [
        "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN",
        "mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma",
        "mmXgiR6KAhZCyQ8ndr2BCfEq1wNG2UnyG6"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaddressbyaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAddressesByAccount: returns a list of every address assigned to a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getaddressesbyaccount
  */
object GetAddressesByAccount : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val account: String = request.params.get<String>("Account", 0)

      val addresses : List<String> = Wallet.get().store.getOutputOwnerships(Blockchain.get().db, account).map { it.stringKey() }

      Right(StringListResult(addresses))
    }
  }
  override fun help() : String =
    """getaddressesbyaccount "account"
      |
      |DEPRECATED. Returns the list of addresses for the given account.
      |
      |Arguments:
      |1. "account"  (string, required) The account name.
      |
      |Result:
      |[                     (json array of string)
      |  "bitcoinaddress"  (string) a bitcoin address associated with the given account
      |  ,...
      |]
      |
      |Examples:
      |> bitcoin-cli getaddressesbyaccount "tabby"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaddressesbyaccount", "params": ["tabby"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


