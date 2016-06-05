package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{GetHeaders, ProtocolMessage, Block}
import org.slf4j.LoggerFactory

/**
  * The message handler for GetHeaders message.
  *
  * "getheaders" is nearly identical to "getblocks", but it is different in three ways.
  * 1. getheaders sends up to 2000 headers, whereas getblocks sends up to 500 invs
  * 2. getheaders responds with "headers" message, whereas getblocks responds with "inv" message.
  * 3. The "headers" message contains a list of (block header and a byte zero indicating 0 transactions), whereas "inv" message has the hash of the block header.
  * 4. getheaders sends the hashStop, whereas getblocks does not send the hashStop.
  *
  */
object GetHeadersMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(GetHeadersMessageHandler.getClass)

  /** Handle GetHeaders message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getHeaders The GetHeaders message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, getHeaders : GetHeaders ) : Unit = {
    // TODO : Implement

/*
    // Step 1 : Get the latest common block with the caller in the best blockchain.
    // Step 2 : If no common block was found, send the hashStop
    // Step 3 : If any common block was found, send up to 2000 headers including the hashStop.
    //          Investigate : Need to understand : GetDistanceBack

*/
  }
}
