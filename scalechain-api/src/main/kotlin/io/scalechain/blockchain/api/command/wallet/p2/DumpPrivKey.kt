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
    bitcoin-cli -testnet dumpprivkey moQR7i8XM4rSGoNwEsw3h4YEuduuP6mxw7

  CLI output :
    cTVNtBK7mBi2yc9syEnwbiUpnpGJKohDWzXMeF4tGKAQ7wvomr95

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "dumprivkey", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** DumpPrivKey: returns the wallet-import-format (WIP) private key corresponding to an address.
  * (But does not remove it from the wallet.)
  *
  * https://bitcoin.org/en/developer-reference#dumpprivkey
  */
object DumpPrivKey : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """dumpprivkey "bitcoinaddress"
      |
      |Reveals the private key corresponding to 'bitcoinaddress'.
      |Then the importprivkey can be used with this output
      |
      |Arguments:
      |1. "bitcoinaddress"   (string, required) The bitcoin address for the private key
      |
      |Result:
      |"key"                (string) The private key
      |
      |Examples:
      |> bitcoin-cli dumpprivkey "myaddress"
      |> bitcoin-cli importprivkey "mykey"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "dumpprivkey", "params": ["myaddress"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


