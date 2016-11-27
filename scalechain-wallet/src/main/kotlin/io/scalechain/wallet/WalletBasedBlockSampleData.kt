package io.scalechain.wallet

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.transaction.ParsedPubKeyScript
import io.scalechain.blockchain.transaction.PublicKey
import io.scalechain.blockchain.transaction.PrivateKey

class WalletBasedBlockSampleData(wallet : Wallet)(implicit override val db : KeyValueDatabase) : BlockSampleData()(db) {

  /** Generate the address from the wallet so that the wallet can sign transactions
    *
    * @param account The name of the account to generate a address.
    */
  override fun generateAccountAddress(account:String) : AddressData {
    val address = wallet.newAddress(account)
    val privateKeys : List<PrivateKey> = wallet.getPrivateKeys(Some(address))
    assert( privateKeys.length == 1 )
    val privateKey = privateKeys.head

    val publicKey = PublicKey.from(privateKey)
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    assert( address == CoinAddress.from(privateKey) )

    AddressData(
      privateKey,
      publicKey,
      publicKeyScript,
      address
    )
  }
}
