package io.scalechain.wallet

import io.scalechain.blockchain.chain.ChainEventListener
import io.scalechain.blockchain.proto.{Hash, TransactionHash, Transaction}

// [Wallet layer] A wallet keeps a list of private keys, and signs transactions using a private key, etc.
object Wallet extends ChainEventListener {
  object SigHash extends Enumeration {
    val ALL                    = new Val(nextId, "ALL")
    val NONE                   = new Val(nextId, "NONE")
    val SINGLE                 = new Val(nextId, "SINGLE")
    // BUGBUG : Should we use bitwise OR??
    val ALL_OR_ANYONECANPAY    = new Val(nextId, "ALL|ANYONECANPAY")
    val NONE_OR_ANYONECANPAY   = new Val(nextId, "NONE|ANYONECANPAY")
    val SINGLE_OR_ANYONECANPAY = new Val(nextId, "SINGLE|ANYONECANPAY")
  }

  case class SignedTransaction (
    // The transaction with a signature.
    transaction : Transaction,
    // The value true if transaction is fully signed; the value false if more signatures are required
    complete : Boolean
  )

  /** signs a transaction.
    *
    * Used by : signrawtransaction RPC.
    *
    * @param transaction The transaction to sign.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return
    */
  def signTransaction( transaction   : Transaction,
                       dependencies  : Option[List[UnspentTranasctionOutput]],
                       privateKeys   : Option[List[String]],
                       sigHash       : Option[String]
                     ) : SignedTransaction = {
    // TODO : Implement
    assert(false)
    null
  }

  case class CoinAmount(value : scala.math.BigDecimal)

  /** Returns the total amount received by the specified address
    *
    * Used by : getreceivedbyaddress RPC.
    *
    * @param address
    * @param confirmations
    */
  def getReceivedByAddress(address : CoinAddress, confirmations : Long) : CoinAmount = {
    // TODO : Implement
    assert(false)
    null
  }


  /** Describes details on a transaction.
    */
  case class TransactionDescriptor(
    involvesWatchonly : Option[Boolean],       // true,
    // The account which the payment was credited to or debited from.
    // May be an empty string (“”) for the default account
    account           : String,                // "someone else's address2",
    // The address paid in this payment, which may be someone else’s address not belonging to this wallet.
    // May be empty if the address is unknown, such as when paying to a non-standard pubkey script or if this is in the move category
    address           : Option[String],        // "n3GNqMveyvaPvUbH469vDRadqpJMPc84JA",
    // Set to one of the following values:
    // • send if sending payment
    // • receive if this wallet received payment in a regular transaction
    // • generate if a matured and spendable coinbase
    // • immature if a coinbase that is not spendable yet
    // • orphan if a coinbase from a block that’s not in the local best block chain
    // • move if an off-block-chain move made with the move RPC
    category          : String,                // "receive",
    // A negative bitcoin amount if sending payment;
    // a positive bitcoin amount if receiving payment (including coinbases)
    amount            : scala.math.BigDecimal, // 0.00050000,
    // ( Since : 0.10.0 )
    // For an output, the output index (vout) for this output in this transaction.
    // For an input, the output index for the output being spent in its transaction.
    // Because inputs list the output indexes from previous transactions,
    // more than one entry in the details array may have the same output index.
    // Not returned for move category payments
    vout              : Option[Int],           // 0,
    // If sending payment, the fee paid as a negative bitcoins value.
    // May be 0. Not returned if receiving payment or for move category payments
    fee               : Option[scala.math.BigDecimal],
    // The number of confirmations the transaction has received.
    // Will be 0 for unconfirmed and -1 for conflicted. Not returned for move category payments
    confirmations     : Option[Long],          // 34714,
    // Set to true if the transaction is a coinbase. Not returned for regular transactions or move category payments
    generated         : Option[Boolean],
    // Only returned for confirmed transactions.
    // The hash of the block on the local best block chain which includes this transaction, encoded as hex in RPC byte order
    blockhash         : Option[Hash],          // "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929",
    // Only returned for confirmed transactions.
    // The block height of the block on the local best block chain which includes this transaction
    blockindex        : Option[Long],          // 11,
    // Only returned for confirmed transactions.
    // The block header time (Unix epoch time) of the block on the local best block chain which includes this transaction
    blocktime         : Option[Long],          // 1411051649,
    // The TXID of the transaction, encoded as hex in RPC byte order. Not returned for move category payments
    txid              : Option[Hash],          // "99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d",
    // An array containing the TXIDs of other transactions that spend the same inputs (UTXOs) as this transaction.
    // Array may be empty. Not returned for move category payments
    // walletconflicts item : The TXID of a conflicting transaction, encoded as hex in RPC byte order
    walletconflicts   : List[Hash],            // : [],
    // A Unix epoch time when the transaction was added to the wallet
    time              : Long,                  // 1418695703,
    // A Unix epoch time when the transaction was detected by the local node,
    // or the time of the block on the local best block chain that included the transaction.
    // Not returned for move category payments
    timereceived      : Option[Long],          // 1418925580
    // For transaction originating with this wallet, a locally-stored comment added to the transaction.
    // Only returned in regular payments if a comment was added.
    // Always returned in move category payments. May be an empty string
    comment : Option[String],
    // For transaction originating with this wallet, a locally-stored comment added to the transaction
    // identifying who the transaction was sent to.
    // Only returned if a comment-to was added. Never returned by move category payments. May be an empty string
    to : Option[String],

    // Only returned by move category payments.
    // This is the account the bitcoins were moved from or moved to,
    // as indicated by a negative or positive amount field in this payment
    otheraccount : Option[String]
  )

  /** Returns the most recent transactions that affect the wallet.
    *
    * Used by : listtransactions RPC.
    *
    * @param account The name of an account to get transactinos from
    * @param count The number of the most recent transactions to list
    * @param skip The number of the most recent transactions which should not be returned.
    * @param includeWatchOnly If set to true, include watch-only addresses in details and calculations as if they were regular addresses belonging to the wallet.
    *
    * @return List of transactions affecting the wallet.
    */
  def listTransactions(
                        account         : String,
                        count           : Int,
                        skip            : Long,
                        includeWatchOnly: Boolean
                      ) : List[TransactionDescriptor] = {
    // TODO : Implement
    assert(false)
    null
  }


  /** Returns an array of unspent transaction outputs belonging to this wallet.
    *
    * Used by : listunspent RPC.
    *
    * @param minimumConfirmations The minimum number of confirmations the transaction containing an output must have in order to be returned.
    * @param maximumConfirmations The maximum number of confirmations the transaction containing an output may have in order to be returned.
    * @param addressesOption If present, only outputs which pay an address in this array will be returned.
    * @return a list of objects each describing an unspent output
    */
  def listUnspent(
                   minimumConfirmations: Long,
                   maximumConfirmations: Long,
                   addressesOption     : Option[List[String]]
                 ) : List[UnspentCoin] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Import a watch-only address into the wallet.
    *
    * @param outputOwnership The ownership object that describes an ownership of a coin.
    * @param account The account name
    * @param rescanBlockchain Whether to rescan whole blockchain to re-index unspent transaction outputs(coins)
    */
  def importOutputOwnership(
    outputOwnership : OutputOwnership,
    account : String,
    rescanBlockchain : Boolean
  ): Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Find an account by coin address.
    *
    * Used by : getaccount RPC.
    *
    * @param address The coin address, which is attached to the account.
    * @return The found account.
    */
  def getAccount(address : CoinAddress) : String = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Returns a new coin address for receiving payments.
    *
    * Used by : newaddress RPC.
    *
    * @return the new address for receiving payments.
    */
  def newAddress(account: String) : CoinAddress = {
    // TODO : Implement
    assert(false)
    null
  }


  /** Returns the current address for receiving payments to this account.
    *
    * Used by : getaccountaddress RPC.
    *
    * @return The coin address for receiving payments.
    */
  def getReceivingAddress(account:String) : CoinAddress = {
    // TODO : Implement
    assert(false)
    null
  }


  /** Called whenever a new transaction comes into a block or the mempool.
    *
    * @param transaction The newly found transaction.
    * @see ChainEventListener
    */
  def onNewTransaction(transaction : Transaction): Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Called whenever a new transaction is removed from the mempool.
    * This also means the transaction does not exist in any block, as the mempool has transactions
    * that are not in any block in the best block chain.
    *
    * @param transaction The transaction removed from the mempool.
    * @see ChainEventListener
    */
  def onRemoveTransaction(transaction : Transaction): Unit = {
    // TODO : Implement
    assert(false)
  }
}