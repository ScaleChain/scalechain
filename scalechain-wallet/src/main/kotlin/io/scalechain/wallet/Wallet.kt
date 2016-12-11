package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.chain.ChainEventListener
import io.scalechain.blockchain.chain.TransactionAnalyzer
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionSigner
import io.scalechain.blockchain.transaction.SignedTransaction
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.Either
import io.scalechain.util.HexUtil
import org.slf4j.LoggerFactory
import java.util.*

/** The WalletOutput with additional information such as OutPoint.
  *
  * @param outPoint The OutPoint which has the transaction hash and output index of this output.
  * @param walletOutput The wallet output stored on the wallet database.
  */
data class WalletOutputWithInfo(
                                 val outPoint : OutPoint,
                                 val walletOutput : WalletOutput
                               )

// <Wallet layer> A wallet keeps a list of private keys, and signs transactions using a private key, etc.
class Wallet() : ChainEventListener {
  private val logger = LoggerFactory.getLogger(Wallet::class.java)

  val store = WalletStore()

  /** signs a transaction.
    *
    * Used by : signrawtransaction RPC.
    *
    * TODO : Test Automation
    * TODO : Need to connect inputs before signing.
    *
    * @param transaction The transaction to sign.
    * @param chainView The view of blockchain required to get the outputs poined by inputs in this transaction.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return
    */
  fun signTransaction(db : KeyValueDatabase,
                      transaction   : Transaction,
                      chainView     : BlockchainView,
                      dependencies  : List<UnspentTransactionOutput>,
                      privateKeys   : List<PrivateKey>?,
                      sigHash       : SigHash
                     ) : SignedTransaction {

    if (privateKeys == null) {
      // Wallet Store : Iterate private keys for all accounts
      val privateKeysFromWallet = store.getPrivateKeys(db, null)

      return TransactionSigner(db).sign(transaction, chainView, dependencies, privateKeysFromWallet, sigHash )
    } else {
      return TransactionSigner(db).sign(transaction, chainView, dependencies, privateKeys, sigHash )
    }
  }

  /** Returns the total amount received by the specified address
    *
    * Used by : getreceivedbyaddress RPC.
    *
    * TODO : Test Automation
    *
    * @param address The coin address to calculate the amount of received coins.
    * @param minConfirmations The number of confirmations to filter the UTXO.
    */
  fun getReceivedByAddress(db : KeyValueDatabase, blockchainView : BlockchainView, address : CoinAddress, minConfirmations : Long) : CoinAmount {
    // TODO : BUGBUG : We are counting outputs that an address is just one of multiple addresses in multisig outputs.
//    println(s"getReceivedByAddress(${address.base58})")


//    println(s"outputs111=${getTransactionOutputs(Some(address))}")

    // Step 1 : Wallet Store : Iterate UTXOs for an output ownership.
    val total = getTransactionOutputs(db, address).fold(0L, { sum : Long, walletOutput : WalletOutputWithInfo ->
      // Step 2 : Sum up the amount of UTXOs.

//        println(s"sum=${sum}, walletOutput=${walletOutput}")
        val confirmations =
            if (walletOutput.walletOutput.blockindex == null) 0L
            else getConfirmations(db, blockchainView, walletOutput.walletOutput.blockindex!!)

//        println(s"confirmations=${confirmations}, minConfirmations=${minConfirmations}")

        if (confirmations >= minConfirmations) {
          sum + walletOutput.walletOutput.transactionOutput.value
        } else {
          sum
        }
      }
    )
    return CoinAmount.from(total)
  }

  /** Get an iterator of transaction hashes searched by an optional account.
    *
    * Category : <Output Ownership -> Transactions> - Search
    *
    * @param accountOption The account to get transactions related to an account
    */
  // BUGBUG : Use list instead of Set? To eliminate duplicate transaction hashes, are we using Set? Need investigation.
  protected fun getTransactionHashes(db : KeyValueDatabase, accountOption : String?) : List<Hash> {
    return store.getOutputOwnerships(db, accountOption).flatMap { ownership : OutputOwnership ->
      store.getTransactionHashes(db, ownership)
    }
  }


  /** Get a list of transactions for a specific account.
    *
    * @param accountOption
    * @return
    */
  protected fun getWalletTransactions(db : KeyValueDatabase, accountOption : String?) : List<WalletTransaction> {
    return getTransactionHashes(db, accountOption).map { transactionHash: Hash ->
      store.getWalletTransaction(db, transactionHash) // returns Option<WalletTransaction>
    }.filterNotNull() // Filter out null values.
  }

  protected val WalletTransactionComparatorInDescendingOrder = object : Comparator<WalletTransaction> {
    override fun compare(o1: WalletTransaction?, o2: WalletTransaction?): Int {
      if (isMoreRecentThan(o1!!, o2!!)) return -1
      else if (isMoreRecentThan(o2!!, o1!!)) return 1
      else return 0
    }

    /**
     * See if the first transaction is more recent than the second transaction.
     *
     * This method is used to sort transactions by recency in descending order.
     *
     * @param a The first transaction to compre.
     * @param b The second transaction to compre.
     * @return true if the first one is more recent. false otherwise.
     */
    protected fun isMoreRecentThan(a : WalletTransaction, b : WalletTransaction) : Boolean {
      // Case 1 : If only one of the two transactions has an empty block index, it is more recent one as the transaction is in the mempool.
      if (a.blockIndex != null && b.blockIndex == null) {
        return false
      } else if (a.blockIndex == null && b.blockIndex != null) { // Case 2 : Same as case 1.
        return true

      } else if (a.blockIndex == null && b.blockIndex == null) {

        // Case 3 : Both of the transactions are in the mempool.
        // Sort by receivedTime
        return a.addedTime > b.addedTime
      } else { // Case 4 : Both transactions are in blocks in the best blockchain.
        // Sort by (blockIndex, transactionIndex)
        val aIndex = a.blockIndex!!
        val bIndex = b.blockIndex!!
        if (aIndex > bIndex) {
          return true
        } else if (aIndex < bIndex) {
          return false
        } else { //aIndex == bIndex
          // We should never have two transactions that have the same transaction index in a block.
          assert(a.transactionIndex!! != b.transactionIndex!!)
          return a.transactionIndex!! > b.transactionIndex!!
        }
      }
    }
  }

  /** Transform WalletTransaction into TransactionDescriptor by using either an input or an output.
    * Filter transactions based on the include watchOnly flag.
    *
    * We get WalletTransaction, TransactionInput, TransactionOutput from the wallet database
    * to merge them and transform them into TransactionDescriptor, which is converted to json of RPC response.
    *
    * @param blockchainView  The view of the best blockchain.
    * @param walletTransaction The WalletTransaction to transform into a TransactionDescriptor
    * @param inputOrOutput Either an input or an output for generating the TransactionDescriptor.
    * @param vout For an output, the output index (vout) for this output in this transaction.
    *              For an input, the output index for the output being spent in its transaction.
    *              Because inputs list the output indexes from previous transactions,
    *              more than one entry in the details array may have the same output index.
    *              Not returned for move category payments
    * @param includeWatchOnly true to include transaction inputs and outputs related to the watch-only addresses. false otherwise.
    * @return the transformed TransactionDescriptor.
    */
  protected fun getTransactionDescriptor(db : KeyValueDatabase,
                                         blockchainView  : BlockchainView,
                                         walletTransaction : WalletTransaction,
                                         inputOrOutput : Either<TransactionInput, TransactionOutput>,
                                         vout : Int,
                                          //payingAddressOption : Option<CoinAddress>,
                                         negativeFeeOption : java.math.BigDecimal?,
                                         outputOwnershipsFilterOption : List<OutputOwnership>?,
                                         includeWatchOnly  : Boolean
                                        ) : WalletTransactionDescriptor? {

    /**
      *  category should be set to one of the following values:
      *  • send if sending payment
      *  • receive if this wallet received payment in a regular transaction
      *  • generate if a matured and spendable coinbase
      *  • immature if a coinbase that is not spendable yet
      *  • orphan if a coinbase from a block that’s not in the local best block chain
      *  • move if an off-block-chain move made with the move RPC
      */
    val env = ChainEnvironment.get()

    val (ownership : OutputOwnership, category: String, amount : java.math.BigDecimal ) = when {
      inputOrOutput is Either.Left -> {
        val input = inputOrOutput.value
        val spentOutput = blockchainView.getTransactionOutput(db, input.getOutPoint())
        // We get a wallet transaction from the wallet database.
        // It means that all inputs were validated, so the output pointed by an input of a validtransaction should exist.
        val ownership = LockingScriptAnalyzer.extractOutputOwnership(spentOutput.lockingScript)
        Triple(ownership, "send", CoinAmount.from(spentOutput.value).value)
      }
      inputOrOutput is Either.Right -> {
        val output = inputOrOutput.value
        val ownership = LockingScriptAnalyzer.extractOutputOwnership(output.lockingScript)

        val category = if (walletTransaction.transaction.inputs.first().isCoinBaseInput()) {
          val confirmations =
            if (walletTransaction.blockIndex == null) 0L
            else getConfirmations(db, blockchainView, walletTransaction.blockIndex!!)

          if (confirmations >= env.CoinbaseMaturity) {
            "generate"
          } else {
            "immature"
          }
        } else {
          "receive"
        }

        Triple(ownership, category, CoinAmount.from(output.value).value)
      }
      else -> throw AssertionError()
    }

    // TODO : BUGBUG : Need to check outputTranasctionIndex as well.
    val isCoinbase = walletTransaction.transaction.inputs[0].isCoinBaseInput()
    val confirmationsOption =
        if (walletTransaction.blockIndex == null) null // BUGBUG : should we return 0L instead of null?
        else getConfirmations(db, blockchainView, walletTransaction.blockIndex!! )

    val accountOption : String? = store.getAccount(db, ownership)
    if (accountOption == null || (outputOwnershipsFilterOption != null && !outputOwnershipsFilterOption.contains(ownership) ) ) {
      return null
    } else {
      val addressOption: CoinAddress? =
        if (ownership is CoinAddress) ownership
        else null


      val involvesWatchonly: Boolean =
        if (addressOption == null) false
        else store.getPrivateKeys(db, addressOption).isEmpty()

      if (involvesWatchonly && !includeWatchOnly) {
        return null
      } else {
        return WalletTransactionDescriptor(
          involvesWatchonly = involvesWatchonly,
          account = accountOption!!,
          // TODO : BUGBUG : According to the specification, this should be the "paying" address.
          address = addressOption?.base58(),
          //address           = payingAddressOption.map(_.base58),
          category = category.toString(),
          amount = amount,
          vout = vout,
          fee = negativeFeeOption,
          confirmations = confirmationsOption,
          // Set to true if the transaction is a coinbase. Not returned for regular transactions or move category payments
          generated = if (isCoinbase) true else null,
          blockhash = walletTransaction.blockHash,
          blockindex = walletTransaction.blockIndex,
          blocktime = walletTransaction.blockTime,
          txid = walletTransaction.transactionId,
          time = walletTransaction.addedTime
        )
      }
    }
  }

  /** Returns the most recent transactions that affect the wallet.
    *
    * listTransactions Converts WalletTransaction stored in the wallet database into TransactionDescriptor, which is converted to json response of RPC invocation.
    *
    * Execution Steps :
    *  1. Get transactions optionally searching with an account.
    *  2. Sort transactions by (block height, the index of the transaction in the block)
    *  3. For each transaction,
    *  3.1 For each input, check if any UTXO owned by output ownership in the wallet is related. (category = send)
    *  3.2 For each output, check if any UTXO owned by output ownership in the wallet is related. (category = receive)
    *
    * Used by : listtransactions RPC.
    *
    * TODO : Test Automation
    *
    * @param blockchainView  The view of the best blockchain.
    * @param accountOption Some(name) to get transactions related to an account; None to get transactions related to all accounts in the wallet.
    * @param count The number of the most recent transactions to list
    * @param skip The number of the most recent transactions which should not be returned.
    * @param includeWatchOnly If set to true, include watch-only addresses in details and calculations as if they were regular addresses belonging to the wallet.
    * @return List of transactions affecting the wallet.
    */
  fun listTransactions(
                        db : KeyValueDatabase,
                        blockchainView  : BlockchainView,
                        accountOption   : String?,
                        count           : Int,
                        skip            : Long,
                        includeWatchOnly: Boolean
                      ) : List<WalletTransactionDescriptor> {
    // TODO : Use RocksDB Snapshot to see consistent data.
    // 1. Get transactions
    val transactions: List<WalletTransaction> = getWalletTransactions(db, accountOption)

    // 2. Sort transactions by recency in descending order. Recent transactions come first. (newest to oldest)
    val recentTransactions: List<WalletTransaction> = transactions.sortedWith(WalletTransactionComparatorInDescendingOrder)

    val outputOwnershipsFilterOption =
        if (accountOption != null) store.getOutputOwnerships(db, accountOption)
        else null

    var transactionIndex = 0L
    // 3. For each transaction,
    val transactionDescriptors = (
      recentTransactions.flatMap { walletTransaction: WalletTransaction ->
        // 3. Calculate the transaction fee.
        // TODO : Optimize : We are searching transaction outputs pointed by inputs of this transaction twice.
        // A. first time -> to calculate fee.
        // B. second time -> within getTransactionDescriptor

        // The fee paid as a negative bitcoins value.
        val negativeFee =
          if (walletTransaction.transaction.inputs[0].isCoinBaseInput()) {
            java.math.BigDecimal.valueOf(0)
          } else {
            -TransactionAnalyzer.calculateFee(db, blockchainView, walletTransaction.transaction)
          }

        /*
            lazy val spentOutputs : List<TransactionOutput> = walletTransaction.transaction.inputs.map { transactionInput : TransactionInput =>
              blockchainView.getTransactionOutput(OutPoint(
                Hash( transactionInput.outputTransactionHash.value ),
                transactionInput.outputIndex.toInt
              ))
            }.filter(_.isDefined).map(_.get) // Remove None values, get rid Option wrapper.

            lazy val spendingAddresses = spentOutputs.flatMap{ transactionOutput =>
              LockingScriptAnalyzer.extractAddresses(transactionOutput.lockingScript))
            }
        */
        // TODO : BUGBUG : if getTransactionDescriptor returns None, we count it as skipped.
        val sendingTransactions = if (walletTransaction.transaction.inputs[0].isCoinBaseInput()) {
          listOf(null)
        } else {
          // 3.2 For each input, check if any UTXO owned by output ownership in the wallet is related. (category = send)
          var inputIndex = -1
          walletTransaction.transaction.inputs.map { transactionInput ->
            inputIndex += 1
            if (transactionIndex >= skip && transactionIndex < skip + count) {
              // Returns Option<TransactionDescriptor>
              val transactionDesc = getTransactionDescriptor(db, blockchainView, walletTransaction, Either.Left(transactionInput), inputIndex, /*None No paying address,*/ negativeFee, outputOwnershipsFilterOption, includeWatchOnly)
              if (transactionDesc != null) {
                transactionIndex += 1
              }
              transactionDesc
            } else {
              null
            }
          }
        }

        // 3.3 For each output, check if any UTXO owned by output ownership in the wallet is related. (category = receive)
        var outputIndex = -1
        val receivingTransactions = walletTransaction.transaction.outputs.map { transactionOutput ->
          outputIndex += 1
          if (transactionIndex >= skip && transactionIndex < skip + count) {
            // Returns Option<TransactionDescriptor>
            //            val payingAddressOption = spendingAddresses.headOption
            val transactionDesc = getTransactionDescriptor(db, blockchainView, walletTransaction, Either.Right(transactionOutput), outputIndex, /*payingAddressOption,*/ negativeFee, outputOwnershipsFilterOption, includeWatchOnly)
            if (transactionDesc != null) {
              transactionIndex += 1
            }
            transactionDesc
          } else {
            null
          }
        }
        sendingTransactions + receivingTransactions
      }
    ).filterNotNull() // Filter out any null values.

    // Change the order oldest to newest.
    return transactionDescriptors.reversed()
  }

  /** Get an iterator for UTXOs.
    *
    * Category : <Output Ownership -> UTXOs> - Search
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    * @return The iterator for UTXOs.
    */
  protected fun getTransactionOutputs(db : KeyValueDatabase, outputOwnershipOption : OutputOwnership?) : List<WalletOutputWithInfo> {
    return store.getTransactionOutPoints(db, outputOwnershipOption).map { outPoint ->

      //println(s"getTransactionOutPoints : ${outPoint}")
      // convert WalletOutput to WalletOutputWithInfo
      val walletOutput = store.getWalletOutput(db, outPoint)
      if (walletOutput != null) {
        //println(s"getWalletOutput : ${walletOutput}")
        WalletOutputWithInfo(
          outPoint,
          walletOutput
        )
      } else {
        null
      }
    } .filterNotNull() // getWalletOutput returns OutPoint?. Get rid of null values.
  }

  /** Convert a WalletOutputWithInfo to a UnspentCoinDescriptor.
    * We got WalletOutputWithInfo from the wallet database.
    * We need UnspentCoinDescriptor to convert it to json to return as a RPC response.
    *
    * @param blockchainView Provides a read-only view of the blockchain.
    * @param walletOutput Some(output) if it is not spent yet. None if it is already spent.
    * @return The converted UnspentCoinDescriptor.
    */
  protected fun getUnspentCoinDescription( db : KeyValueDatabase,
                                           blockchainView : BlockchainView,
                                           addressOption  : CoinAddress?,
                                           walletOutput   : WalletOutputWithInfo ) : UnspentCoinDescriptor? {
    if ( walletOutput.walletOutput.spent ) {
      return null
    } else {
      // TODO : BUGBUG : Extract redeem script.
      val redeemScriptOption   : String? = null

      val confirmations =
        if (walletOutput.walletOutput.blockindex == null) 0L
        else getConfirmations(db, blockchainView, walletOutput.walletOutput.blockindex!!, walletOutput.outPoint)

      val env = ChainEnvironment.get()

      return UnspentCoinDescriptor(
        txid          = walletOutput.outPoint.transactionHash,
        vout          = walletOutput.outPoint.outputIndex,
        address       = addressOption?.base58(),
        account       = if (addressOption != null) store.getAccount( db, addressOption ) else null,
        scriptPubKey  = HexUtil.hex(walletOutput.walletOutput.transactionOutput.lockingScript.data),
        redeemScript  = redeemScriptOption,
        amount        = CoinAmount.from(walletOutput.walletOutput.transactionOutput.value ).value,
        confirmations = confirmations,
        spendable     = !walletOutput.walletOutput.coinbase || (confirmations >= env.CoinbaseMaturity)
      )
    }
  }

  /** Get the number of confirmations for a transaction in a block.
    *
    * @param blockchainView The read-only view of the blockchain.
    * @param blockHeight The height of a block, where the transaction exists.
    * @return
    */
  protected fun getConfirmations( db : KeyValueDatabase, blockchainView : BlockchainView, blockHeight : Long, outPointOption : OutPoint? = null) : Long {
    val confirmations = blockchainView.getBestBlockHeight() - blockHeight + 1

    if (confirmations < 0 ) {
      logger.error("Negative confirmation. best=${blockchainView.getBestBlockHeight()}, blockHeight= ${blockHeight}, outpoint = ${outPointOption}")
      assert( false )
    }
    return confirmations
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
  fun listUnspent(
                   db : KeyValueDatabase,
                   blockchainView : BlockchainView,
                   minimumConfirmations: Long,
                   maximumConfirmations: Long,
                   addressesOption     : List<CoinAddress>?
                 ) : List<UnspentCoinDescriptor> {
    // Need to wait for registerTransaction/unregisterTransaction, otherwise we may return a coin as unspent,
    // even though it is being marked as spent by the registerTransaction.
    return synchronized(this) {
      // TODO : Use RocksDB Snapshot to see consistent data.
      // TODO : BUGBUG : Need to consider ParsedPubKeyScript

      val addressesFilter =
        if (addressesOption != null) {
          addressesOption
        } else {
          val allAddresses : List<CoinAddress> = store.getOutputOwnerships(db, null).map { ownership ->
            if (ownership is CoinAddress) ownership
            else null // filter out ParsedPubKeyScript
          }.filterNotNull()
          allAddresses
        }

      addressesFilter.flatMap { coinAddress ->
        // Wallet Store : Iterate UTXOs for a specific coin address
        getTransactionOutputs(db, coinAddress)
          .filter { !it.walletOutput.spent } // filter only unspent.
          .map { walletOutput ->
            getUnspentCoinDescription( db, blockchainView, coinAddress, walletOutput)
          }
      }.filterNotNull() // getUnspentCoinDescription returns Option<UnspentCoinDescriptor>. Filter out None values.
        .filter{ it.confirmations >= minimumConfirmations }
        .filter{ it.confirmations <= maximumConfirmations }
    }
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
  fun importOutputOwnership(
     db : KeyValueDatabase,
     blockchainView : BlockchainView,
     account : String,
     outputOwnership : OutputOwnership,
     rescanBlockchain : Boolean
  ) : Unit {

    // TODO : Need to support ParsedPubKeyScript.
    if (outputOwnership is ParsedPubKeyScript) {
      logger.warn("Public key scripts are not supported for importaddress RPC.")
      throw WalletException(ErrorCode.UnsupportedFeature)
    }

    // Step 1 : Wallet Store : Add an output ownership to an account. Create an account if it does not exist.
    store.putOutputOwnership(db, account, outputOwnership)

    // Step 2 : Register the address as an receiving address.
    // TODO : Bitcoin Compatibility : bitcoind might not register the watch-only address as a receiving address.
    store.putReceivingAddress(db, account, outputOwnership)

    var tranasctionIndex = -1
    // Step 3 : Rescan blockchain
    if (rescanBlockchain) {
      blockchainView.getIterator(db, height = 0L).forEach { chainBlock : ChainBlock ->
        chainBlock.block.transactions.forEach { transaction ->
          tranasctionIndex += 1
          registerTransaction(db, transaction.hash(), transaction, chainBlock, tranasctionIndex)
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
  fun getAccount(db : KeyValueDatabase, address : CoinAddress) : String? {
    return store.getAccount(db, address)
  }

  /** Returns a new coin address for receiving payments.
    *
    * TODO : Test Automation
    * Used by : newaddress RPC.
    *
    * @return the address for receiving payments.
    */
  fun newAddress(db : KeyValueDatabase, account: String) : CoinAddress {
    // Step 1 : Generate a random number and a private key.
    val privateKey : PrivateKey = PrivateKey.generate()

    // Step 2 : Create an address.
    val address = CoinAddress.from(privateKey)

    // Step 3 : Wallet Store : Put an address into an account.
    store.putOutputOwnership(db, account, address)

    // Step 4 : Wallet Store : Put the private key into an the address.
    store.putPrivateKeys(db, address, listOf(privateKey))

    // Step 5 : Put the address as the receiving address of the account.
    store.putReceivingAddress(db, account, address)

    return address
  }


  /** Get an iterator private keys for an address or all accounts.
    *
    * TODO : Add test
    *
    * Category : <Account -> Output Ownerships> - Search
    *
    * @param addressOption Some(address) to get private keys for an address. A Multisig address may have multiple keys for it.
    *                      None to get private keys for all accounts.
    */
  fun getPrivateKeys(db : KeyValueDatabase, addressOption : OutputOwnership?) : List<PrivateKey> {
    return store.getPrivateKeys(db, addressOption)
  }


  /** Returns the current address for receiving payments to this account.
    * Create one if not existent.
    *
    * Used by : getaccountaddress RPC.
    *
    * TODO : Test Automation
    *
    * @return The coin address for receiving payments.
    */
  fun getReceivingAddress(db : KeyValueDatabase, account:String) : CoinAddress {
    val addressOption : OutputOwnership? = store.getReceivingAddress(db, account)
    if ( addressOption == null ) { // The account does not have any receiving address.
      // Create a address and set it as the receiving address of the account.
      return newAddress(db, account)
    } else {
      return when {
        addressOption is CoinAddress -> addressOption
        // We should always get coin address. Because we are not putting any parsed public key script as the ownership.
        else -> throw AssertionError()
      }
    }
  }

  /**
    * Among the given output ownerships, get a list of output ownerships in the wallet database.
    *
    * @param outputOwnerships The output ownerships to check if they exist in the wallet database.
    * @return The filtered list of output ownerships that are in the wallet database.
    */
  protected fun getWalletOutputOwnerships(db : KeyValueDatabase, outputOwnerships : List<OutputOwnership>) : List<OutputOwnership> {
    return outputOwnerships.filter { ownership : OutputOwnership ->
      store.ownershipExists(db, ownership)
    }
  }

  /**
    * Given a locking script, get a list of output ownerships in the wallet database.
    *
    * @param lockingScript The locking script to check to produce any possible output ownerships that matches ones in the wallet database.
    * @return The filtered list of output ownerships that are in the wallet database.
    */
  protected fun getWalletOutputOwnerships(db : KeyValueDatabase, lockingScript : LockingScript) : List<OutputOwnership> {
    val outputOwnerships : List<OutputOwnership> = LockingScriptAnalyzer.extractPossibleOutputOwnerships(lockingScript)
    return getWalletOutputOwnerships(db, outputOwnerships)
  }

  /** Invoked whenever a transaction comes into a mempool.
    * This method is not invoked for transactions that are included in a block.
    *
    * TODO : Test Automation
    *
    * @param transaction The newly found transaction.
    * @see ChainEventListener
    */
  override fun onNewTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction, chainBlock : ChainBlock?, transactionIndex : Int?): Unit {
    registerTransaction(db, transactionHash, transaction, chainBlock, transactionIndex)
  }

  /** Invoked whenever a transaction is removed from the mempool.
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
  override fun onRemoveTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction): Unit {
    unregisterTransaction(db, transactionHash, transaction)
  }

  /** Register a transaction into the wallet database.
    * 1. Put each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database spent if this transaction spends it.
    * 3. Put the transaction as a wallet transaction if it is related to any output ownership.
    *
    * @param transaction The transaction to register.
    * @param chainBlock Some(block) if the transaction is included in a block; None otherwise.
    * @param transactionIndex An Additional value for sorting transactions by recency.
                              Some(transactionIndex) if the transaction is in a block on the best blockchain.
                              None if the block is in the mempool.

    */
  protected fun registerTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction, chainBlock : ChainBlock?, transactionIndex : Int?) : Unit {
    synchronized(this) { // threads can compete : (1) block attach/detach (2) putTransaction.
      //println(s"registerTransaction=${transaction}")

      logger.trace("<Wallet register tx:${transactionHash}> started.")

      // If the transaction is related to the output
      var isTransactionRelated = false

      // Step 2 : Put each UTXO if the output ownership owns it.
      var outputIndex = -1
      transaction.outputs.forEach { transactionOutput ->
        outputIndex += 1

        val outPoint = OutPoint(transactionHash, outputIndex)

        val walletOutputOwnerships = getWalletOutputOwnerships(db, transactionOutput.lockingScript)

        if (walletOutputOwnerships.isEmpty()) {
          // Do nothing, the transaction output is not related to the output ownerships in the wallet.
        } else {

          walletOutputOwnerships.forEach { ownership: OutputOwnership ->
            store.putTransactionHash(db, ownership, transactionHash)
            store.putTransactionOutPoint(db, ownership, outPoint)

            //println(s"registerTransaction outPoint=> ${outPoint}")
            // Step 2.1 : Wallet Store : Put a UTXO into the output ownership.
          }

          isTransactionRelated = true

          val blockHeightOption = chainBlock?.height

          val walletOutputOption = store.getWalletOutput(db, outPoint)

          if (walletOutputOption == null) {
            // A transaction can be registered more than once. Ex> when added to a mempool, when a block is attached.
            // So, we need to put the output only if the output does not exist yet.
            // Otherwise, we may overwrite the "spent" flag from true to false.

            val walletOutput = WalletOutput(
              blockindex = blockHeightOption,
              // Whether this output is in the generation transaction.
              // TODO : BUGBUG : Need to check the outputIndex as well to see if an
              coinbase = transaction.inputs[0].outputTransactionHash.isAllZero(),
              spent = false,
              transactionOutput = transactionOutput
            )

            store.putWalletOutput(db, outPoint, walletOutput)

            logger.trace("<Wallet register tx:${transactionHash}> put outpoint : ${outPoint}, wallet output : ${walletOutput}")
          } else {
            store.putWalletOutput(
              db,
              outPoint,
              walletOutputOption.copy(
                blockindex = blockHeightOption)
            )

            logger.trace("<Wallet register tx:${transactionHash}> updated the blockindex from ${walletOutputOption.blockindex} to ${blockHeightOption}. outpoint : ${outPoint}")
          }
        }
      }


      if (transaction.inputs[0].isCoinBaseInput()) {
        // do nothing
      } else {
        var inputIndex = -1
        // Step 3 : Mark a UTXO spent if this transaction spends it.
        transaction.inputs.forEach { transactionInput ->
          inputIndex += 1
          // TODO : Check if the transaction input is generation transaction input?

          // Step 3 : Block Store : Get the transaction output the input is spending.
          val spentOutput = OutPoint(
            Hash(transactionInput.outputTransactionHash.value),
            transactionInput.outputIndex.toInt())

          val walletOutputOption = store.getWalletOutput(db, spentOutput)
          if (walletOutputOption != null) {
            val walletOutputOwnerships = getWalletOutputOwnerships(db, walletOutputOption.transactionOutput.lockingScript)
            assert(!walletOutputOwnerships.isEmpty())

            walletOutputOwnerships.forEach { ownership: OutputOwnership ->
              store.putTransactionHash(db, ownership, transactionHash)
            }

            // We have the output in our wallet.
            // Step 4 : Wallet Store : Mark a UTXO spent searching by OutPoint.
            if (store.markWalletOutputSpent(db, spentOutput, true)) {

              logger.trace("<Wallet register tx:${transactionHash}> set output spent. outpoint : ${spentOutput}, inputIndex : ${inputIndex}")
            }

            isTransactionRelated = true
          }
        }
      }

      // Step 5 : Add a transaction.
      if (isTransactionRelated) {
        val addedTime = store.getWalletTransaction(db, transactionHash)?.addedTime ?: System.currentTimeMillis()

        val walletTransaction = WalletTransaction(
          blockHash         = chainBlock?.block?.header?.hash(),
          blockIndex        = chainBlock?.height,
          blockTime         = chainBlock?.block?.header?.timestamp ,
          transactionId     = transactionHash,
          addedTime         = addedTime,
          transactionIndex  = transactionIndex,
          transaction       = transaction
        )
        store.putWalletTransaction(db, transactionHash, walletTransaction)
      } else {
        assert(store.getWalletTransaction(db, transactionHash) == null)
      }
    }
  }

  /** Register a transaction from the wallet database.
    * 1. Remove each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database unspent if this transaction spends it.
    * 3. Remove the transaction from the wallet database if it is related to any output ownership.
    *
    * @param transaction The transaction to register.
    */
  protected fun unregisterTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction) : Unit {
    synchronized(this) { // threads can compete : (1) block attach/detach (2) putTransaction.
      val currentTime = System.currentTimeMillis()

      logger.trace("<Wallet unregister tx:${transactionHash}> started.")

      // TODO : BUGBUG : When the JVM crashes while executing registerTransaction, related keys may exists on wallet indexes without the WalletTransaction being put into the wallet index.
      // If the transaction is related to the output

      var isTransactionRelated = false

      var outputIndex = -1
      // Step 2 : Remove each UTXO if the output ownership owns it.
      transaction.outputs.forEach { transactionOutput ->
        outputIndex += 1
        val outPoint = OutPoint(transactionHash, outputIndex)

        val walletOutputOwnerships = getWalletOutputOwnerships(db, transactionOutput.lockingScript)

        walletOutputOwnerships.forEach { ownership: OutputOwnership ->
          // Step 2.1 : Wallet Store : Remove a transaction from the output ownership by transaction hash.
          store.delTransactionOutPoint(db, ownership, outPoint)
          // Step 2.2 : Wallet Store : Remove a transaction from the output ownership.
          store.delTransactionHash(db, ownership, transactionHash)
        }

        if (walletOutputOwnerships.isEmpty()) {
          // Do nothing, the transaction output is not related to the output ownerships in the wallet.
        } else {
          store.delWalletOutput(db, outPoint)

          logger.trace("<Wallet unregister tx:${transactionHash}> del outpoint : ${outPoint}")

          isTransactionRelated = true
        }
      }

      var inputIndex = -1
      if (transaction.inputs[0].isCoinBaseInput()) {
        // Do nothing
      } else {
        // Step 3 : Mark a UTXO unspent if this transaction spends it.
        transaction.inputs.forEach { transactionInput ->
          // TODO : Check if the transaction input is generation transaction input?
          inputIndex += 1

          // Step 3 : Block Store : Get the transaction output the input is spending.
          val spentOutput = OutPoint(
            Hash( transactionInput.outputTransactionHash.value ),
            transactionInput.outputIndex.toInt())

          val walletOutputOption = store.getWalletOutput(db, spentOutput)

          if (walletOutputOption != null) {

            val walletOutputOwnerships = getWalletOutputOwnerships(db, walletOutputOption.transactionOutput.lockingScript)
            assert(!walletOutputOwnerships.isEmpty())

            walletOutputOwnerships.forEach { ownership: OutputOwnership ->
              store.delTransactionHash(db, ownership, transactionHash)
            }

            // Step 4 : Wallet Store : Mark a UTXO unspent searching by OutPoint.
            if (store.markWalletOutputSpent(db, spentOutput, false)) { // returns true if the output was found in the wallet database.
              logger.trace("<Wallet unregister tx:${transactionHash}> set output unspent. outpoint : ${spentOutput}, inputIndex : ${inputIndex}")
            }

            isTransactionRelated = true
          }
        }
      }
      // Step 5 : Wallet Store : Remove a transaction.
      if (isTransactionRelated) {
        store.delWalletTransaction(db, transactionHash)
      } else {
        assert( store.getWalletTransaction(db, transactionHash) == null )
      }
    }
  }
  companion object {
    private var theWallet : Wallet? = null
    fun create() : Wallet {
      theWallet = Wallet()
      return theWallet!!
    }

    fun get() : Wallet {
      return theWallet!!
    }
  }
}
