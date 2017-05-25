package io.scalechain.blockchain.oap.wallet

import io.scalechain.blockchain.oap.transaction.OapTransactionOutput
import io.scalechain.blockchain.proto.Hash
import io.scalechain.wallet.WalletTransactionDescriptor

/**
  * Created by shannon on 16. 12. 26.
  */

data class OapTransactionDescriptor(
  val involvesWatchonly: Boolean,
  val account: String,
  val address: String?,
  val category: String,
  val amount: java.math.BigDecimal,
  val vout: Int?,
  val fee: java.math.BigDecimal?,
  val confirmations: Long?,
  val generated: Boolean?,
  val blockhash: Hash?,
  val blockindex: Long?,
  val blocktime: Long?,
  val txid: Hash?,
  val time: Long,
  val asset_id: String?,
  val quantity: Int?
) {

  companion object {

    @JvmStatic
    fun from(desc: WalletTransactionDescriptor) : OapTransactionDescriptor{
      return OapTransactionDescriptor(
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
        null,
        null
      )
    }

    @JvmStatic
    fun from(desc: WalletTransactionDescriptor, output : OapTransactionOutput) : OapTransactionDescriptor {
      return OapTransactionDescriptor(
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
        if (output.assetId == null) null else output.assetId.base58(),
        if (output.assetId == null) null else output.quantity
      )
    }
  }
}

