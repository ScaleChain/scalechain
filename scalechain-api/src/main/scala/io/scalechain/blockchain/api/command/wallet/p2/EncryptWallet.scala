package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet encryptwallet "test"

  CLI output :
    wallet encrypted; Bitcoin server stopping, restart to run with encrypted
    wallet. The keypool has been flushed, you need to make a new backup.

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "encryptwallet", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** EncryptWallet: encrypts the wallet with a passphrase.
  * This is only to enable encryption for the first time.
  * After encryption is enabled, you will need to enter the passphrase to use private keys.
  *
  * https://bitcoin.org/en/developer-reference#encryptwallet
  */
object EncryptWallet extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """encryptwallet "passphrase"
      |
      |Encrypts the wallet with 'passphrase'. This is for first time encryption.
      |After this, any calls that interact with private keys such as sending or signing
      |will require the passphrase to be set prior the making these calls.
      |Use the walletpassphrase call for this, and then walletlock call.
      |If the wallet is already encrypted, use the walletpassphrasechange call.
      |Note that this will shutdown the server.
      |
      |Arguments:
      |1. "passphrase"    (string) The pass phrase to encrypt the wallet with. It must be at least 1 character, but should be long.
      |
      |Examples:
      |
      |Encrypt you wallet
      |> bitcoin-cli encryptwallet "my pass phrase"
      |
      |Now set the passphrase to use the wallet, such as for signing or sending bitcoin
      |> bitcoin-cli walletpassphrase "my pass phrase"
      |
      |Now we can so something like sign
      |> bitcoin-cli signmessage "bitcoinaddress" "test message"
      |
      |Now lock the wallet again by removing the passphrase
      |> bitcoin-cli walletlock
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "encryptwallet", "params": ["my pass phrase"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


