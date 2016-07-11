package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.net.{IncompleteBlockCache, BlockSigner}
import io.scalechain.blockchain.net.message.InvFactory
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{Hash, ProtocolMessage, Transaction}
import io.scalechain.util.Config
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Tx message.
  */
object TxMessageHandler {
  private lazy val logger = Logger( LoggerFactory.getLogger(TxMessageHandler.getClass) )



  /** Handle Transaction message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param transaction The Transaction message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, transaction : Transaction ) : Unit = {
    val transactionHash = transaction.hash
    logger.trace(s"[P2P] Received a transaction. Hash : ${transactionHash}")

    // TODO : Step 0 : Add the inventory as a known inventory to the node that sent the "tx" message.

    // During block reorganization, transactions/blocks are attached/detached.
    // During block reorganization, transaction indexes are inconsistent state. We need to synchronize with block reorganization.
    // Optimize : To get rid of the synchronized block, keep transactions and indexes consistent even during block reorganization.
    Blockchain.get.synchronized {

      try {
        if (TransactionProcessor.exists(transactionHash)) {
          logger.trace(s"The transaction already exists. ${transaction}")
        } else {

          if (Config.isPrivate && (Blockchain.get.getBestBlockHeight >= Config.InitialSetupBlocks)) {
            val blockSignature = BlockSigner.get.extractSignedBlockHash(Blockchain.get, transaction)
            if (blockSignature.isDefined) {
              val permissionedAddresses = context.communicator.getPermissionedAddresses()
              if ( permissionedAddresses.contains(blockSignature.get.address) ) {
                val incompleteBlock = IncompleteBlockCache.addSigningTransaction(blockSignature.get.blockHash, transaction)
                if (incompleteBlock.hasEnoughSigningTransactions(BlockMessageHandler.RequiredSigningTransactions)) {
                  BlockMessageHandler.handle(context, incompleteBlock.block.get)
                  logger.trace(s"[Transaction Handler] A block with enough signing transactions found. Delegating to BlockMessageHandler. Block Hash : ${blockSignature.get.blockHash}")
                } else {
                  logger.trace(s"[Transaction Handler] A block with signing transactions found. But need more signing transactions. Current Transactions : ${incompleteBlock.signingTxs.size}, Required : ${BlockMessageHandler.RequiredSigningTransactions}, Block Hash : ${blockSignature.get.blockHash}")
                }
              } else {
                logger.warn(s"[Transaction Handler] A signing transaction was signed by permission-less address. Signed By Address : ${blockSignature.get.address}, Permissioned Addresses : ${permissionedAddresses}")
              }
            }
          }

          // Try to put the transaction into the disk-pool
          TransactionProcessor.putTransaction(transactionHash, transaction)

          // Yes! the transaction was put into the disk-pool.
          // Step 2 : Recursively check if any orphan transaction depends on this transaction.
          // Also delete the newly accepted transactions from indexes for orphan transactions.
          val acceptedChildren: List[Hash] = TransactionProcessor.acceptChildren(transactionHash)

          // Step 3 : Relay the transaction as an inventory
          val invMessage = InvFactory.createTransactionInventories(transactionHash :: acceptedChildren)
          context.communicator.sendToAll(invMessage)
          logger.trace(s"Propagating inventories for the newly accepted transactions. ${invMessage}")
        }
      } catch {
        case e: ChainException => {
          if (e.code == ErrorCode.ParentTransactionNotFound) {
            // A transaction pointed by an input of the transaction does not exist. add it as an orphan.
            TransactionProcessor.putOrphan(transactionHash, transaction)
            logger.info(s"An orphan transaction was received. Hash : ${transactionHash}, Transaction : ${transaction}")
          } else if (e.code == ErrorCode.TransactionOutputAlreadySpent) {
            logger.trace(s"A double spending transaction was received. Hash : ${transactionHash}, Transaction : ${transaction}")
          }
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
