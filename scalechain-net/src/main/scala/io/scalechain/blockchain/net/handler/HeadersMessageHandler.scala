package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{ProtocolMessage, Headers}
import org.slf4j.LoggerFactory

/**
  * The message handler for Headers message.
  */
object HeadersMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(HeadersMessageHandler.getClass)

  /** Handle Headers message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param headers The Headers message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, headers : Headers ) : Unit = {
    // TODO : Implement

/*
    // Step 1 : read block headers
    // Step 2 : Accept block headers.
    AcceptBlockHeader(header, state, &pindexLast)
        - Step 1 : Check if the block header already exists, return the block index of it if it already exists.
        - Step 2 : Check the proof of work and block timestamp.
        CheckBlockHeader(block, state)
        - Step 3 : Get the block index of the previous block.
        - Step 4 : Check proof of work, block timestamp, block checkpoint, block version based on majority of recent block versions.
        ContextualCheckBlockHeader(block, state, pindexPrev)
        - Step 5 : Add the new block as a block index.
        AddToBlockIndex(block)

    // Step 3 : Request next block headers using "getheaders" message
    //    TODO : Continue Investigation
*/
  }
}
