package io.scalechain.blockchain.api.command.oap

import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.net.RpcSubSystem
import io.scalechain.blockchain.oap.command.AssetTransferTo
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.IOapConstants
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.transaction.PrivateKey
import io.scalechain.util.HexUtil
import io.scalechain.util.Either
import io.scalechain.util.Either.Right

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


/** TransferAsset: creates an transaction for tranfering an asset and send it to blockchain
  * The destination addresses must be an asset address.
  *
  * Parameter #1 : from_address (String, Required)
  * The bitcoin address from which the asset is transfered.
  *
  * Parameter #2 : tos (Array, Required)
  * Array of asset trafers.
  * Array Item : Asset Tranfser
  * Asset Address : An Asset Address to which asset is tranfered
  * Asset ID : Id of asset to be transfered
  * Asset Quantity : The quantity of Asset
  *
  * Paramter #3 : private_keys (string, optional) List of PrivateKeys in base58 check format.
  * To tranfer from watch only address, private key of the address should be provided by caller.
  *
  * Parameter #4 : change_address (String, Optional, defalut=issuer_address)
  * The bitcon address that receives coin and asset change.
  * If Change Address is not given From Address is used
  *
  * Parameter #5 : fees (Long, Optional)
  *
  * Result: (String)
  * The seirialied transaction in hex.
  *
  */
object TransferAsset  : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val fromAddress: String                     = request.params.get<String>("from_address", 0)
      // BUGBUG : OAP : Make sure this works
      val tos: List<AssetTransferTo>              = request.params.getList<AssetTransferTo>("tos", 1)
      val privateKeyStrings: List<String>?        = request.params.getListOption<String>("private_keys", 2)
      val changeAddress: String                   = request.params.getOption<String>("change_address", 3) ?: fromAddress
      val fees: Long                              = request.params.getOption<Long>("fees", 4) ?: IOapConstants.DEFAULT_FEES_IN_SATOSHI.toLong()

      val privateKeys: List<PrivateKey>? =
      if (privateKeyStrings != null) {
        if (privateKeyStrings.size == 0) {
          null
        }
        else {
          privateKeyStrings.map { keyString ->
            try {
              PrivateKey.from(keyString)
            } catch(e: GeneralException) {
              throw OapException(OapException.INVALID_PRIVATE_KEY, "Invalid private key");
            }
          }
        }
      } else {
        null
      }

      val hash: Hash = RpcSubSystem.get().transferAsset(
        fromAddress, tos, privateKeys, changeAddress, fees
      )
      Right(StringResult(
        HexUtil.hex(hash.value.array)
      ))
    }
    /*
          {

          case e : InvalidRpcParameter  => {
            Left(RpcError( RpcError.RPC_INVALID_PARAMS.code, RpcError.RPC_INVALID_PARAMS.messagePrefix, e.toString))
        }
    */
  }

  override fun help() : String =
    """transferasset "fromAddress" [{"to_address":"to_address","asset_id": "asset_id, "quantity":quantity },...] "changeAddress", fees  )
      |
      |Sign inputs for raw transaction (serialized, hex-encoded).
      |The second optional argument (may be null) is an array of previous transaction outputs that
      |this transaction depends on but may not yet be in the block chain.
      |The third optional argument (may be null) is an array of base58-encoded private
      |keys that, if given, will be the only keys used to sign the transaction.
      |
      |
      |Arguments:
      |1. "from_address"     (string, required) The bitcoin Address
      |2. "tos"             (string, required) An json array of Asset Transfer
      |     [               (json array of json objects)
      |       {
      |         "to_address":"id",    (String,  required) The Asset Adress
      |         "asset_id":n,         (numeric, required) The Asset Id
      |         "quntity": quntity,   (int,     required) The Asset Quantity
      |       }
      |       ,...
      |    ]
      |3. "private_keys"
      |3. "change_address"    (string, optional, default=fromAdress) A bitcoin address to which the coin and asset changes go.
      |4. "fees"              (numeric, default=ALL) The fees to pay.
      |
      |Result:
      |{
      |  "result" : "value",           (string) The hex-encoded raw transaction.
      |}
      |
      |Examples:
      |> bitcoin-cli transferasset "fromAddress" "[ { "asset_address" : "asset_address", "asset_id":"asset_id", "quantity" : quntity} ]" "changeAddress" fees
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "transferasset", "params": ["fromAddress" "[ { "asset_address" : "asset_address", "asset_id":"asset_id", "quantity" : quntity} ]" "changeAddress" fees] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}

