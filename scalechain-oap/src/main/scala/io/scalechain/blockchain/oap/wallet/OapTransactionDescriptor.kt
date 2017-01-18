package io.scalechain.blockchain.oap.wallet

import io.scalechain.blockchain.oap.transaction.OapTransactionOutput
import io.scalechain.blockchain.proto.Hash
import io.scalechain.wallet.WalletTransactionDescriptor

import scala.reflect.internal.util.Statistics.Quantity

/**
  * Created by shannon on 16. 12. 26.
  */

object OapTransactionDescriptor {
//  def from(desc: WalletTransactionDescriptor, asset_id: Option[String], quantity: Option[Int]) = {
//    OapTransactionDescriptor.apply(
//      desc.involvesWatchonly,
//      desc.account,
//      desc.address,
//      desc.category,
//      desc.amount,
//      desc.vout,
//      desc.fee,
//      desc.confirmations,
//      desc.generated,
//      desc.blockhash,
//      desc.blockindex,
//      desc.blocktime,
//      desc.txid,
//      desc.time,
//      asset_id,
//      quantity
//    )
//  }

  def from(desc: WalletTransactionDescriptor) = {
    OapTransactionDescriptor.apply(
      desc.involvesWatchonly,
      desc.account,
      desc.address,
      desc.category,
      desc.amount,
      desc.vout,
      desc.fee,
      desc.confirmations,
      desc.generated,
      desc.blockhash,
      desc.blockindex,
      desc.blocktime,
      desc.txid,
      desc.time,
      None,
      None
    )
  }

  def from(desc: WalletTransactionDescriptor, output : OapTransactionOutput) = {
    OapTransactionDescriptor.apply(
      desc.involvesWatchonly,
      desc.account,
      desc.address,
      desc.category,
      desc.amount,
      desc.vout,
      desc.fee,
      desc.confirmations,
      desc.generated,
      desc.blockhash,
      desc.blockindex,
      desc.blocktime,
      desc.txid,
      desc.time,
      if (output.getAssetId == null) None else Option(output.getAssetId.base58()),
      if (output.getAssetId == null) None else Option(output.getQuantity)
    )
  }


}

case class OapTransactionDescriptor(
  involvesWatchonly: Boolean,
  account: String,
  address: Option[String],
  category: String,
  amount: scala.math.BigDecimal,
  vout: Option[Int],
  fee: Option[scala.math.BigDecimal],
  confirmations: Option[Long],
  generated: Option[Boolean],
  blockhash: Option[Hash],
  blockindex: Option[Long],
  blocktime: Option[Long],
  txid: Option[Hash],
  time: Long,
  asset_id: Option[String],
  quantity: Option[Int]
);

