package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.processor.{BlockProcessor, TransactionProcessor}
import io.scalechain.blockchain.proto.InvType.InvType
import io.scalechain.blockchain.proto._
import org.slf4j.LoggerFactory

/**
  * The message handler for GetData message.
  */
object GetDataMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(GetDataMessageHandler.getClass)

  /** Handle GetData message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getData The GetData message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, getData : GetData ) : Unit = {
    // TODO : Step 1 : Return an error if the number of inventories is greater than 50,000.
    // Step 2 : For each inventory, send data for it.
    val messagesToSend : List[ProtocolMessage] =
    getData.inventories.map { inventory : InvVector =>
      inventory.invType match {
        case InvType.MSG_TX => { // Get the transaction we have. Orphan transactions are not returned.
          // TODO : send tx message only if it is in the relay memory. A 'tx' is put into the relay memory by sendfrom, sendtoaddress, sendmany RPC.
          // For now, send a transaction if we have it.
          // Returns Option[Transaction]
          TransactionProcessor.getTransaction(inventory.hash)
        }
        case InvType.MSG_BLOCK => { // Get the block we have. Orphan blocks are not returned.
          // Returns Option[Block]
          BlockProcessor.getBlock(inventory.hash)
        }
        case _ => {
          logger.warn(s"Unknown inventory type for the inventory : ${inventory}")
          None
        }
      }
    }.filter(_.isDefined).map(_.get) // Filter out None values.

    // Step 3 : Send data messages ( either Transaction or Block )
    messagesToSend foreach { message : ProtocolMessage =>
      context.peer.send(message)
    }
  }
}
