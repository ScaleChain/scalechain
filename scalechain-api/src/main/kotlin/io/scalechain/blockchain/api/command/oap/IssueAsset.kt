package io.scalechain.blockchain.api.command.oap

import java.util

import com.google.gson.JsonElement
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.api.command.{RpcCommand, TransactionFormatter}
import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.net.{IssueAssetResult, RpcSubSystem}
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.{IOapConstants, OpenAssetsProtocol}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.{PrivateKey, SigHash}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsValue}
/*
  CLI command :
    bitcoin-cli -testnet signrawtransaction 01000000011da9283b4ddf8d\
      89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e0000000000ffff\
      ffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a51\
      59f58488ac00000000



  CLI output :
    {
        "hex" : "01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a47304402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000",
        "complete" : true
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "signrawtransaction", "params": ["01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e0000000000ffffffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/



/** IssueAsset : create an unsigned transaction for issuing Asset from a bitcoin Address.
  * Asset ID is calculated from issuer_address.
  * The destination address("to_address") must be an Asset Address.
  *
  * Arguments:
  * Parameter #1 : issuer_address(string, required) A bitcoin address from which Assets issued.
  *   Issued Asset Id is caculated from isser_address.
  *
  * Parameter #2. "to_address"         (string, required) An Asset address that receives issued assets.
  *   The to_address must be an Asset Address.
  *
  * Parameter #3. "amount"             (int,    required) The quantity of Assets to issue.
  *
  * Parameter #4. "metadata     "      (string, required) The medata of issuing Asset in HEX string.
  *
  * Parameter #5. "change_address"     (string, optional) A Coin Address that receives coin change.
  *   The bitcon address that receives coin and asset change.
  *   If chang_address is not givend, to_address is used.
  * Parameter #6  "fees"               (long, optional)
  *
  *
  * Result:
  * {
  *  *  "result" : "value",           (string) The hex-encoded raw transaction.
  *  }
  *
  *  Examples:
  *  > bitcoin-cli issueasset "issuer_address" "to_address" "quantity" "metadata" "change_address" "fees"
  *  > curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "issueasset", "params": ["issuer_address" "to_address" "quantity" "metadata" "change_address" "fees"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
  */
object IssueAsset extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val issuerAddress:     String               = request.params.get[String]("issuer_address", 0)
      val toAddress:         String               = request.params.get[String]("to_address", 1)
      val quantity:          Int                  = request.params.get[Int]("quantity", 2)
      val hashOption:        Option[String]       = request.params.getOption[String]("hash", 3)
      val privateKeyStrings: Option[List[String]] = request.params.getListOption[String]("private_keys", 4)
      val changeAddress:     String               = request.params.getOption[String]("change_address", 5).getOrElse(issuerAddress)
      val fees: Long                              = request.params.getOption[Long]("fees", 6).getOrElse(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
      // Moved metadata to the last of Argument.
      val metadataOption:  Option[JsObject]       = request.params.getOption[JsObject]("metadata", 7)

      val privateKeys =
        if (privateKeyStrings.isDefined) {
          if (privateKeyStrings.get.size == 0)
            None
          else
            privateKeyStrings.map { keyStrings =>
              try {
                keyStrings.map(PrivateKey.from(_))
              } catch {
                case e: GeneralException => throw new OapException(OapException.INVALID_PRIVATE_KEY, "Invalid private key");
              }
            }
        } else {
          None
        }

      val result = RpcSubSystem.get.issueAsset(
                issuerAddress,
                toAddress,
                quantity,
                hashOption,
                privateKeys,
                changeAddress,
                fees,
                metadataOption
      )
      Right(
        Some(
          IssueAssetResultResult(result)
        )
      )
    }
  }
  def help() : String =
    """issueasset "issuer_address" "to_address" "quantity" "metadata" "change_address" "fees"
      |
      |create an unsigned transaction for issuing Asset from a bitcoin Address.
      |
      |Asset ID is calculated from issuer_address.
      |The destination address("to_address") must be an Asset Address.
      |
      |
      |Arguments:
      |1. "issuer_address"     (string, required) A bitcoin address from which Assets issued.
      |2. "to_address"         (string, required) An Asset address that receives issued assets.
      |3. "quantity"           (numeric,required) The quantity of Assets to issue.
      |4. "hash_option"        (string, optional) The Asset Definition Pointer in Hex format
      |5. "private_keys"       (string, optional) The array of Private Keys in base58 check format.
      |6. "change_address"     (string, optional) A Coin Address that receives coin change.
      |7  "fees"               (long,   optional, default=20000) The fees to pay
      |8. "metadata     "      (Obejct, optional) The medata of issuing Asset in .
      |
      |Result:
      |{
      |  "result" : "value",           (string) The hex-encoded raw transaction.
      |}
      |
      |Examples:
      |> bitcoin-cli issueasset "issuer_address" "to_address" "quantity" "metadata" "change_address" "fees"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "issueasset", "params": ["n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG", "n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG", 10000, "f1b575b277228d97583e355951022a8ac14a12e2" "n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG" 20000, {}] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}

case class IssueAssetResultResult(item : IssueAssetResult) extends RpcResult;