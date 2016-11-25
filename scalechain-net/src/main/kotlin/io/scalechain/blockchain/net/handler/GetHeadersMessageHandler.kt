package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.{ErrorCode, NetException}
import io.scalechain.blockchain.chain.{BlockLocatorHashes, Blockchain, BlockLocator}
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.net.message.{HeadersFactory, InvFactory}
import io.scalechain.blockchain.proto._
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
  private val logger = LoggerFactory.getLogger(GetHeadersMessageHandler.javaClass)

  /** Handle GetHeaders message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getHeaders The GetHeaders message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, getHeaders : GetHeaders ) : Unit {
    // We don't support the headers first approach yet.
    logger.warn("GetHeaders message is not supported yet.")


    /*
      // TODO : Investigate : Need to request block data for headers we got?
      // TODO : Investigate : Need to understand : GetDistanceBack

      // Step 1 : Prepare the hashes of block headers to send.
      val blockHashesToSend =
        if (getHeaders.blockLocatorHashes.isEmpty) {
          // Step 1.A : If no block locator hash was set, send the hash stop.
          if (BlockProcessor.hasNonOrphan(getHeaders.hashStop)) {
            List(getHeaders.hashStop)
          } else {
            List()
          }
        } else {
          // Step 1.B.1 : Get the list of block hashes to send.
          val locator = BlockLocator(Blockchain.get)
          // Step 1.B.2 : Skip the common block, start building the list of block hashes from the next block of the common block.
          //              Stop constructing the block hashes if we hit the count limit, 500. GetBlocks sends up to 500 block hashes.
          //              Note : GetHeaders returns the hashStop. BlockLocator.getHashes also returns the hashStop. Nothing to do for the hashStop hash.
          val blockHashes =
            // During block reorganization, transactions/blocks are attached/detached.
            // We need to synchronize with block reorganization, as getheaders message depends on a 'consistent' view of the best blockchain.
            // getheaders message should not see any inconsistent state of the best blockchain while block reorganization is in-progress.
            Blockchain.get.synchronized {
              locator.getHashes(BlockLocatorHashes(getHeaders.blockLocatorHashes), getHeaders.hashStop, maxHashCount = 2000)
            }
          blockHashes
        }

      // Step 2 : Pack the block hashes into an Inv message, and reply it to the requester.
      if (blockHashesToSend.isEmpty) {
        // Do nothing. Nothing to send.
      } else {
        val blockHeaders : List<BlockHeader> = blockHashesToSend.map { hash : Hash =>
          // As we get the block header hashes from getHashes, we are sure that we have the block header.
          BlockProcessor.getBlockHeader(hash).get
        }
        val headersMessage = HeadersFactory.create(blockHeaders)
        context.peer.send(headersMessage)
      }
    */
  }
}
