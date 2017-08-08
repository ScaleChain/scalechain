package io.scalechain.wallet

/**
 * Created by kangmo on 16/12/2016.
 */
interface WalletTests : WalletStoreAccountTest, WalletStoreOutPointTest, WalletStoreTransactionHashTest, WalletStoreWalletOutputTest, WalletStoreWalletTransactionTest {
  override fun addTests() {
    super<WalletStoreAccountTest>.addTests()
    super<WalletStoreOutPointTest>.addTests()
    super<WalletStoreTransactionHashTest>.addTests()
    super<WalletStoreWalletOutputTest>.addTests()
    super<WalletStoreWalletTransactionTest>.addTests()
  }
}