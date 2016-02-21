package io.scalechain.blockchain.api.command.utility.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Check the signature on the message created in the example for signmessage.
    bitcoin-cli -testnet verifymessage \
      mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe \
      IL98ziCmwYi5pL+dqKp4Ux+zCa4hP/xbjHmWh+Mk/lefV/0pWV1p/gQ94jgExSmgH2/+PDcCCrOHAady2IEySSI= \
      'Hello, World!'

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "verifymessage", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** VerifyMessage: verifies a signed message.
  *
  * https://bitcoin.org/en/developer-reference#verifymessage
  */
object VerifyMessage extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
"""

"""
}


