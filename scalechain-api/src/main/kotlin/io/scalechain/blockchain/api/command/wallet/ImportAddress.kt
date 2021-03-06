package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.transaction.ParsedPubKeyScript
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.ScriptParseException
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.UnsupportedFeature
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import io.scalechain.wallet.Wallet
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    bitcoin-cli -testnet importaddress \
      muhtvdmsnbQEPFuEmxcChX58fGvXaaUoVt "watch-only test" true

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "importaddress", "params": ["muhtvdmsnbQEPFuEmxcChX58fGvXaaUoVt", "watch-only test", true] }

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
object ImportAddress : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val scriptOrAddress  : String = request.params.get<String>("Script", 0)
      val account          : String = request.params.getOption<String>("Account" , 1) ?: ""
      val rescanBlockchain : Boolean = request.params.getOption<Boolean>("Rescan Blockchain", 2) ?: true
///      val p2sh  : Boolean = request.params.getOption<Boolean>("Allow P2SH Scripts"  , 3) ?: false

      val coinOwnership =
        // Step 1 : Check if it is an address.
        try {
          CoinAddress.from(scriptOrAddress)
        } catch(e : GeneralException) {
          // Step 2 : Check if it is a public key hash script.
          val scriptBytes = HexUtil.bytes(scriptOrAddress)
          try {
            ParsedPubKeyScript( ScriptParser.parse(LockingScript(Bytes(scriptBytes))) )
          } catch(e : ScriptParseException) {
            // Step 3 : If it is neither an address nor an script, throw an exception.
            // The RpcInvalidAddress is converted to an RPC error, RPC_INVALID_ADDRESS_OR_KEY by handlingException.
            throw GeneralException(ErrorCode.RpcInvalidAddress)
          }
        }

      Wallet.get().importOutputOwnership(
        Blockchain.get().db,
        Blockchain.get(),
        account,
        coinOwnership,
        rescanBlockchain
      )

      // None is converted to JsNull, so we will have { result : null .. } within the response json.
      Right(null)
    }
  }

  // BUGBUG : Add fourth parameter.
  // 4. p2sh                 (boolean, optional, default=false) Add the P2SH version of the script as well

  override fun help() : String =
    """importaddress "address" ( "label" rescan p2sh )
      |
      |Adds a script (in hex) or address that can be watched as if it were in your wallet but cannot be used to spend.
      |
      |Arguments:
      |1. "script"           (string, required) The hex-encoded script (or address)
      |2. "label"            (string, optional, default="") An optional label
      |3. rescan             (boolean, optional, default=true) Rescan the wallet for transactions
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
    """.trimMargin()
}


