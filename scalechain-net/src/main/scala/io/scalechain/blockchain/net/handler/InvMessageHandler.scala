package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{Inv, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Inv message.
  */
object InvMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(InvMessageHandler.getClass)

  /** Handle Inv message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param inv The Inv message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, inv : Inv ) : Unit = {
    // TODO : Implement

/*
    // Step 1 : Return an error if the number of inventories is more than 50,000
    if (vInv.size() > 50000) ...

    // Step 2 : Add the inventory as a known inventory to the node that sent the "inv" message.
    LOOP inv := for each inventory in the "inv" message
        CNode::AddInventoryKnown
            - Put to inventory to std::set<CInv> setInventoryKnown

    // Step 3 : Check if we already have it
    bool fAlreadyHave = AlreadyHave(txdb, inv);
        - Transaction : Check the transaction database, orphan transactions, and mempool
        - Block : Check the block database, orphan blocks,

    // Step 4 : If we don't have it yet, send "getdata" message to the peer that sent the "inv" message
    pfrom->AskFor(inv)

    // Step 5 : If we already have it and it is an orphan block, send request to get the root of the orphan block.
    pfrom->PushGetBlocks(
        pindexBest,
        GetOrphanRoot(mapOrphanBlocks[inv.hash])
            // Work back to the first block in the orphan chain
            - while (mapOrphanBlocks.count(pblock->hashPrevBlock))
                  pblock = mapOrphanBlocks[pblock->hashPrevBlock];
            - return pblock->GetHash(); // This is the block the node does not have yet.
    );

*/
  }
}
