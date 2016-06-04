package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{GetData, ProtocolMessage, Addr}
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
  def handle( context : MessageHandlerContext, getData : GetData ) : Option[ProtocolMessage] = {
    // TODO : Implement
    None

/*
    // Step 1 : Return an error if the number of inventories should is greater than 50,000.
    // Step 2 : For each inventory, send data for it.
    LOOP inv := For each inventory in the "getdata" message
        // 1. For a block hash :
            1.1 read the block from disk to send the block message.
            1.2 Using hashContinue set by 'getblocks' when it reached at the count limit, let the peer learn about our best block hash if it requested the hashConitnue block hash.
        // 2. For a transaction hash :
            send tx message only if it is in the relay memory. A 'tx' is put into the relay memory by sendfrom, sendtoaddress, sendmany RPC.
*/
  }
}
