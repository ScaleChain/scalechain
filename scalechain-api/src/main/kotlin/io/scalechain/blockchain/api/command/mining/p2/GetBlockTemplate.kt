package io.scalechain.blockchain.api.command.mining.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # CLI command parameters are from https://en.bitcoin.it/wiki/Getblocktemplate
    bitcoin-cli -testnet getblocktemplate '{"capabilities": ["coinbasetxn", "workid", "coinbase/append"]}'

  CLI output :
    {
      "coinbasetxn": {
       "data": "0100000001000000000000000000000000000000000000000000000000000000
      0000000000ffffffff1302955d0f00456c6967697573005047dc66085fffffffff02fff1052a01
      0000001976a9144ebeb1cd26d6227635828d60d3e0ed7d0da248fb88ac01000000000000001976
      a9147c866aee1fa2f3b3d5effad576df3dbf1f07475588ac00000000"
      },
      "previousblockhash": "000000004d424dec1c660a68456b8271d09628a80cc62583e5904f5894a2483c",
      "transactions": [],
      "expires": 120,
      "target": "00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "longpollid": "some gibberish",
      "height": 23957,
      "version": 2,
      "curtime": 1346886758,
      "mutable": ["coinbase/append"],
      "bits": "ffff001d"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblocktemplate", "params": [{"capabilities": ["coinbasetxn", "workid", "coinbase/append"]}] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBlockTemplate: gets a block template or proposal for use with mining software.
  *
  * https://bitcoin.org/en/developer-reference#getblocktemplate
  */
object GetBlockTemplate extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """getblocktemplate ( "jsonrequestobject" )
      |
      |If the request parameters include a 'mode' key, that is used to explicitly select between the default 'template' request or a 'proposal'.
      |It returns data needed to construct a block to work on.
      |See https://en.bitcoin.it/wiki/BIP_0022 for full specification.
      |
      |Arguments:
      |1. "jsonrequestobject"     (string, optional) A json object in the following spec
      |     {
      |       "mode":"template"      (string, optional) This must be set to "template" or omitted
      |       "capabilities":[       (array, optional) A list of strings
      |           "support"          (string) client side supported feature, 'longpoll', 'coinbasetxn', 'coinbasevalue', 'proposal', 'serverlist', 'workid'
      |           ,...
      |       ]
      |     }
      |
      |
      |Result:
      |{
      |  "version" : n,                      (numeric) The block version
      |  "previousblockhash" : "xxxx",       (string) The hash of current highest block
      |  "transactions" : [                  (array) contents of non-coinbase transactions that should be included in the next block
      |      {
      |         "data" : "xxxx",             (string) transaction data encoded in hexadecimal (byte-for-byte)
      |         "hash" : "xxxx",             (string) hash/id encoded in little-endian hexadecimal
      |         "depends" : [                (array) array of numbers
      |             n                        (numeric) transactions before this one (by 1-based index in 'transactions' list) that must be present in the final block if this one is
      |             ,...
      |         ],
      |         "fee": n,                   (numeric) difference in value between transaction inputs and outputs (in Satoshis); for coinbase transactions, this is a negative Number of the total collected block fees (ie, not including the block subsidy); if key is not present, fee is unknown and clients MUST NOT assume there isn't one
      |         "sigops" : n,               (numeric) total number of SigOps, as counted for purposes of block limits; if key is not present, sigop count is unknown and clients MUST NOT assume there aren't any
      |         "required" : true|false     (boolean) if provided and true, this transaction must be in the final block
      |      }
      |      ,...
      |  ],
      |  "coinbaseaux" : {                  (json object) data that should be included in the coinbase's scriptSig content
      |      "flags" : "flags"              (string)
      |  },
      |  "coinbasevalue" : n,               (numeric) maximum allowable input to coinbase transaction, including the generation award and transaction fees (in Satoshis)
      |  "coinbasetxn" : { ... },           (json object) information for coinbase transaction
      |  "target" : "xxxx",                 (string) The hash target
      |  "mintime" : xxx,                   (numeric) The minimum timestamp appropriate for next block time in seconds since epoch (Jan 1 1970 GMT)
      |  "mutable" : [                      (array of string) list of ways the block template may be changed
      |     "value"                         (string) A way the block template may be changed, e.g. 'time', 'transactions', 'prevblock'
      |     ,...
      |  ],
      |  "noncerange" : "00000000ffffffff",   (string) A range of valid nonces
      |  "sigoplimit" : n,                 (numeric) limit of sigops in blocks
      |  "sizelimit" : n,                  (numeric) limit of block size
      |  "curtime" : ttt,                  (numeric) current timestamp in seconds since epoch (Jan 1 1970 GMT)
      |  "bits" : "xxx",                   (string) compressed target of next block
      |  "height" : n                      (numeric) The height of the next block
      |}
      |
      |Examples:
      |> bitcoin-cli getblocktemplate
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblocktemplate", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


