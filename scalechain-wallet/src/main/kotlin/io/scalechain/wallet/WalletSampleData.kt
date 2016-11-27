package io.scalechain.wallet

import io.scalechain.blockchain.chain.ChainSampleData
import io.scalechain.blockchain.chain.ChainEventListener
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.transaction.TransactionTestDataTrait


class WalletSampleData(wallet:Wallet)(protected override val db : KeyValueDatabase) : ChainSampleData(Some(wallet))(db) {
  override fun onAddressGeneration(account: String, address: CoinAddress): Unit {
    wallet.importOutputOwnership(TestBlockchainView, account, address, false)(db)
  }
}
