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
    bitcoin-cli -testnet getrawchangeaddress

  CLI output :
    mnycUc8FRjJodfKhaj9QBZs2PwxxYoWqaK

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getrawchangeaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetRawChangeAddress: returns a new Bitcoin address for receiving change.
  * This is for use with raw transactions, not normal use.
  *
  * https://bitcoin.org/en/developer-reference#getrawchangeaddress
  */
object GetRawChangeAddress : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """getrawchangeaddress
      |
      |Returns a new Bitcoin address, for receiving change.
      |This is for use with raw transactions, NOT normal use.
      |
      |Result:
      |"address"    (string) The address
      |
      |Examples:
      |> bitcoin-cli getrawchangeaddress
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getrawchangeaddress", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


