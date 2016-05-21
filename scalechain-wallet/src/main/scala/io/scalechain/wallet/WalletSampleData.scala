package io.scalechain.wallet

import io.scalechain.blockchain.chain.{ChainSampleData, ChainEventListener}
import io.scalechain.blockchain.transaction.{CoinAddress, TransactionTestDataTrait}


class WalletSampleData(wallet:Wallet) extends ChainSampleData(Some(wallet)) {
  override def onAddressGeneration(account: String, address: CoinAddress): Unit = {
    wallet.importOutputOwnership(TestBlockchainView, account, address, false)
  }
}
