package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.net.Node
import io.scalechain.blockchain.net.message.InvFactory
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.util.Config
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.hash

/**
  * The message handler for Tx message.
  */
object TxMessageHandler {
  private val logger = LoggerFactory.getLogger(TxMessageHandler.javaClass)

  /** Handle Transaction message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param transaction The Transaction message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, transaction : Transaction ) : Unit {
    val db = Blockchain.get().db

    val transactionHash = transaction.hash()
    logger.trace("<P2P> Received a transaction. Hash : ${transactionHash}")

    // Do not process the message during initial block download.
    if ( ! Node.get().isInitialBlockDownload() ) {
      // TODO : Step 0 : Add the inventory as a known inventory to the node that sent the "tx" message.
      try {
        if (TransactionProcessor.exists(db, transactionHash)) {
          logger.trace("The transaction already exists. ${transaction}")
        } else {

          // Try to put the transaction into the disk-pool
          TransactionProcessor.putTransaction(db, transactionHash, transaction)

          // Yes! the transaction was put into the disk-pool.
          // Step 2 : Recursively check if any orphan transaction depends on this transaction.
          // Also delete the newly accepted transactions from indexes for orphan transactions.
          val acceptedChildren: List<Hash> = TransactionProcessor.acceptChildren(db, transactionHash)
          /*
                  // Step 3 : Relay the transaction as an inventory
                  val invMessage = InvFactory.createTransactionInventories(transactionHash :: acceptedChildren)
                  context.communicator.sendToAll(invMessage)
                  logger.trace(s"Propagating inventories for the newly accepted transactions. ${invMessage}")
          */
        }
      } catch(e: ChainException) {
        if (e.code == ErrorCode.ParentTransactionNotFound) {
          // A transaction pointed by an input of the transaction does not exist. add it as an orphan.
          TransactionProcessor.putOrphan(db, transactionHash, transaction)
          logger.info("An orphan transaction was received. Hash : ${transactionHash}, Transaction : ${transaction}")
        } else if (e.code == ErrorCode.TransactionOutputAlreadySpent) {
          logger.trace("A double spending transaction was received. Hash : ${transactionHash}, Transaction : ${transaction}")
        }
      }
    }


 /*
    pfrom->AddInventoryKnown(tx inventory)

    if (tx.AcceptToMemoryPool(true, &fMissingInputs)) // Not an orphan
    {
        // Step 1 : Notify the transaction, check if the transaction should be stored in the wallet.
        SyncWithWallets

        // Step 2 : Relay the transaction as an inventory
        RelayMessage
          - RelayInventory // For each connected node, relay the transaction inventory.

        // Step 3 : Recursively check if any orphan transaction depends on this transaction.
        Loop newTx := each transaction newly added
          Loop orphanTx := for each transaction that depends on the newTx
            if (orphanTx.AcceptToMemoryPool(true)) { // Not an orphan anymore
              add the tx to the newly added transactions list.

        // Step 4 : For each orphan transaction that has all inputs connected, remove from the orphan transaction.
        Loop newTx := each transaction newly added
          EraseOrphanTx(hash);
            - Remove the orphan transaction both from mapOrphanTransactions and mapOrphanTransactionsByPrev.
    }
    else if (fMissingInputs) // An orphan
    {
        // Add the transaction as an orphan transaction.
        AddOrphanTx(vMsg);
        - Add the orphan transaction to mapOrphanTransactions and mapOrphanTransactionsByPrev.
    }
 */
  }

}
