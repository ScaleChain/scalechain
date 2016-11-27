package io.scalechain.wallet

import io.scalechain.blockchain.transaction.ChainTestTrait
/*
/**
  * Created by kangmo on 7/9/16.
  */
trait WalletTestTrait : BlockchainTestTrait with ChainTestTrait{
  this: Suite =>

  var wallet : Wallet = null

  override fun beforeEach() {
    super.beforeEach()

    wallet = Wallet.create()
    chain.setEventListener(wallet)

    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)
  }

  override fun afterEach() {

    super.afterEach()

    wallet  = null
  }

}
*/