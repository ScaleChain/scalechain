package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getwalletinfo

  CLI output :
    {
        "walletversion" : 60000,
        "balance" : 1.45060000,
        "txcount" : 17,
        "keypoololdest" : 1398809500,
        "keypoolsize" : 196,
        "unlocked_until" : 0
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getwalletinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetWalletInfo: provides information about the wallet.
  *
  * Since - New in 0.9.2
  *
  * https://bitcoin.org/en/developer-reference#getwalletinfo
  */
object GetWalletInfo extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """getwalletinfo
      |Returns an object containing various wallet state info.
      |
      |Result:
      |{
      |  "walletversion": xxxxx,     (numeric) the wallet version
      |  "balance": xxxxxxx,         (numeric) the total confirmed balance of the wallet in BTC
      |  "unconfirmed_balance": xxx, (numeric) the total unconfirmed balance of the wallet in BTC
      |  "immature_balance": xxxxxx, (numeric) the total immature balance of the wallet in BTC
      |  "txcount": xxxxxxx,         (numeric) the total number of transactions in the wallet
      |  "keypoololdest": xxxxxx,    (numeric) the timestamp (seconds since GMT epoch) of the oldest pre-generated key in the key pool
      |  "keypoolsize": xxxx,        (numeric) how many new keys are pre-generated
      |  "unlocked_until": ttt,      (numeric) the timestamp in seconds since epoch (midnight Jan 1 1970 GMT) that the wallet is unlocked for transfers, or 0 if the wallet is locked
      |  "paytxfee": x.xxxx,         (numeric) the transaction fee configuration, set in BTC/kB
      |}
      |
      |Examples:
      |> bitcoin-cli getwalletinfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getwalletinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


