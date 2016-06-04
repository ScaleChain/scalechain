package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{ProtocolMessage, Transaction}
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Transaction message.
  */
object TransactionMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(TransactionMessageHandler.getClass)

  val chain = Blockchain.get

  /** Handle Transaction message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param transaction The Transaction message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, transaction : Transaction ) : Option[ProtocolMessage] = {

    val transactionHash = transaction.hash
    if (chain.getTransaction(transactionHash).isEmpty) { // Process the transaction only if we don't have it yet.
      logger.info(s"[P2P] Received a transaction. Hash : ${transactionHash}")
      chain.putTransaction(transaction)

      // Propagate the transaction only if the block transaction was not found.
      //peerCommunication.sendToAll(transaction)
    }

 /*
    // Step 0 : Add the inventory as a known inventory to the node that sent the "tx" message.
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

    None
  }

}
