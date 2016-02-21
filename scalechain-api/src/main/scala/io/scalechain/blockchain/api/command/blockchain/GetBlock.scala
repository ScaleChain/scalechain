package io.scalechain.blockchain.api.command.blockchain

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.{HexUtil, ByteArray}
import HexUtil._
import ByteArray._
import Hash._
/*
  CLI command :
    scalechain-cli -testnet getblock \
                000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39 \
                true

  CLI output :
    {
        "hash" : "000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39",
        "confirmations" : 88029,
        "size" : 189,
        "height" : 227252,
        "version" : 2,
        "merkleroot" : "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a",
        "tx" : [
            "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a"
        ],
        "time" : 1398824312,
        "nonce" : 1883462912,
        "bits" : "1d00ffff",
        "difficulty" : 1.00000000,
        "chainwork" : "000000000000000000000000000000000000000000000000083ada4a4009841a",
        "previousblockhash" : "00000000c7f4990e6ebf71ad7e21a47131dfeb22c759505b3998d7a814c011df",
        "nextblockhash" : "00000000afe1928529ac766f1237657819a11cfcc8ca6d67f119e868ed5b6188"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblock", "params": ["000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39", "true"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetBlockResult(
  hash : Hash,                        // "000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39"
  confirmations : Long,               // 88029
  size : Int,                         // 189
  height : Long,                      // 227252
  version : Int,                      // 2
  merkleroot : Hash,                  // "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a"
  tx : List[Hash],                    // [ "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a" ]
  time : Long,                        // 1398824312
  nonce : Int,                        // 1883462912
  bits : String,                      // "1d00ffff"
  difficulty : scala.math.BigDecimal, // 1.00000000
  chainwork : Hash,                   // "000000000000000000000000000000000000000000000000083ada4a4009841a"
  previousblockhash : Hash,           // "00000000c7f4990e6ebf71ad7e21a47131dfeb22c759505b3998d7a814c011df"
  nextblockhash : Hash                // "00000000afe1928529ac766f1237657819a11cfcc8ca6d67f119e868ed5b6188"*/
) extends RpcResult

/** GetBlock: gets a block with a particular header hash
  * from the local block database either as a JSON object or as a serialized block.
  *
  * https://bitcoin.org/en/developer-reference#getblock
  */
object GetBlock extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    Right(
      GetBlockResult(
        Hash("000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39"),
        88029,
        189,
        227252,
        2,
        Hash("c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a"),
        List(Hash("c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a")),
        1398824312,
        1883462912,
        "1d00ffff",
        1.00000000,
        Hash("000000000000000000000000000000000000000000000000083ada4a4009841a"),
        Hash("00000000c7f4990e6ebf71ad7e21a47131dfeb22c759505b3998d7a814c011df"),
        Hash("00000000afe1928529ac766f1237657819a11cfcc8ca6d67f119e868ed5b6188")
      )
    )
  }
  def help() : String =
    """getblock "hash" ( verbose )
      |
      |If verbose is false, returns a string that is serialized, hex-encoded data for block 'hash'.
      |If verbose is true, returns an Object with information about block <hash>.
      |
      |Arguments:
      |1. "hash"          (string, required) The block hash
      |2. verbose           (boolean, optional, default=true) true for a json object, false for the hex encoded data
      |
      |Result (for verbose = true):
      |{
      |  "hash" : "hash",     (string) the block hash (same as provided)
      |  "confirmations" : n,   (numeric) The number of confirmations, or -1 if the block is not on the main chain
      |  "size" : n,            (numeric) The block size
      |  "height" : n,          (numeric) The block height or index
      |  "version" : n,         (numeric) The block version
      |  "merkleroot" : "xxxx", (string) The merkle root
      |  "tx" : [               (array of string) The transaction ids
      |     "transactionid"     (string) The transaction id
      |     ,...
      |  ],
      |  "time" : ttt,          (numeric) The block time in seconds since epoch (Jan 1 1970 GMT)
      |  "mediantime" : ttt,    (numeric) The median block time in seconds since epoch (Jan 1 1970 GMT)
      |  "nonce" : n,           (numeric) The nonce
      |  "bits" : "1d00ffff", (string) The bits
      |  "difficulty" : x.xxx,  (numeric) The difficulty
      |  "chainwork" : "xxxx",  (string) Expected number of hashes required to produce the chain up to this block (in hex)
      |  "previousblockhash" : "hash",  (string) The hash of the previous block
      |  "nextblockhash" : "hash"       (string) The hash of the next block
      |}
      |
      |Result (for verbose=false):
      |"data"             (string) A string that is serialized, hex-encoded data for block 'hash'.
      |
      |Examples:
      |> bitcoin-cli getblock "00000000c937983704a73af28acdec37b049d214adbda81d7e2a3dd146f6ed09"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblock", "params": ["00000000c937983704a73af28acdec37b049d214adbda81d7e2a3dd146f6ed09"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}