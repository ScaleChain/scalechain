package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.command.blockchain.GetBlockResult
import io.scalechain.blockchain.api.command.network.PeerInfo
import io.scalechain.blockchain.api.command.rawtx.{DecodedRawTransaction, SignRawTransactionResult, UnspentTranasctionOutput, RawTransaction}
import io.scalechain.blockchain.api.command.wallet.{UnspentCoin, TransactionItem}
import io.scalechain.blockchain.proto._
import spray.json.{JsObject, JsValue}

class BlockFormatter {
  /** Get the GetBlockResult case class instance from a block.
    *
    * Used by : getblockhash RPC.
    * @param block The block to format.
    * @return The GetBlockResult instance.
    */
  def getBlockResult(block : Block) : GetBlockResult = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get a serialized block data.
    *
    * Used by : getblockhash RPC.
    *
    * @param block The block to format.
    * @return The serialized string value.
    */
  def getSerializedBlock(block : Block) : String = {
    // TODO : Implement
    assert(false)
    null
  }
}


class BlockStore {
  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash : Hash = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : BlockHash ) : Block = {
    // TODO : Implement
    assert(false)
    null
  }
}

class Mempool {

}

class TransactionFormatter {
  /** Convert a transaction to a RawTransaction instance.
    *
    * Used by getrawtransaction RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted RawTransaction instance.
    */
  def getRawTransaction(transaction : Transaction) : RawTransaction = {
    // TODO : Implement
    assert(false)
    null
  }


  /** Convert a transaction to a TransactionItem, which is an element of array to respond for listtransactions RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted transaction item.
    */
  def getTransactionItem( transaction : Transaction ) : TransactionItem =  {
    // TODO : Implement
    assert(false)
    null
  }
}

class TransactionDecoder {
  /** Decodes a serialized transaction hex string into a DecodedRawTransaction.
    *
    * Used by : decoderawtransaction RPC.
    *
    * @param serializedTransction The serialized transaction.
    * @return The decoded transaction, DecodedRawTransaction instance.
    */
  def decodeRawTransaction(serializedTransction : String) : DecodedRawTransaction = {
    // TODO : Implement
    assert(false)
    null
  }
}

class Wallet {
  object SigHash extends Enumeration {
    val ALL                    = new Val(nextId, "ALL")
    val NONE                   = new Val(nextId, "NONE")
    val SINGLE                 = new Val(nextId, "SINGLE")
    // BUGBUG : Should we use bitwise OR??
    val ALL_OR_ANYONECANPAY    = new Val(nextId, "ALL|ANYONECANPAY")
    val NONE_OR_ANYONECANPAY   = new Val(nextId, "NONE|ANYONECANPAY")
    val SINGLE_OR_ANYONECANPAY = new Val(nextId, "SINGLE|ANYONECANPAY")
  }

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
  ) : SignRawTransactionResult = {
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

}

class TransactionStore {
  /** Find a transaction by transaction hash.
    *
    * Used by : getrawtransaction RPC.
    *
    * @param transactionHash The transaction hash.
    * @return The found transaction
    */
  def getTransaction(transactionHash : TransactionHash) : Transaction = {
    // TODO : Implement
    assert(false)
    null
  }
}

class PeerBroker {
  /** List of responses for submitblock RPC.
    */
  object SubmitBlockResult extends Enumeration {
    val DUPLICATE         = new Val(nextId, "duplicate")
    val DUPLICATE_INVALID = new Val(nextId, "duplicate-invalid")
    val INCONCLUSIVE      = new Val(nextId, "inconclusive")
    val REJECTED          = new Val(nextId, "rejected")
  }

  /** Accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
    *
    * Used by : submitblock RPC.
    *
    * @param serializedBlock The serialized string that has block data.
    * @param parameters The JsObject we got from the second parameter of submitblock RPC. A common parameter is a workid string.
    * @return
    */
  def submitBlock(serializedBlock : String, parameters : JsObject) : SubmitBlockResult.Value = {
    // TODO : Implement
    assert(false)
    SubmitBlockResult.DUPLICATE_INVALID
  }

  /** Validates a transaction and broadcasts it to the peer-to-peer network.
    *
    * Used by : sendrawtransaction RPC.
    *
    * @param transaction The serialized transaction.
    * @param allowHighFees Whether to allow the transaction to pay a high transaction fee.
    * @return
    */
  def sendRawTransaction(transaction : String, allowHighFees : Boolean) : TransactionHash = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  def getPeerInfos() : List[PeerInfo] = {
    // TODO : Implement
    assert(false)
    null
  }
}

case class CoinAddress(address:String)

case class Account(name:String) {
  /** Returns the current address for receiving payments to this account.
    *
    * Used by : getaccountaddress RPC.
    *
    * @return The coin address for receiving payments.
    */
  def getReceivingAddress : CoinAddress = {
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
  def newAddress : CoinAddress = {
    // TODO : Implement
    assert(false)
    null
  }
}


class AccountStore {
  /** Find an account by coin address.
    *
    * Used by : getaccount RPC.
    *
    * @param address The coin address, which is attached to the account.
    * @return The found account.
    */
  def getAccount(address : CoinAddress) : Account = {
    // TODO : Implement
    assert(false)
    null
  }
}