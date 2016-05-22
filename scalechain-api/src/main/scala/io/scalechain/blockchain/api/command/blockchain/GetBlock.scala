package io.scalechain.blockchain.api.command.blockchain

import io.scalechain.blockchain.api.RpcSubSystem
import io.scalechain.blockchain.api.command.{BlockFormatter, RpcCommand}
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash._
import io.scalechain.blockchain.api.domain.{StringResult, RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.{BlockInfo, Block, HashFormat, Hash}
import io.scalechain.util.{HexUtil, ByteArray}
import HexUtil._
import ByteArray._
import Hash._
import spray.json.DefaultJsonProtocol._

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

case class GetBlockResult (
  // The hash of this block’s block header encoded as hex in RPC byte order. This is the same as the hash provided in parameter #1
  hash : Hash,                        // "000000000fe549a89848c76070d4132872cfb6efe5315d01d7ef77e4900f2d39"
  // The number of confirmations the transactions in this block have,
  // starting at 1 when this block is at the tip of the best block chain.
  // This score will be -1 if the the block is not part of the best block chain
//  confirmations : Long,               // 88029
  // The size of this block in serialized block format, counted in bytes
  size : Int,                         // 189
  // The height of this block on its block chain
  height : Long,                      // 227252
  // This block’s version number. See block version numbers
  version : Int,                      // 2
  // The merkle root for this block, encoded as hex in RPC byte order
  merkleroot : Hash,                  // "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a"
  // An array containing the TXIDs of all transactions in this block.
  // The transactions appear in the array in the same order they appear in the serialized block
  // tx item : The TXID of a transaction in this block, encoded as hex in RPC byte order
  tx : List[Hash],                    // [ "c738fb8e22750b6d3511ed0049a96558b0bc57046f3f77771ec825b22d6a6f4a" ]
  // The value of the time field in the block header, indicating approximately when the block was created
  time : Long,                        // 1398824312
  // The nonce which was successful at turning this particular block into one that could be added to the best block chain
  nonce : Long,                        // 1883462912
  // The value of the nBits field in the block header, indicating the target threshold this block’s header had to pass
//  bits : String,                      // "1d00ffff"
  // The estimated amount of work done to find this block relative to the estimated amount of work done to find block 0
//  difficulty : scala.math.BigDecimal, // 1.00000000
  // The estimated number of block header hashes miners had to check from the genesis block to this block, encoded as big-endian hex
//  chainwork : Hash,                   // "000000000000000000000000000000000000000000000000083ada4a4009841a"
  // The hash of the header of the previous block, encoded as hex in RPC byte order. Not returned for genesis block
  previousblockhash : Option[Hash]   // "00000000c7f4990e6ebf71ad7e21a47131dfeb22c759505b3998d7a814c011df"
  // The hash of the next block on the best block chain, if known, encoded as hex in RPC byte order
//  nextblockhash : Option[Hash]        // "00000000afe1928529ac766f1237657819a11cfcc8ca6d67f119e868ed5b6188"*/
) extends RpcResult

/** GetBlock: gets a block with a particular header hash
  * from the local block database either as a JSON object or as a serialized block.
  *
  * Parameter #1 : Header Hash (String, Required)
  *   The hash of the header of the block to get, encoded as hex in RPC byte order
  *
  * Parameter #2 : Format (Boolean, Optional)
  *   Set to false to get the block in serialized block format;
  *   set to true (the default) to get the decoded block as a JSON object
  *
  * Result: (String;hex) (if Format was false)
  *   The requested block as a serialized block, encoded as hex, or JSON null if an error occurred.
  *
  * Result: (Object;hex) (if Format was true or omitted)
  *   An object containing the requested block, or JSON null if an error occurred
  *
  *
  * https://bitcoin.org/en/developer-reference#getblock
  */
object GetBlock extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      // Convert request.params.paramValues, which List[JsValue] to SignRawTransactionParams instance.
      val headerHashString  : String  = request.params.get[String]("Header Hash", 0)
      val format            : Boolean = request.params.getOption[Boolean]("Format", 1).getOrElse(true)

      val headerHash = Hash( HexUtil.bytes(headerHashString) )

      val blockOption : Option[(BlockInfo, Block)] = RpcSubSystem.get.getBlock(headerHash)

      val resultOption = if (format) {
        blockOption.map{ case (blockInfo, block) => BlockFormatter.getBlockResult(blockInfo, block) }
      } else {
        blockOption.map{ case (blockInfo, block) =>
          StringResult( BlockFormatter.getSerializedBlock( block ) )
        }
      }

      Right(resultOption)
    }
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