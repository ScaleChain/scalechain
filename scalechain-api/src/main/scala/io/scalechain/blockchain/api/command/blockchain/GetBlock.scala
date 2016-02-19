package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.Hash

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
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblock", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetBlockResult(
  hash : Hash,
  confirmations : Long,
  size : Int,
  height : Long,
  version : Int,
  merkleroot : Hash,
  tx : List[String],
  time : Long,
  nonce : Int,
  bits : String,
  difficulty : scala.math.BigDecimal,
  chainwork : Hash,
  previousblockhash : Hash,
  nextblockhash : Hash
)

/** GetBlock: gets a block with a particular header hash
  * from the local block database either as a JSON object or as a serialized block.
  *
  * https://bitcoin.org/en/developer-reference#getblock
  */
object GetBlock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


