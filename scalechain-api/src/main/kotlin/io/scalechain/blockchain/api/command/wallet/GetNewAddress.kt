package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.wallet.Wallet
import io.scalechain.util.Either
import io.scalechain.util.Either.Right

/*
  CLI command :
    # Create a new address in the “doc test” account.
    bitcoin-cli -testnet getnewaddress "doc test"

  CLI output :
    mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnewaddress", "params": ["doc test"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNewAddress: returns a new coin address for receiving payments.
  * If an account is specified, payments received with the address will be credited to that account.
  *
  * Parameter #1 : Account (String, Optional)
  *   The name of the account to put the address in.
  *   The default is the default account, an empty string (“”)
  *
  * Result: (String;base58)
  *   A P2PKH address which has not previously been returned by this RPC.
  *   The address will be marked as a receiving address in the wallet.
  *
  *   The address may already have been part of the keypool,
  *   so other RPCs such as the dumpwallet RPC may have disclosed it previously.
  *
  *   If the wallet is unlocked, its keypool will also be filled to its max (by default, 100 unused keys).
  *   If the wallet is locked and its keypool is empty, this RPC will fail.
  *
  * https://bitcoin.org/en/developer-reference#getnewaddress
  */
object GetNewAddress : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val account: String = request.params.getOption<String>("Account", 0) ?: ""

      val newCoinAddress : CoinAddress = Wallet.get().newAddress(Blockchain.get().db, account)

      Right(StringResult(newCoinAddress.base58()))
    }
  }
  override fun help() : String =
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
      |"bitcoinaddress"    (string) The bitcoin address
      |
      |Examples:
      |> bitcoin-cli getnewaddress
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getnewaddress", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()

}


