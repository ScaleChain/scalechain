package io.scalechain.wallet

import io.scalechain.blockchain.chain.ChainSampleData
import io.scalechain.blockchain.chain.ChainEventListener
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.transaction.TransactionTestDataTrait


open class WalletSampleData(override val db : KeyValueDatabase, private val wallet:Wallet) : ChainSampleData(db, wallet) {
  override fun onAddressGeneration(account: String, address: CoinAddress): Unit {
    wallet.importOutputOwnership(db, TestBlockchainView, account, address, false)
  }
}
