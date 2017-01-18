package io.scalechain.blockchain.oap.command

/**
  * Created by shannon on 16. 12. 16.
  */

/** An asset tranfer input class, which is used as an input parameter of transferasset RPC.
  *
  * @param to_address The asset address which will spend this asset
  * @param asset_id The id of asset to transer
  * @param quantity The amount of an asset to transfer
  */
case class AssetTransferTo(to_address   : String, asset_id     : String, quantity    : Int)
