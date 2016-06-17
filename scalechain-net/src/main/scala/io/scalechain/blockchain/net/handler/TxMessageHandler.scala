package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.net.message.InvFactory
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{Hash, ProtocolMessage, Transaction}
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Tx message.
  */
object TxMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(TxMessageHandler.getClass)

  val chain = Blockchain.get

  /** Handle Transaction message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param transaction The Transaction message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, transaction : Transaction ) : Unit = {
    val transactionHash = transaction.hash
    logger.info(s"[P2P] Received a transaction. Hash : ${transactionHash}")

    // TODO : Step 0 : Add the inventory as a known inventory to the node that sent the "tx" message.

    try {
      // Try to put the transaction into the disk-pool
      TransactionProcessor.putTransaction(transactionHash, transaction)

      // Yes! the transaction was put into the disk-pool.
      // Step 2 : Recursively check if any orphan transaction depends on this transaction.
      // Also delete the newly accepted transactions from indexes for orphan transactions.
      val acceptedChildren : List[Hash] = TransactionProcessor.acceptChildren(transactionHash)

      // Step 3 : Relay the transaction as an inventory
      val invMessage = InvFactory.createTransactionInventories( transactionHash :: acceptedChildren )
      context.communicator.sendToAll( invMessage )
    } catch {
      case e : ChainException => {
        if (e.code == ErrorCode.ParentTransactionNotFound) {
          // A transaction pointed by an input of the transaction does not exist. add it as an orphan.
          TransactionProcessor.putOrphan(transactionHash, transaction)
        } else if (e.code == ErrorCode.TransactionOutputAlreadySpent) {
          logger.warn(s"A double spending transaction was received. Hash : ${transactionHash}, Transaction : ${transaction}")
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
