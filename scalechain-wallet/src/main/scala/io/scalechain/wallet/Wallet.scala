package io.scalechain.wallet

import java.io.File

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.{Blockchain, TransactionAnalyzer, ChainEventListener}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.{BlockIndex, DiskBlockStorage}
import io.scalechain.blockchain.transaction.SigHash.SigHash
import io.scalechain.blockchain.transaction.TransactionSigner.SignedTransaction
import io.scalechain.blockchain.transaction._
import io.scalechain.crypto.{Hash160, HashFunctions, ECKey}
import org.slf4j.LoggerFactory

object Wallet {
  private var theWallet : Wallet = null
  def create( walletPath : File) : Wallet = {
    theWallet = new Wallet(walletPath)
    theWallet
  }

  def get() = {
    assert(theWallet != null)
    theWallet
  }
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

// [Wallet layer] A wallet keeps a list of private keys, and signs transactions using a private key, etc.
class Wallet(walletFolder : File) extends ChainEventListener with AutoCloseable {
  private val logger = Logger( LoggerFactory.getLogger(classOf[Wallet]) )

  val store = new WalletStore(walletFolder)

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
  def signTransaction(transaction   : Transaction,
                      chainView     : BlockchainView,
                      dependencies  : List[UnspentTransactionOutput],
                      privateKeys   : Option[List[PrivateKey]],
                      sigHash       : SigHash
                     ) : SignedTransaction = {

    if (privateKeys.isEmpty) {
      // Wallet Store : Iterate private keys for all accounts
      val privateKeysFromWallet = store.getPrivateKeys(None)

      TransactionSigner.sign(transaction, chainView, dependencies, privateKeysFromWallet, sigHash )
    } else {
      TransactionSigner.sign(transaction, chainView, dependencies, privateKeys.get, sigHash )
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
  def getReceivedByAddress(blockchainView : BlockchainView, address : CoinAddress, minConfirmations : Long) : CoinAmount = {
    // TODO : BUGBUG : We are counting outputs that an address is just one of multiple addresses in multisig outputs.
//    println(s"getReceivedByAddress(${address.base58})")


//    println(s"outputs111=${getTransactionOutputs(Some(address))}")

    // Step 1 : Wallet Store : Iterate UTXOs for an output ownership.
    val total = getTransactionOutputs(Some(address)).foldLeft(0L) {
      // Step 2 : Sum up the amount of UTXOs.
      (sum : Long, walletOutput : WalletOutputWithInfo) => {
//        println(s"sum=${sum}, walletOutput=${walletOutput}")
        val confirmations = walletOutput.walletOutput.blockindex.map( getConfirmations(blockchainView, _) ).getOrElse(0L)

//        println(s"confirmations=${confirmations}, minConfirmations=${minConfirmations}")

        if (confirmations >= minConfirmations) {
          sum + walletOutput.walletOutput.transactionOutput.value
        } else {
          sum
        }
      }
    }
    CoinAmount.from(total)
  }

  /** Get an iterator of transaction hashes searched by an optional account.
    *
    * Category : [Output Ownership -> Transactions] - Search
    *
    * @param accountOption The account to get transactions related to an account
    */
  protected[wallet] def getTransactionHashes(accountOption : Option[String]) : Set[Hash] = {
    store.getOutputOwnerships(accountOption).flatMap { ownership : OutputOwnership =>
      store.getTransactionHashes(Some(ownership))
    }.toSet
  }


  /** Get a list of transactions for a specific account.
    *
    * @param accountOption
    * @return
    */
  protected [wallet] def getWalletTransactions(accountOption : Option[String]) : Set[WalletTransaction] = {
    getTransactionHashes(accountOption).map { transactionHash: Hash =>
      store.getWalletTransaction(transactionHash) // returns Option[WalletTransaction]
    }.filter(_.isDefined) // Filter out None values.
      .map(_.get) // Get rid of the Option wrapper.
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
  protected [wallet] def isMoreRecentThan(a : WalletTransaction, b : WalletTransaction) : Boolean = {
    a.addedTime > b.addedTime

    // Case 1 : If only one of the two transactions has an empty block index, it is more recent one as the transaction is in the mempool.
    if (a.blockIndex.isDefined && b.blockIndex.isEmpty) {
      false
    } else if (a.blockIndex.isEmpty && b.blockIndex.isDefined) { // Case 2 : Same as case 1.
      true

    } else  if (a.blockIndex.isEmpty && b.blockIndex.isEmpty) {

      // Case 3 : Both of the transactions are in the mempool.
      // Sort by receivedTime
      a.addedTime > b.addedTime
    } else { // Case 4 : Both transactions are in blocks in the best blockchain.
      // Sort by (blockIndex, transactionIndex)
      val aIndex = a.blockIndex.get
      val bIndex = b.blockIndex.get
      if ( aIndex > bIndex) {
        true
      } else if ( aIndex < bIndex ) {
        false
      } else { //aIndex == bIndex
        // We should never have two transactions that have the same transaction index in a block.
        assert(a.transactionIndex.get != b.transactionIndex.get)
        a.transactionIndex.get > b.transactionIndex.get
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
  protected [wallet] def getTransactionDescriptor( blockchainView  : BlockchainView,
                                                   walletTransaction : WalletTransaction,
                                                   inputOrOutput : Either[TransactionInput, TransactionOutput],
                                                   vout : Int,
                                                   //payingAddressOption : Option[CoinAddress],
                                                   negativeFeeOption : Option[scala.math.BigDecimal],
                                                   outputOwnershipsFilterOption : Option[List[OutputOwnership]],
                                                   includeWatchOnly  : Boolean
                                                 ) : Option[WalletTransactionDescriptor] = {

    /**
      *  category should be set to one of the following values:
      *  • send if sending payment
      *  • receive if this wallet received payment in a regular transaction
      *  • generate if a matured and spendable coinbase
      *  • immature if a coinbase that is not spendable yet
      *  • orphan if a coinbase from a block that’s not in the local best block chain
      *  • move if an off-block-chain move made with the move RPC
      */
    val env = ChainEnvironment.get

    val (ownership : OutputOwnership, category: String, amount : scala.math.BigDecimal ) = inputOrOutput match {
      case Left(input) => {
        val spentOutput = blockchainView.getTransactionOutput(input.getOutPoint)
        // We get a wallet transaction from the wallet database.
        // It means that all inputs were validated, so the output pointed by an input of a validtransaction should exist.
        val ownership = LockingScriptAnalyzer.extractOutputOwnership(spentOutput.lockingScript)
        (ownership, "send", CoinAmount.from(spentOutput.value).value)
      }
      case Right(output) => {
        val ownership = LockingScriptAnalyzer.extractOutputOwnership(output.lockingScript)

        val category = if (walletTransaction.transaction.inputs.head.isCoinBaseInput()) {
          val confirmations = walletTransaction.blockIndex.map( getConfirmations(blockchainView, _) ).getOrElse(0L)
          if (confirmations >= env.CoinbaseMaturity) {
            "generate"
          } else {
            "immature"
          }
        } else {
          "receive"
        }

        (ownership, category, CoinAmount.from(output.value).value)
      }
    }

    // TODO : BUGBUG : Need to check outputTranasctionIndex as well.
    val isCoinbase = walletTransaction.transaction.inputs(0).isCoinBaseInput()
    val confirmationsOption = walletTransaction.blockIndex.map ( getConfirmations(blockchainView, _) )

    val accountOption : Option[String] = store.getAccount(ownership)
    if (accountOption.isEmpty || (outputOwnershipsFilterOption.isDefined && !outputOwnershipsFilterOption.get.contains(ownership) ) ) {
      None
    } else {
      val addressOption: Option[CoinAddress] = ownership match {
        case address: CoinAddress => Some(address)
        case _ => None
      }

      val involvesWatchonly: Boolean = addressOption.map(address => store.getPrivateKeys(Some(address)).isEmpty).getOrElse(false)
      if (involvesWatchonly && !includeWatchOnly) {
        None
      } else {
        Some(
          WalletTransactionDescriptor(
            involvesWatchonly = involvesWatchonly,
            account = accountOption.get,
            // TODO : BUGBUG : According to the specification, this should be the "paying" address.
            address = addressOption.map(_.base58),
            //address           = payingAddressOption.map(_.base58),
            category = category.toString,
            amount = amount,
            vout = Some(vout),
            fee = negativeFeeOption,
            confirmations = confirmationsOption,
            // Set to true if the transaction is a coinbase. Not returned for regular transactions or move category payments
            generated = if (isCoinbase) Some(true) else None,
            blockhash = walletTransaction.blockHash,
            blockindex = walletTransaction.blockIndex,
            blocktime = walletTransaction.blockTime,
            txid = walletTransaction.transactionId,
            time = walletTransaction.addedTime
          )
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
  def listTransactions(
                        blockchainView  : BlockchainView,
                        accountOption   : Option[String],
                        count           : Int,
                        skip            : Long,
                        includeWatchOnly: Boolean
                      ) : List[WalletTransactionDescriptor] = {

    // The blockchain might be in progress of block reorganization.
    // As block reorganization attaches/detaches transactions, followings might be inconsistent, so we need to
    // synchronize with Blockchain so that we get the consistent view after the block reorganization finishes.
    //
    // (1) the list of transactions
    // (2) the list of transaction outputs.
    // (3) the flag whether an output was spent or not
    Blockchain.get.synchronized {

      // 1. Get transactions
      val transactions: List[WalletTransaction] = getWalletTransactions(accountOption).toList

      // 2. Sort transactions by recency in descending order. Recent transactions come first. (newest to oldest)
      val recentTransactions: List[WalletTransaction] = transactions.sortWith(isMoreRecentThan)

      val outputOwnershipsFilterOption = accountOption.map { account =>
        store.getOutputOwnerships(Some(account))
      }

      var transactionIndex = 0L
      // 3. For each transaction,
      val transactionDescriptors = (
        recentTransactions.flatMap { walletTransaction: WalletTransaction =>
          // 3. Calculate the transaction fee.
          // TODO : Optimize : We are searching transaction outputs pointed by inputs of this transaction twice.
          // A. first time -> to calculate fee.
          // B. second time -> within getTransactionDescriptor

          // The fee paid as a negative bitcoins value.
          // We will calculate the fee only if it is necenssary
          lazy val negativeFee =
            if (walletTransaction.transaction.inputs(0).isCoinBaseInput()) {
              scala.math.BigDecimal(0)
            } else {
              -TransactionAnalyzer.calculateFee(blockchainView, walletTransaction.transaction)
            }

          /*
              lazy val spentOutputs : List[TransactionOutput] = walletTransaction.transaction.inputs.map { transactionInput : TransactionInput =>
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
          val sendingTransactions = if (walletTransaction.transaction.inputs(0).isCoinBaseInput()) {
            List(None)
          } else {
            // 3.2 For each input, check if any UTXO owned by output ownership in the wallet is related. (category = send)
            var inputIndex = -1
            walletTransaction.transaction.inputs.map { transactionInput =>
              inputIndex += 1
              if (transactionIndex >= skip && transactionIndex < skip + count) {
                // Returns Option[TransactionDescriptor]
                val transactionDesc = getTransactionDescriptor(blockchainView, walletTransaction, Left(transactionInput), inputIndex, /*None No paying address,*/ Some(negativeFee), outputOwnershipsFilterOption, includeWatchOnly)
                if (transactionDesc.isDefined) {
                  transactionIndex += 1
                }
                transactionDesc
              } else {
                None
              }
            }
          }

          // 3.3 For each output, check if any UTXO owned by output ownership in the wallet is related. (category = receive)
          var outputIndex = -1
          val receivingTransactions = walletTransaction.transaction.outputs.map { transactionOutput =>
            outputIndex += 1
            if (transactionIndex >= skip && transactionIndex < skip + count) {
              // Returns Option[TransactionDescriptor]
              //            val payingAddressOption = spendingAddresses.headOption
              val transactionDesc = getTransactionDescriptor(blockchainView, walletTransaction, Right(transactionOutput), outputIndex, /*payingAddressOption,*/ None, outputOwnershipsFilterOption, includeWatchOnly)
              if (transactionDesc.isDefined) {
                transactionIndex += 1
              }
              transactionDesc
            } else {
              None
            }
          }

          sendingTransactions ::: receivingTransactions
        }
      ).filter(_.isDefined) // Filter out any None values.
      .map(_.get) // Get rid of the Option wrapper from Option[TransactionDescriptor]

      // Change the order oldest to newest.
      transactionDescriptors.reverse
    }
  }

  /** Get an iterator for UTXOs.
    *
    * Category : [Output Ownership -> UTXOs] - Search
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    * @return The iterator for UTXOs.
    */
  protected[wallet] def getTransactionOutputs(outputOwnershipOption : Option[OutputOwnership]) : List[WalletOutputWithInfo] = {
    store.getTransactionOutPoints(outputOwnershipOption).map { outPoint =>

      //println(s"getTransactionOutPoints : ${outPoint}")
      store.getWalletOutput(outPoint).map{ walletOutput => // convert WalletOutput to WalletOutputWithInfo

        //println(s"getWalletOutput : ${walletOutput}")
        WalletOutputWithInfo(
          outPoint,
          walletOutput
        )
      }
    } .filter(_.isDefined) // getWalletOutput returns Option[OutPoint]. Get rid of None values.
      .map(_.get)       // Now get rid of the Option wrapper.
  }

  /** Convert a WalletOutputWithInfo to a UnspentCoinDescriptor.
    * We got WalletOutputWithInfo from the wallet database.
    * We need UnspentCoinDescriptor to convert it to json to return as a RPC response.
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

      val confirmations = walletOutput.walletOutput.blockindex.map( getConfirmations(blockchainView, _, Some(walletOutput.outPoint) ) ).getOrElse(0L)
      val env = ChainEnvironment.get
      Some(
        UnspentCoinDescriptor(
          txid          = walletOutput.outPoint.transactionHash,
          vout          = walletOutput.outPoint.outputIndex,
          address       = addressOption.map( _.base58 ),
          account       = addressOption.flatMap( store.getAccount(_ ) ),
          scriptPubKey  = walletOutput.walletOutput.transactionOutput.lockingScript.data,
          redeemScript  = redeemScriptOption,
          amount        = CoinAmount.from(walletOutput.walletOutput.transactionOutput.value ).value,
          confirmations = confirmations,
          spendable     = !walletOutput.walletOutput.coinbase || (confirmations >= env.CoinbaseMaturity)
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
  protected [wallet] def getConfirmations( blockchainView : BlockchainView, blockHeight : Long, outPointOption : Option[OutPoint] = None) = {
    val confirmations = blockchainView.getBestBlockHeight() - blockHeight + 1

    if (confirmations < 0 ) {
      logger.error(s"Negative confirmation. best=${blockchainView.getBestBlockHeight()}, blockHeight= ${blockHeight}, outpoint = ${outPointOption}")
      assert( false )
    }
    confirmations
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

    // The blockchain might be in progress of block reorganization.
    // As block reorganization attaches/detaches transactions, followings might be inconsistent, so we need to
    // synchronize with Blockchain so that we get the consistent view after the block reorganization finishes.
    //
    // (1) the list of transactions
    // (2) the list of transaction outputs.
    // (3) the flag whether an output was spent or not
    Blockchain.get.synchronized {

      // TODO : BUGBUG : Need to consider ParsedPubKeyScript
      val allAddresses = store.getOutputOwnerships(None).collect { case address: CoinAddress => address }

      val addressesFilter =
        if (addressesOption.isDefined) {
          addressesOption.get
        } else {
          allAddresses
        }

      addressesFilter.flatMap { coinAddress =>
        // Wallet Store : Iterate UTXOs for a specific coin address
        getTransactionOutputs(Some(coinAddress))
          .filter(!_.walletOutput.spent) // filter only unspent.
          .map { walletOutput =>
          getUnspentCoinDescription(
            blockchainView, Some(coinAddress), walletOutput
          )
        }
      }.filter(_.isDefined) // getUnspentCoinDescription returns Option[UnspentCoinDescriptor]. Filter out None values.
        .map(_.get) // Get rid of the Option wrapper to convert Option[UnspentCoinDescriptor] to UnspentCoinDescriptor
        .filter(_.confirmations >= minimumConfirmations)
        .filter(_.confirmations <= maximumConfirmations)
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
  def importOutputOwnership(
     blockchainView : BlockchainView,
     account : String,
     outputOwnership : OutputOwnership,
     rescanBlockchain : Boolean
  ): Unit = {

    // Step 1 : Wallet Store : Add an output ownership to an account. Create an account if it does not exist.
    store.putOutputOwnership(account, outputOwnership)

    // Step 2 : Register the address as an receiving address.
    // TODO : Bitcoin Compatibility : bitcoind might not register the watch-only address as a receiving address.
    store.putReceivingAddress(account, outputOwnership)

    var tranasctionIndex = -1
    // Step 3 : Rescan blockchain
    if (rescanBlockchain) {
      blockchainView.getIterator(height = 0L) foreach { chainBlock : ChainBlock =>
        chainBlock.block.transactions foreach { transaction =>
          tranasctionIndex += 1
          registerTransaction(List(outputOwnership), transaction, Some(chainBlock), Some(tranasctionIndex))
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
  def getAccount(address : CoinAddress) : Option[String] = {

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

    // Step 2 : Create an address.
    val address = CoinAddress.from(privateKey)

    // Step 3 : Wallet Store : Put an address into an account.
    store.putOutputOwnership(account, address)

    // Step 4 : Wallet Store : Put the private key into an the address.
    store.putPrivateKeys(address, List(privateKey))

    // Step 5 : Put the address as the receiving address of the account.
    store.putReceivingAddress(account, address)

    address
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
  def getReceivingAddress(account:String) : CoinAddress = {
    val addressOption : Option[OutputOwnership] = store.getReceivingAddress(account)
    if ( addressOption.isEmpty ) { // The account does not have any receiving address.
      // Create a new address and set it as the receiving address of the account.
      newAddress(account)
    } else {
      addressOption.get match {
        case address : CoinAddress => address
        // We should always get coin address. Because we are not putting any parsed public key script as the ownership.
        case _ => assert(false); null
      }
    }
  }

  /**
    * Among the given output ownerships, get a list of output ownerships in the wallet database.
    *
    * @param outputOwnerships The output ownerships to check if they exist in the wallet database.
    * @return The filtered list of output ownerships that are in the wallet database.
    */
  protected[wallet] def getWalletOutputOwnerships(outputOwnerships : List[OutputOwnership]) : List[OutputOwnership] = {
    outputOwnerships.filter { ownership : OutputOwnership =>
      store.ownershipExists(ownership)
    }
  }

  /**
    * Given a locking script, get a list of output ownerships in the wallet database.
    *
    * @param lockingScript The locking script to check to produce any possible output ownerships that matches ones in the wallet database.
    * @return The filtered list of output ownerships that are in the wallet database.
    */
  protected[wallet] def getWalletOutputOwnerships(lockingScript : LockingScript) : List[OutputOwnership] = {
    val outputOwnerships : List[OutputOwnership] = LockingScriptAnalyzer.extractPossibleOutputOwnerships(lockingScript)
    getWalletOutputOwnerships(outputOwnerships)
  }

  /** Invoked whenever a new transaction comes into a mempool.
    * This method is not invoked for transactions that are included in a block.
    *
    * TODO : Test Automation
    *
    * @param transaction The newly found transaction.
    * @see ChainEventListener
    */
  def onNewTransaction(transaction : Transaction, chainBlock : Option[ChainBlock], transactionIndex : Option[Int]): Unit = {
    registerTransaction(store.getOutputOwnerships(None), transaction, chainBlock, transactionIndex)
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
    unregisterTransaction(store.getOutputOwnerships(None), transaction)
  }

  /** Register a transaction into the wallet database.
    * 1. Put each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database spent if this transaction spends it.
    * 3. Put the transaction as a wallet transaction if it is related to any output ownership.
    *
    * @param ownerships The list of output ownerships to check.
    * @param transaction The transaction to register.
    * @param chainBlock Some(block) if the transaction is included in a block; None otherwise.
    * @param transactionIndex An Additional value for sorting transactions by recency.
                              Some(transactionIndex) if the transaction is in a block on the best blockchain.
                              None if the block is in the mempool.

    */
  protected[wallet] def registerTransaction(ownerships: List[OutputOwnership], transaction : Transaction, chainBlock : Option[ChainBlock], transactionIndex : Option[Int]): Unit = {
    Blockchain.get.synchronized {
      //println(s"registerTransaction=${transaction}")

      // Step 1 : Calulate the transaction hash.
      val transactionHash = transaction.hash

      logger.trace(s"[Wallet register tx:${transactionHash}] started.")

      val addedTime = store.getWalletTransaction(transactionHash).map( _.addedTime ).getOrElse(System.currentTimeMillis())

      // If the transaction is related to the output
      var isTransactionRelated = false

      // Step 2 : Put each UTXO if the output ownership owns it.
      var outputIndex = -1
      transaction.outputs foreach { transactionOutput =>
        outputIndex += 1

        val outPoint = OutPoint(transactionHash, outputIndex)

        val walletOutputOwnerships = getWalletOutputOwnerships(transactionOutput.lockingScript)
        walletOutputOwnerships foreach { ownership : OutputOwnership =>
          store.putTransactionHash(ownership, transactionHash)
          store.putTransactionOutPoint(ownership, outPoint)

          //println(s"registerTransaction outPoint=> ${outPoint}")
          // Step 2.1 : Wallet Store : Put a UTXO into the output ownership.
        }

        if (walletOutputOwnerships.isEmpty) {
          // Do nothing, the transaction output is not related to the output ownerships in the wallet.
        } else {
          isTransactionRelated = true

          val blockHeightOption = chainBlock.map( _.height )

          val walletOutputOption = store.getWalletOutput(outPoint)

          if (walletOutputOption.isEmpty) {
            // A transaction can be registered more than once. Ex> when added to a mempool, when a block is attached.
            // So, we need to put the output only if the output does not exist yet.
            // Otherwise, we may overwrite the "spent" flag from true to false.

            val walletOutput = WalletOutput(
              blockindex  = blockHeightOption,
              // Whether this output is in the generation transaction.
              // TODO : BUGBUG : Need to check the outputIndex as well to see if an
              coinbase = transaction.inputs(0).outputTransactionHash.isAllZero(),
              spent = false,
              transactionOutput = transactionOutput
            )
            store.putWalletOutput(outPoint, walletOutput)

            logger.debug(s"[Wallet register tx:${transactionHash}] put new outpoint : ${outPoint}, wallet output : ${walletOutput}")
          } else {
            store.putWalletOutput(outPoint,
              walletOutputOption.get.copy(
                blockindex = blockHeightOption)
            )

            logger.debug(s"[Wallet register tx:${transactionHash}] updated the blockindex from ${walletOutputOption.get.blockindex} to ${blockHeightOption}. outpoint : ${outPoint}")
          }
        }
      }

      if (transaction.inputs(0).isCoinBaseInput()) {
        // do nothing
      } else {
        var inputIndex = -1
        // Step 3 : Mark a UTXO spent if this transaction spends it.
        transaction.inputs foreach { transactionInput =>
          inputIndex += 1
          // TODO : Check if the transaction input is generation transaction input?

          // Step 3 : Block Store : Get the transaction output the input is spending.
          val spentOutput = OutPoint(
            Hash( transactionInput.outputTransactionHash.value ),
            transactionInput.outputIndex.toInt)

          val walletOutputOption = store.getWalletOutput(spentOutput)
          if (walletOutputOption.isDefined) {
            val walletOutputOwnerships = getWalletOutputOwnerships(walletOutputOption.get.transactionOutput.lockingScript)
            assert(!walletOutputOwnerships.isEmpty)

            walletOutputOwnerships foreach { ownership: OutputOwnership =>
              store.putTransactionHash(ownership, transactionHash)
            }

            // We have the output in our wallet.
            // Step 4 : Wallet Store : Mark a UTXO spent searching by OutPoint.
            if ( store.markWalletOutputSpent(spentOutput, true) ) {
              logger.debug(s"[Wallet register tx:${transactionHash}] set output spent. outpoint : ${spentOutput}, inputIndex : ${inputIndex}")
            }

            isTransactionRelated = true
          }
        }
      }

      // Step 5 : Add a transaction.
      if (isTransactionRelated) {
        val walletTransaction = WalletTransaction(
          blockHash         = chainBlock.map( _.block.header.hash ),
          blockIndex        = chainBlock.map( _.height ),
          blockTime         = chainBlock.map( _.block.header.timestamp ) ,
          transactionId     = Some(transactionHash),
          addedTime         = addedTime,
          transactionIndex  = transactionIndex,
          transaction       = transaction
        )
        store.putWalletTransaction(transactionHash, walletTransaction)
      } else {
        assert(store.getWalletTransaction(transactionHash).isEmpty)
      }
    }
  }

  /** Register a transaction from the wallet database.
    * 1. Remove each UTXO if any output ownership owns it.
    * 2. Mark a UTXO in the wallet database unspent if this transaction spends it.
    * 3. Remove the transaction from the wallet database if it is related to any output ownership.
    *
    * @param ownerships The list of output ownerships to check.
    * @param transaction The transaction to register.
    */
  protected[wallet] def unregisterTransaction(ownerships: List[OutputOwnership], transaction : Transaction): Unit = {
    Blockchain.get.synchronized {
      val currentTime = System.currentTimeMillis()

      // Step 1 : Calulate the transaction hash.
      val transactionHash = transaction.hash

      logger.trace(s"[Wallet unregister tx:${transactionHash}] started.")

      // TODO : BUGBUG : When the JVM crashes while executing registerTransaction, related keys may exists on wallet indexes without the WalletTransaction being put into the wallet index.
      // If the transaction is related to the output

      var isTransactionRelated = false

      var outputIndex = -1
      // Step 2 : Remove each UTXO if the output ownership owns it.
      transaction.outputs foreach { transactionOutput =>
        outputIndex += 1
        val outPoint = OutPoint(transactionHash, outputIndex)

        val walletOutputOwnerships = getWalletOutputOwnerships(transactionOutput.lockingScript)

        walletOutputOwnerships foreach { ownership: OutputOwnership =>
          // Step 2.1 : Wallet Store : Remove a transaction from the output ownership by transaction hash.
          store.delTransactionOutPoint(ownership, outPoint)
          // Step 2.2 : Wallet Store : Remove a transaction from the output ownership.
          store.delTransactionHash(ownership, transactionHash)
        }

        if (walletOutputOwnerships.isEmpty) {
          // Do nothing, the transaction output is not related to the output ownerships in the wallet.
        } else {
          store.delWalletOutput(outPoint)

          logger.debug(s"[Wallet unregister tx:${transactionHash}] del outpoint : ${outPoint}")

          isTransactionRelated = true
        }
      }

      var inputIndex = -1
      if (transaction.inputs(0).isCoinBaseInput()) {
        // Do nothing
      } else {
        // Step 3 : Mark a UTXO unspent if this transaction spends it.
        transaction.inputs foreach { transactionInput =>
          // TODO : Check if the transaction input is generation transaction input?
          inputIndex += 1

          // Step 3 : Block Store : Get the transaction output the input is spending.
          val spentOutput = OutPoint(
            Hash( transactionInput.outputTransactionHash.value ),
            transactionInput.outputIndex.toInt)

          val walletOutputOption = store.getWalletOutput(spentOutput)

          if (walletOutputOption.isDefined) {

            val walletOutputOwnerships = getWalletOutputOwnerships(walletOutputOption.get.transactionOutput.lockingScript)
            assert(!walletOutputOwnerships.isEmpty)

            walletOutputOwnerships foreach { ownership: OutputOwnership =>
              store.delTransactionHash(ownership, transactionHash)
            }

            // Step 4 : Wallet Store : Mark a UTXO unspent searching by OutPoint.
            if (store.markWalletOutputSpent(spentOutput, false)) { // returns true if the output was found in the wallet database.
              logger.debug(s"[Wallet unregister tx:${transactionHash}] set output unspent. outpoint : ${spentOutput}, inputIndex : ${inputIndex}")
            }

            isTransactionRelated = true
          }
        }
      }
      // Step 5 : Wallet Store : Remove a transaction.
      if (isTransactionRelated) {
        store.delWalletTransaction(transactionHash)
      } else {
        assert( store.getWalletTransaction(transactionHash).isEmpty )
      }
    }
  }

  /** Close the wallet.
    *
    */
  def close() : Unit = {
    store.close
  }
}