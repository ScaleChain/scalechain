package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

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
object GetTransaction : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """gettransaction "txid" ( includeWatchonly )
      |
      |Get detailed information about in-wallet transaction <txid>
      |
      |Arguments:
      |1. "txid"    (string, required) The transaction id
      |2. "includeWatchonly"    (bool, optional, default=false) Whether to include watchonly addresses in balance calculation and details[]
      |
      |Result:
      |{
      |  "amount" : x.xxx,        (numeric) The transaction amount in BTC
      |  "confirmations" : n,     (numeric) The number of confirmations
      |  "blockhash" : "hash",  (string) The block hash
      |  "blockindex" : xx,       (numeric) The block index
      |  "blocktime" : ttt,       (numeric) The time in seconds since epoch (1 Jan 1970 GMT)
      |  "txid" : "transactionid",   (string) The transaction id.
      |  "time" : ttt,            (numeric) The transaction time in seconds since epoch (1 Jan 1970 GMT)
      |  "timereceived" : ttt,    (numeric) The time received in seconds since epoch (1 Jan 1970 GMT)
      |  "bip125-replaceable": "yes|no|unknown"  (string) Whether this transaction could be replaced due to BIP125 (replace-by-fee);
      |                                                   may be unknown for unconfirmed transactions not in the mempool
      |  "details" : [
      |    {
      |      "account" : "accountname",  (string) DEPRECATED. The account name involved in the transaction, can be "" for the default account.
      |      "address" : "bitcoinaddress",   (string) The bitcoin address involved in the transaction
      |      "category" : "send|receive",    (string) The category, either 'send' or 'receive'
      |      "amount" : x.xxx,                 (numeric) The amount in BTC
      |      "label" : "label",              (string) A comment for the address/transaction, if any
      |      "vout" : n,                       (numeric) the vout value
      |    }
      |    ,...
      |  ],
      |  "hex" : "data"         (string) Raw data for transaction
      |}
      |
      |Examples:
      |> bitcoin-cli gettransaction "1075db55d416d3ca199f55b6084e2115b9345e16c5cf302fc80e9d5fbf5d48d"
      |> bitcoin-cli gettransaction "1075db55d416d3ca199f55b6084e2115b9345e16c5cf302fc80e9d5fbf5d48d" true
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "gettransaction", "params": ["1075db55d416d3ca199f55b6084e2115b9345e16c5cf302fc80e9d5fbf5d48d"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


