package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    # Sign a the message “Hello, World!” using the following address.
    bitcoin-cli -testnet signmessage mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe \
      'Hello, World!'

  CLI output :
    IL98ziCmwYi5pL+dqKp4Ux+zCa4hP/xbjHmWh+Mk/lefV/0pWV1p/gQ94jgExSmgH2/+PDcCCrOHAady2IEySSI=

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "signmessage", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SignMessage: signs a message with the private key of an address.
  *
  * https://bitcoin.org/en/developer-reference#signmessage
  */
object SignMessage : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """signmessage "bitcoinaddress" "message"
      |
      |Sign a message with the private key of an address
      |
      |Arguments:
      |1. "bitcoinaddress"  (string, required) The bitcoin address to use for the private key.
      |2. "message"         (string, required) The message to create a signature of.
      |
      |Result:
      |"signature"          (string) The signature of the message encoded in base 64
      |
      |Examples:
      |
      |Unlock the wallet for 30 seconds
      |> bitcoin-cli walletpassphrase "mypassphrase" 30
      |
      |Create the signature
      |> bitcoin-cli signmessage "1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ" "my message"
      |
      |Verify the signature
      |> bitcoin-cli verifymessage "1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ" "signature" "my message"
      |
      |As json rpc
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "signmessage", "params": ["1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ", "my message"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


