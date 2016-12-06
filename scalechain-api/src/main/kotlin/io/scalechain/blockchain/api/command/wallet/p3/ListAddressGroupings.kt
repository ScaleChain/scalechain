package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    bitcoin-cli -testnet listaddressgroupings

  CLI output :
    [
        [
            [
                "mgKgzJ7HR64CrB3zm1B4FUUCLtaSqUKfDb",
                0.00000000
            ],
            [
                "mnUbTmdAFD5EAg3348Ejmonub7JcWtrMck",
                0.00000000,
                "test1"
            ]
        ]
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listaddressgroupings", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListAddressGroupings: lists groups of addresses that
  * may have had their common ownership made public by common use as inputs in the same transaction or
  * from being used as change from a previous transaction.
  *
  * https://bitcoin.org/en/developer-reference#listaddressgroupings
  */
object ListAddressGroupings : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
"""

"""
}


