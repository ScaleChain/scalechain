package io.scalechain.wallet

import io.scalechain.blockchain.chain.{ChainSampleData, ChainEventListener}
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.{CoinAddress, TransactionTestDataTrait}


class WalletSampleData(wallet:Wallet)(protected override val db : KeyValueDatabase) extends ChainSampleData(Some(wallet))(db) {
  override def onAddressGeneration(account: String, address: CoinAddress): Unit = {
    wallet.importOutputOwnership(TestBlockchainView, account, address, false)(db)
  }
}
