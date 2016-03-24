package io.scalechain.wallet

import io.scalechain.blockchain.proto.{TransactionHash, Transaction}

import scala.collection.mutable.HashMap

// [Wallet layer] A wallet keeps a list of private keys, and signs transactions using a private key, etc.
class Wallet {

  val addressBookByName = HashMap(
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dA" -> "test1",
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dB" -> "test1",
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dC" -> "test2"
  )

  val addressBookByPurpose = HashMap(
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dA" -> "received",
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dB" -> "unknown",
    "1N11X5X9okJvwKt7MAa2ZnuiqFwJbHd8dC" -> "received"
  )

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

  /** signs a transaction in the serialized transaction format
    *
    * Used by : signrawtransaction RPC.
    *
    * @param transaction The transaction to sign as a serialized transaction.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return
    */
  def signRawTransaction(
                          transaction   : String,
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
                      ) : List[Transaction] = {
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

  /** Spends an amount from a local account to a bitcoin address.
    *
    * @param fromAccount The name of the account from which the coins should be spent.
    * @param toAddress A P2PKH or P2SH address to which the coins should be sent.
    * @param amount The amount to spend in coins. We will ensure the account has sufficient coins to pay this amount.
    * @param confirmations The minimum number of confirmations an incoming transaction must have.
    * @param comment  A locally-stored (not broadcast) comment assigned to this transaction. Default is no comment.
    * @param commentTo Meant to be used for describing who the payment was sent to. Default is no comment.
    * @return
    */
  def sendFrom(
                fromAccount:   String,
                toAddress:     CoinAddress,
                amount:        CoinAmount,
                confirmations: Long,
                comment:       Option[String],
                commentTo:     Option[String]
              ) : TransactionHash = {
    // TODO : Implement
    assert(false)
    null
  }

  /**
    *
    * @param address
    * @param account
    */
  def setAddressBook(address: CoinAddress, account: Account) = {
    addressBookByName += (address.address -> account.name)
    // addressBookByPurpose += (address.address -> address.purpose)
    // TODO: Implement DB
  }

}