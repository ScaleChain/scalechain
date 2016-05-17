package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.chain.{ChainBlock, BlockchainView, ChainEventListener}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.transaction.SigHash.SigHash
import io.scalechain.blockchain.transaction.TransactionSigner.SignedTransaction
import io.scalechain.blockchain.transaction._
import io.scalechain.crypto.{Hash160, HashFunctions, ECKey}

// [Wallet layer] A wallet keeps a list of private keys, and signs transactions using a private key, etc.
object Wallet extends ChainEventListener {
  // BUGBUG : Change the folder name without the "target" path.
  val walletFolder = new File("./target/wallet")

  val store = new WalletStore(walletFolder)

  /** signs a transaction.
    *
    * Used by : signrawtransaction RPC.
    *
    * TODO : Test Automation
    *
    * @param transaction The transaction to sign.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return
    */
  def signTransaction(transaction   : Transaction,
                      dependencies  : List[UnspentTransactionOutput],
                      privateKeys   : Option[List[PrivateKey]],
                      sigHash       : SigHash
                     ) : SignedTransaction = {
    if (privateKeys.isEmpty) {
      // Wallet Store : Iterate private keys for all accounts
      val privateKeysFromWallet = store.getPrivateKeys.toList

      TransactionSigner.sign(transaction, dependencies, privateKeysFromWallet, sigHash )
    } else {
      TransactionSigner.sign(transaction, dependencies, privateKeys.get, sigHash )
    }
  }

  case class CoinAmount(value : scala.math.BigDecimal)

  /** Returns the total amount received by the specified address
    *
    * Used by : getreceivedbyaddress RPC.
    *
    * TODO : Test Automation
    *
    * @param address The coin address to calculate the amount of received coins.
    * @param minConfirmations The number of confirmations to filter the UTXO.
    */
  def getReceivedByAddress(blockchainView : BlockchainView, address : CoinAddress, minConfirmations : Long) : CoinAmount = {
    // TODO : BUGBUG : We are counting outputs that an address is just one of multiple addresses in multisig outputs.

    // Step 1 : Wallet Store : Iterate UTXOs for an output ownership.
    val total = getTransactionOutputs(Some(address)).foldLeft(scala.math.BigDecimal(0L)) {
            // TODO : c
      // Step 2 : Sum up the amount of UTXOs.
      (sum : scala.math.BigDecimal, walletOutput : WalletOutputWithInfo) => {
        val confirmations = walletOutput.walletOutput.blockindex.map( getConfirmations(blockchainView, _) ).getOrElse(0L)
        if (confirmations >= minConfirmations) {
          sum + walletOutput.walletOutput.transactionOutput.value
        } else {
          sum
        }
      }
    }
    CoinAmount(total)
  }



  /** Returns the most recent transactions that affect the wallet.
    *
    * Used by : listtransactions RPC.
    *
    * TODO : Test Automation
    *
    * @param blockchainView  The view of the best blockchain.
    * @param account The name of an account to get transactions from
    * @param count The number of the most recent transactions to list
    * @param skip The number of the most recent transactions which should not be returned.
    * @param includeWatchOnly If set to true, include watch-only addresses in details and calculations as if they were regular addresses belonging to the wallet.
    * @return List of transactions affecting the wallet.
    */
  def listTransactions(
                        blockchainView : BlockchainView,
                        account         : String,
                        count           : Int,
                        skip            : Long,
                        includeWatchOnly: Boolean
                      ) : List[TransactionDescriptor] = {
    // TODO : Implement
    assert(false)
    null
    // Wallet Store : Iterate transactions by providing an account and the skip count.
    /*
    store.getTransactions(account, skip, includeWatchOnly).map { transaction : Transaction =>

    }*/
    //   Stop the iteration after getting 'count' transactions.
  }

  /** The WalletOutput with additional information such as OutPoint.
    *
    * @param outPoint The OutPoint which has the transaction hash and output index of this output.
    * @param walletOutput The wallet output stored on the wallet database.
    */
  case class WalletOutputWithInfo(
    outPoint : OutPoint,
    walletOutput : WalletOutput
  )

  /** Get an iterator for UTXOs.
    *
    * Category : [Output Ownership -> UTXOs] - Search
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    * @return The iterator for UTXOs.
    */
  def getTransactionOutputs(outputOwnershipOption : Option[OutputOwnership]) : Iterator[WalletOutputWithInfo] = {
    store.getTransactionOutPoints(outputOwnershipOption).map { outPoint =>
      store.getWalletOutput(outPoint).map{ walletOutput => // convert WalletOutput to WalletOutputWithInfo
        WalletOutputWithInfo(
          outPoint,
          walletOutput
        )
      }
    }.filter(_.isEmpty) // getWalletOutput returns Option[OutPoint]. Get rid of None values.
      .map(_.get) // Now get rid of the Option wrapper.
  }

  /** Convert a WalletOutputWithInfo to a UnspentCoinDescriptor.
    * We got WalletOutputWithInfo from the wallet database.
    * We need UnspentCoinDescriptor to convert it to json to return as a RPC response.
    *
    *
    * @param blockchainView Provides a read-only view of the blockchain.
    * @param walletOutput Some(output) if it is not spent yet. None if it is already spent.
    * @return The converted UnspentCoinDescriptor.
    */
  protected [wallet] def getUnspentCoinDescription( blockchainView : BlockchainView,
                                                    addressOption  : Option[CoinAddress],
                                                    walletOutput   : WalletOutputWithInfo
                                                  ) : Option[UnspentCoinDescriptor] = {
    if ( walletOutput.walletOutput.spent ) {
      None
    } else {
      // TODO : BUGBUG : Extract redeem script.
      val redeemScriptOption   : Option[String] = None

      val confirmations = walletOutput.walletOutput.blockindex.map( getConfirmations(blockchainView, _) ).getOrElse(0L)
      // TODO : Move to a configuration object.
      val COINBASE_MATURITY_CONFIRMATIONS = 100
      Some(
        UnspentCoinDescriptor(
          txid          = walletOutput.outPoint.transactionHash,
          vout          = walletOutput.outPoint.outputIndex,
          address       = addressOption.map( _.base58 ),
          account       = addressOption.map( store.getAccount(_ ) ),
          scriptPubKey  = walletOutput.walletOutput.transactionOutput.lockingScript.data,
          redeemScript  = redeemScriptOption,
          amount        = scala.math.BigDecimal( walletOutput.walletOutput.transactionOutput.value ),
          confirmations = confirmations,
          spendable     = !walletOutput.walletOutput.coinbase || (confirmations > COINBASE_MATURITY_CONFIRMATIONS)
        )
      )
    }
  }

  /** Get the number of confirmations for a transaction in a block.
    *
    * @param blockchainView The read-only view of the blockchain.
    * @param blockHeight The height of a block, where the transaction exists.
    * @return
    */
  protected [wallet] def getConfirmations( blockchainView : BlockchainView, blockHeight : Long) = {
    blockchainView.getBestBlockHeight() - blockHeight + 1
  }

  /** Returns an array of unspent transaction outputs belonging to this wallet.
    *
    * Used by : listunspent RPC.
    *
    * TODO : Test Automation
    *
    * @param blockchainView  The view of the best blockchain.
    * @param minimumConfirmations The minimum number of confirmations the transaction containing an output must have in order to be returned.
    * @param maximumConfirmations The maximum number of confirmations the transaction containing an output may have in order to be returned.
    * @param addressesOption If present, only outputs which pay an address in this array will be returned.
    * @return a list of objects each describing an unspent output
    */
  def listUnspent(
                   blockchainView : BlockchainView,
                   minimumConfirmations: Long,
                   maximumConfirmations: Long,
                   addressesOption     : Option[List[CoinAddress]]
                 ) : List[UnspentCoinDescriptor] = {

    // TODO : BUGBUG : Need to consider ParsedPubKeyScript
    val allAddresses = store.getOutputOwnerships().map{case address:CoinAddress => address}.toList

    val addressesFilter =
      if (addressesOption.isDefined) {
        addressesOption.get
      } else {
        allAddresses
      }

    addressesFilter.flatMap { coinAddress =>
      // Wallet Store : Iterate UTXOs for a specific coin address
      getTransactionOutputs(Some(coinAddress)).map { walletOutput =>
        getUnspentCoinDescription(
          blockchainView, Some(coinAddress), walletOutput
        )
      }
    }

      .filter( _.isEmpty ) // getUnspentCoinDescription returns Option[UnspentCoinDescriptor]. Filter out None values.
     .map( _.get )     // Get rid of the Option wrapper to convert Option[UnspentCoinDescriptor] to UnspentCoinDescriptor
     .filter( _.confirmations >= minimumConfirmations )
     .filter( _.confirmations <= maximumConfirmations )
     .toList
  }

  /** Import a watch-only address into the wallet.
    *
    * TODO : Test Automation
    *
    * @param blockchainView Provides a chain iterator, which iterates blocks in the best blockchain.
    * @param outputOwnership The ownership object that describes an ownership of a coin.
    * @param account The account name
    * @param rescanBlockchain Whether to rescan whole blockchain to re-index unspent transaction outputs(coins)
    */
  def importOutputOwnership(
     blockchainView : BlockchainView,
     account : String,
     outputOwnership : OutputOwnership,
     rescanBlockchain : Boolean
  ): Unit = {

    // Step 1 : Wallet Store : Add an output ownership to an account. Create an account if it does not exist.
    store.putOutputOwnership(account, outputOwnership)

    // Step 2 : Rescan blockchain
    if (rescanBlockchain) {
      blockchainView.getIterator(height = 0L) foreach { chainBlock : ChainBlock =>
        chainBlock.block.transactions foreach { transaction =>
          registerTransaction(List(outputOwnership), transaction, Some(chainBlock))
        }
      }
    }
  }

  /** Find an account by coin address.
    *
    * TODO : Test Automation
    *
    * Used by : getaccount RPC.
    *
    * @param address The coin address, which is attached to the account.
    * @return The found account.
    */
  def getAccount(address : CoinAddress) : String = {

    store.getAccount(address)
  }

  /** Returns a new coin address for receiving payments.
    *
    * TODO : Test Automation
    * Used by : newaddress RPC.
    *
    * @return the new address for receiving payments.
    */
  def newAddress(account: String) : CoinAddress = {
    // Step 1 : Generate a random number and a private key.
    val privateKey : PrivateKey = PrivateKey.generate

    // Step 2 : Create a public key.
    val publicKey : Array[Byte] = ECKey.publicKeyFromPrivate(privateKey.value, false /* uncompressed */)

    // Step 3 : Hash the public key.
    val publicKeyHash : Hash160 = HashFunctions.hash160(publicKey)

    // Step 4 : Create an address.
    val address = CoinAddress.from(publicKeyHash.value)

    // Step 5 : Wallet Store : Put an address into an account.
    store.putOutputOwnership(account, address)

    // Step 6 : Wallet Store : Put the private key into an the address.
    store.putPrivateKey(address, privateKey)

    // Step 7 : Put the address as the receiving address of the account.
    store.putReceivingAddress(account, address)

    address
  }


  /** Returns the current address for receiving payments to this account.
    *
    * Used by : getaccountaddress RPC.
    *
    * TODO : Test Automation
    *
    * @return The coin address for receiving payments.
    */
  def getReceivingAddress(account:String) : CoinAddress = {
    store.getReceivingAddress()
  }


  /** Invoked whenever a new transaction comes into a mempool.
    * This method is not invoked for transactions that are included in a block.
    *
    * TODO : Test Automation
    *
    * @param transaction The newly found transaction.
    * @see ChainEventListener
    */
  def onNewTransaction(transaction : Transaction): Unit = {
    registerTransaction(store.getOutputOwnerships().toList, transaction, None)
  }

  /** Invoked whenever a new transaction is removed from the mempool.
    * This method is not invoked for transactions that are included in a block.
    *
    * This also means the transaction does not exist in any block, as the mempool has transactions
    * that are not in any block in the best block chain.
    *
    * TODO : Test Automation
    *
    * @param transaction The transaction removed from the mempool.
    * @see ChainEventListener
    */
  def onRemoveTransaction(transaction : Transaction): Unit = {
    unregisterTransaction(store.getOutputOwnerships().toList, transaction, None)
  }

  /** Invoked whenever a new block is added to the best blockchain.
    *
    * @param chainBlock The block added to the best blockchain.
    */
  def onNewBlock(chainBlock:ChainBlock) : Unit = {
    chainBlock.block.transactions foreach { transaction =>
      registerTransaction(
        store.getOutputOwnerships().toList,
        transaction,
        Some(chainBlock)
      )
    }
  }

  /** Invoked whenever a block is removed from the best blockchain during the block reorganization.
    *
    * @param chainBlock The block to remove from the best blockchain.
    */
  def onRemoveBlock(chainBlock:ChainBlock) : Unit = {
    chainBlock.block.transactions foreach { transaction =>
      unregisterTransaction(
        store.getOutputOwnerships().toList,
        transaction,
        Some(chainBlock)
      )
    }
  }

  /** Register a transaction into the wallet database.
    * 1. Put each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database spent if this transaction spends it.
    * 3. Put the transaction as a wallet transaction if it is related to any output ownership.
    *
    * @param ownerships The list of output ownerships to check.
    * @param transaction The transaction to register.
    * @param chainBlock Some(block) if the transaction is included in a block; None otherwise.
    */
  protected[wallet] def registerTransaction(ownerships: List[OutputOwnership], transaction : Transaction, chainBlock : Option[ChainBlock]): Unit = {

    val currentTime = System.currentTimeMillis()

    // Step 1 : Calulate the transaction hash.
    val transactionHash = Hash( HashCalculator.transactionHash(transaction) )

    // If the transaction is related to the output
    var isTransactionRelated = false
    var outputIndex = 0

    ownerships foreach { ownership =>
      var isTransactionRelatedToTheOwnership = false
      // Step 2 : Put each UTXO if the output ownership owns it.
      transaction.outputs foreach { transactionOutput =>
        if (ownership.owns(transactionOutput)) {
          val outPoint = OutPoint(transactionHash, outputIndex)
          // Step 2.1 : Wallet Store : Put a UTXO into the output ownership.
          store.putTransactionOutPoint(ownership, outPoint)
          val walletOutput = WalletOutput(
            blockindex  = chainBlock.map( _.height ),
            // Whether this output is in the generation transaction.
            // TODO : BUGBUG : Need to check the outputIndex as well to see if an
            coinbase = transaction.inputs(0).outputTransactionHash.isAllZero(),
            spent = false,
            transactionOutput = transactionOutput
          )
          store.putWalletOutput(outPoint, walletOutput)
          isTransactionRelated = true
          isTransactionRelatedToTheOwnership = true
        }
        outputIndex += 1
      }

      // Step 3 : Mark a UTXO spent if this transaction spends it.
      transaction.inputs foreach { transactionInput =>
        // TODO : Check if the transaction input is generation transaction input?

        // Step 3 : Block Store : Get the transaction output the input is spending.
        val spentOutput = OutPoint(
          Hash( transactionInput.outputTransactionHash.value ),
          transactionInput.outputIndex.toInt)

        // Step 4 : Wallet Store : Mark a UTXO spent searching by OutPoint.
        if (store.markOutPointSpent(spentOutput, true)) {
          isTransactionRelated = true
          isTransactionRelatedToTheOwnership = true
        } else {
          // We should not have the output in our wallet database.
          assert( store.getWalletOutput(spentOutput).isEmpty )
        }
      }

      // Step 4 : Wallet Store : Put a transaction into the output ownership.
      if (isTransactionRelatedToTheOwnership) {
        store.putTransaction(ownership, transactionHash)
      }
    }

    // Step 5 : Add a transaction.
    if (isTransactionRelated) {
      val walletTransaction = WalletTransaction(
        blockhash         = chainBlock.map{ b => Hash(HashCalculator.blockHeaderHash(b.block.header) ) },
        blockindex        = chainBlock.map( _.height ),
        blocktime         = chainBlock.map( _.block.header.timestamp ) ,
        txid              = Some(transactionHash),
        time              = currentTime,
        transaction       : Transaction
      )
      store.putTransaction(transactionHash, walletTransaction)
    } else {
      assert(store.getTransaction(transactionHash).isEmpty)
    }
  }

  /** Register a transaction from the wallet database.
    * 1. Remove each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database unspent if this transaction spends it.
    * 3. Remove the transaction from the wallet database if it is related to any output ownership.
    *
    * @param ownerships The list of output ownerships to check.
    * @param transaction The transaction to register.
    * @param chainBlock Some(block) if the transaction is included in a block; None otherwise.
    */
  protected[wallet] def unregisterTransaction(ownerships: List[OutputOwnership], transaction : Transaction, chainBlock : Option[ChainBlock]): Unit = {

    val currentTime = System.currentTimeMillis()

    // Step 1 : Calulate the transaction hash.
    val transactionHash = Hash( HashCalculator.transactionHash(transaction) )

    // If the transaction is related to the output
    var isTransactionRelated = false
    var outputIndex = 0

    ownerships foreach { ownership =>
      var isTransactionRelatedToTheOwnership = false
      // Step 2 : Remove each UTXO if the output ownership owns it.
      transaction.outputs foreach { transactionOutput =>
        if (ownership.owns(transactionOutput)) {
          val outPoint = OutPoint(transactionHash, outputIndex)
          // Step 2.1 : Wallet Store : Remove a transaction from the output ownership by transaction hash.
          store.delTransactionOutPoint(ownership, outPoint)
          store.delWalletOutput(outPoint)
          isTransactionRelated = true
          isTransactionRelatedToTheOwnership = true
        }
        outputIndex += 1
      }

      // Step 3 : Mark a UTXO unspent if this transaction spends it.
      transaction.inputs foreach { transactionInput =>
        // TODO : Check if the transaction input is generation transaction input?

        // Step 3 : Block Store : Get the transaction output the input is spending.
        val spentOutput = OutPoint(
          Hash( transactionInput.outputTransactionHash.value ),
          transactionInput.outputIndex.toInt)

        // Step 4 : Wallet Store : Mark a UTXO spent searching by OutPoint.
        if (store.markOutPointSpent(spentOutput, false)) { // returns true if the output was found in the wallet database.
          isTransactionRelated = true
          isTransactionRelatedToTheOwnership = true
        } else {
          // We should not have the output in our wallet database.
          assert( store.getWalletOutput(spentOutput).isEmpty )
        }
      }

      // Step 4 : Wallet Store : Remove a transaction from the output ownership.
      if (isTransactionRelatedToTheOwnership) {
        store.delTransaction(ownership, transactionHash)
      }
    }

    // Step 5 : Wallet Store : Remove a transaction.
    if (isTransactionRelated) {
      store.delTransaction(transactionHash)
    } else {
      assert(store.getTransaction(transactionHash).isEmpty)
    }
  }
}