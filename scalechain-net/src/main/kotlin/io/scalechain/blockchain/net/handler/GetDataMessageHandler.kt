package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.net.MessageSummarizer
import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.*
import org.slf4j.LoggerFactory

/**
  * The message handler for GetData message.
  */
object GetDataMessageHandler {
  private val logger = LoggerFactory.getLogger(GetDataMessageHandler.javaClass)

  /** Handle GetData message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getData The GetData message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, getData : GetData ) : Unit {
    val db = Blockchain.get().db
    // TODO : Step 1 : Return an error if the number of inventories is greater than 50,000.
    // Step 2 : For each inventory, send data for it.
    val messagesToSend : List<ProtocolMessage> =
      getData.inventories.map { inventory: InvVector ->
        when(inventory.invType){
          InvType.MSG_TX -> {
            // Get the transaction we have. Orphan transactions are not returned.
            // TODO : send tx message only if it is in the relay memory. A 'tx' is put into the relay memory by sendfrom, sendtoaddress, sendmany RPC.
            // For now, send a transaction if we have it.
            // Returns Option<Transaction>

            TransactionProcessor.getTransaction(db, inventory.hash)
          }
          InvType.MSG_BLOCK -> {
            // Get the block we have. Orphan blocks are not returned.
            // Returns Option<Block>
            BlockProcessor.get().getBlock(inventory.hash)
          }
          else -> {
            logger.warn("Unknown inventory type for the inventory : ${inventory}")
            null
          }
        }
      }.filterNotNull() // Filter out None values.


    // Step 3 : Send data messages ( either Transaction or Block )
    messagesToSend.forEach { message : ProtocolMessage ->
      logger.trace("Responding to getdata. Message : ${MessageSummarizer.summarize(message)}")
      context.peer.send(message)
    }

    // TODO : Step 4 : Need to send NotFound message for not found block or transaction.
    // This is necessary for the SPV clients. We will implement this feature when we support SPV clients.
  }
}
