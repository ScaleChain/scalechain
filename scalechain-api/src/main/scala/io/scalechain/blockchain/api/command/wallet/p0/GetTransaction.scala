package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet gettransaction \
      5a7d24cd665108c66b2d56146f244932edae4e2376b561b3d396d5ae017b9589

  CLI output :
    {
        "amount" : 0.00000000,
        "fee" : 0.00000000,
        "confirmations" : 106670,
        "blockhash" : "000000008b630b3aae99b6fe215548168bed92167c47a2f7ad4df41e571bcb51",
        "blockindex" : 1,
        "blocktime" : 1396321351,
        "txid" : "5a7d24cd665108c66b2d56146f244932edae4e2376b561b3d396d5ae017b9589",
        "walletconflicts" : [
        ],
        "time" : 1396321351,
        "timereceived" : 1418924711,
        "details" : [
            {
                "account" : "",
                "address" : "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN",
                "category" : "send",
                "amount" : -0.10000000,
                "vout" : 0,
                "fee" : 0.00000000
            },
            {
                "account" : "doc test",
                "address" : "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN",
                "category" : "receive",
                "amount" : 0.10000000,
                "vout" : 0
            }
        ],
        "hex" : "0100000001cde58f2e37d000eabbb60d9cf0b79ddf67cede6dba58732539983fa341dd5e6c010000006a47304402201feaf12908260f666ab369bb8753cdc12f78d0c8bdfdef997da17acff502d321022049ba0b80945a7192e631c03bafd5c6dc3c7cb35ac5c1c0ffb9e22fec86dd311c01210321eeeb46fd878ce8e62d5e0f408a0eab41d7c3a7872dc836ce360439536e423dffffffff0180969800000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac00000000"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getransaction", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetTransaction: gets detailed information about an in-wallet transaction.
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#gettransaction
  */
object GetTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


