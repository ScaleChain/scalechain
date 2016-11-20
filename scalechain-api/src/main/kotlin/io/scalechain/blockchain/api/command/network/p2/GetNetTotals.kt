package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getnettotals

  CLI output :
    {
        "totalbytesrecv" : 723992206,
        "totalbytessent" : 16846662695,
        "timemillis" : 1419268217354
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnettotals", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNetTotals: returns information about network traffic,
  * including bytes in, bytes out, and the current time.
  *
  * https://bitcoin.org/en/developer-reference#getnettotals
  */
object GetNetTotals : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getnettotals
      |
      |Returns information about network traffic, including bytes in, bytes out,
      |and current time.
      |
      |Result:
      |{
      |  "totalbytesrecv": n,   (numeric) Total bytes received
      |  "totalbytessent": n,   (numeric) Total bytes sent
      |  "timemillis": t,       (numeric) Total cpu time
      |  "uploadtarget":
      |  {
      |    "timeframe": n,                         (numeric) Length of the measuring timeframe in seconds
      |    "target": n,                            (numeric) Target in bytes
      |    "target_reached": true|false,           (boolean) True if target is reached
      |    "serve_historical_blocks": true|false,  (boolean) True if serving historical blocks
      |    "bytes_left_in_cycle": t,               (numeric) Bytes left in current time cycle
      |    "time_left_in_cycle": t                 (numeric) Seconds left in current time cycle
      |  }
      |}
      |
      |Examples:
      |> bitcoin-cli getnettotals
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getnettotals", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


