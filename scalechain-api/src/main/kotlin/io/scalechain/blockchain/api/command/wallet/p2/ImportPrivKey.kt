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
    bitcoin-cli -testnet importprivkey \
      cU8Q2jGeX3GNKNa5etiC8mgEgFSeVUTRQfWE2ZCzszyqYNK4Mepy \
      "test label" \
      true

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "importprivkey", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** ImportPrivKey: adds a private key to your wallet.
  * The key should be formatted in the wallet import format created by the dumpprivkey RPC.
  *
  * https://bitcoin.org/en/developer-reference#importprivkey
  */
object ImportPrivKey : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """importprivkey "bitcoinprivkey" ( "label" rescan )
      |
      |Adds a private key (as returned by dumpprivkey) to your wallet.
      |
      |Arguments:
      |1. "bitcoinprivkey"   (string, required) The private key (see dumpprivkey)
      |2. "label"            (string, optional, default="") An optional label
      |3. rescan               (boolean, optional, default=true) Rescan the wallet for transactions
      |
      |Note: This call can take minutes to complete if rescan is true.
      |
      |Examples:
      |
      |Dump a private key
      |> bitcoin-cli dumpprivkey "myaddress"
      |
      |Import the private key with rescan
      |> bitcoin-cli importprivkey "mykey"
      |
      |Import using a label and without rescan
      |> bitcoin-cli importprivkey "mykey" "testing" false
      |
      |As a JSON-RPC call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "importprivkey", "params": ["mykey", "testing", false] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


