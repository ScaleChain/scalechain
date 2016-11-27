package io.scalechain.wallet

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*

class WalletBasedBlockSampleData(override val db : KeyValueDatabase, private val wallet : Wallet) : BlockSampleData(db) {

  /** Generate the address from the wallet so that the wallet can sign transactions
    *
    * @param account The name of the account to generate a address.
    */
  override fun generateAccountAddress(account:String) : TransactionTestDataTrait.AddressData {
    val address = wallet.newAddress(db, account)
    val privateKeys : List<PrivateKey> = wallet.getPrivateKeys(db, address)
    assert( privateKeys.size == 1 )
    val privateKey = privateKeys.first()

    val publicKey = PublicKey.from(privateKey)
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    assert( address == CoinAddress.from(privateKey) )

    return TransactionTestDataTrait.AddressData(
      privateKey,
      publicKey,
      publicKeyScript,
      address
    )
  }
}
