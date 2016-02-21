package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet importaddress \
      muhtvdmsnbQEPFuEmxcChX58fGvXaaUoVt "watch-only test" true

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "importaddress", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** ImportAddress: adds an address or pubkey script to the wallet without the associated private key,
  * allowing you to watch for transactions affecting that address or
  * pubkey script without being able to spend any of its outputs.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#importaddress
  */
object ImportAddress extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """importaddress "address" ( "label" rescan p2sh )
      |
      |Adds a script (in hex) or address that can be watched as if it were in your wallet but cannot be used to spend.
      |
      |Arguments:
      |1. "script"           (string, required) The hex-encoded script (or address)
      |2. "label"            (string, optional, default="") An optional label
      |3. rescan               (boolean, optional, default=true) Rescan the wallet for transactions
      |4. p2sh                 (boolean, optional, default=false) Add the P2SH version of the script as well
      |
      |Note: This call can take minutes to complete if rescan is true.
      |If you have the full public key, you should call importpublickey instead of this.
      |
      |Examples:
      |
      |Import a script with rescan
      |> bitcoin-cli importaddress "myscript"
      |
      |Import using a label without rescan
      |> bitcoin-cli importaddress "myscript" "testing" false
      |
      |As a JSON-RPC call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "importaddress", "params": ["myscript", "testing", false] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin

}


