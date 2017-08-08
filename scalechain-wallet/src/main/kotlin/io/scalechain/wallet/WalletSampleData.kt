package io.scalechain.wallet

import io.scalechain.blockchain.chain.ChainSampleData
import io.scalechain.blockchain.chain.ChainEventListener
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAddress


open class WalletSampleData(db : KeyValueDatabase, private val wallet : Wallet) : ChainSampleData(db, wallet) {
  override fun onAddressGeneration(account: String, address: CoinAddress): Unit {
    // BUGBUG : Kotlin bug : wallet is null.
    // assert(wallet != null)
    // wallet.importOutputOwnership(db, TestBlockchainView, account, address, false)

    val w = chainEventListener as Wallet
    w.importOutputOwnership(db, TestBlockchainView, account, address, false)
  }
}
