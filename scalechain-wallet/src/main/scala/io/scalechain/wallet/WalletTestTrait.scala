package io.scalechain.wallet

import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.transaction.ChainTestTrait
import org.scalatest.Suite

/**
  * Created by kangmo on 7/9/16.
  */
trait WalletTestTrait extends BlockchainTestTrait with ChainTestTrait{
  this: Suite =>

  var wallet : Wallet = null

  override def beforeEach() {
    super.beforeEach()

    wallet = Wallet.create()
    chain.setEventListener(wallet)

    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)
  }

  override def afterEach() {

    super.afterEach()

    wallet  = null
  }

}
