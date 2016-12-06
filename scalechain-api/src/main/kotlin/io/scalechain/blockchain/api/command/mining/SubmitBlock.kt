package io.scalechain.blockchain.api.command.mining

import com.google.gson.JsonObject
import io.scalechain.blockchain.UnsupportedFeature
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    # Submit the following block with the workid, “test”.

    bitcoin-cli -testnet submitblock 02000000df11c014a8d798395b5059c\
      722ebdf3171a4217ead71bf6e0e99f4c7000000004a6f6a2db225c81e77773f6\
      f0457bcb05865a94900ed11356d0b75228efb38c7785d6053ffff001d005d437\
      0010100000001000000000000000000000000000000000000000000000000000\
      0000000000000ffffffff0d03b477030164062f503253482fffffffff0100f90\
      29500000000232103adb7d8ef6b63de74313e0cd4e07670d09a169b13e4eda2d\
      650f529332c47646dac00000000 \
      '{ "workid": "test" }'



  CLI output :
    duplicate

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "submitblock", "params": ["02000000df11c014a8d798395b5059c722ebdf3171a4217ead71bf6e0e99f4c7000000004a6f6a2db225c81e77773f6f0457bcb05865a94900ed11356d0b75228efb38c7785d6053ffff001d005d43700101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0d03b477030164062f503253482fffffffff0100f9029500000000232103adb7d8ef6b63de74313e0cd4e07670d09a169b13e4eda2d650f529332c47646dac00000000", '{ "workid": "test" }'] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SubmitBlock: accepts a block, verifies it is a valid addition to the block chain, and
  * broadcasts it to the network.
  *
  * Extra parameters are ignored by Bitcoin Core but may be used by mining pools or other programs.
  *
  * Parameter #1 : Block (String;hex, Required)
  *   The full block to submit in serialized block format as hex.
  *
  * Parameter #2 : Parameters (Object, Optional)
  *   A JSON object containing extra parameters. Not used directly by Bitcoin Core and also not broadcast to the network.
  *   This is available for use by mining pools and other software. A common parameter is a workid string.
  *
  * Result :
  *   If the block submission succeeded, set to JSON null.
  *   If submission failed, set to one of the following strings: duplicate, duplicate-invalid, inconclusive, or rejected.
  *   The JSON-RPC error field will still be set to null if submission failed for one of these reasons
  *
  * Result: (Type)
  *   Description
  *
  * https://bitcoin.org/en/developer-reference#submitblock
  */
object SubmitBlock : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val serializedBlock : String  = request.params.get<String>("Block", 0)

      val parameters      : JsonObject = when(request.params.paramValues[1]) {
        is JsonObject -> request.params.paramValues[1].asJsonObject
        else -> throw RpcException(ErrorCode.RpcParameterTypeConversionFailure, "The Parameters should be JsObject, but it is not" )
      }
/*
      // Step 1 : decode the block
      val block = BlockDecoder.decodeBlock(serializedBlock)

      // Step 2 : verify the block, including all transactions in it.
      BlockVerifier(block).verify()

      // Step 3 : try to save block
      val isNewBlock = SubSystem.blockDatabaseService.putBlock(block)

      val response =
        if (isNewBlock) {
          SubSystem.peerService.submitBlock(block, parameters)
          NullResult..
        } else {
          None
        }
*/
/*
      // TODO : Implement
      val errorMessageOption = Some("duplicate")
      val resultOption = errorMessageOption.map(StringResult(_))
      Right(resultOption)
*/
      throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
    }
  }
  override fun help() : String =
    """submitblock "hexdata" ( "jsonparametersobject" )
      |
      |Attempts to submit block to network.
      |The 'jsonparametersobject' parameter is currently ignored.
      |See https://en.bitcoin.it/wiki/BIP_0022 for full specification.
      |
      |Arguments
      |1. "hexdata"    (string, required) the hex-encoded block data to submit
      |2. "jsonparametersobject"     (string, optional) object of optional parameters
      |    {
      |      "workid" : "id"    (string, optional) if the server provided a workid, it MUST be included with submissions
      |    }
      |
      |Result:
      |
      |Examples:
      |> bitcoin-cli submitblock "mydata"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "submitblock", "params": ["mydata"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


