package io.scalechain.wallet

import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.transaction.ChainTestTrait

/**
  * Created by kangmo on 7/9/16.
  */
abstract class WalletTestTrait : BlockchainTestTrait(), ChainTestTrait{

  lateinit var wallet : Wallet

  override fun beforeEach() {
    super.beforeEach()

    wallet = Wallet.create()
    chain.setEventListener(wallet)

    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)
  }

  override fun afterEach() {

    super.afterEach()
  }

}
