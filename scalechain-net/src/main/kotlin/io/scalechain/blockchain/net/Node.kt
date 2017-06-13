package io.scalechain.blockchain.net

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.message.GetBlocksFactory
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.Config
import org.slf4j.LoggerFactory


/**
 * Created by kangmo on 8/8/16.
 */
class Node(private val peerCommunicator: PeerCommunicator, private val chain : Blockchain) {
    private val logger = LoggerFactory.getLogger(Node::class.java)

    // BUGBUG : What if the best peer was a malicious node?
    // The best peer for initial block download
    var bestPeerForIBD : Peer? = null

    var lastBlockHashForIBD : Hash = Hash.ALL_ZERO

    /**
     * Return the best height if we received version message more than two thirds of total nodes.
     *
     * @return Some(best Peer) that has the highest Version.startHeight if we received version message more than two thirds of total nodes. None otherwise.
     */
    fun getBestPeer() : Peer? {
        val maxPeerCount = Config.peerAddresses().size
        val peerInfos = peerCommunicator.getPeerInfos()

        // We have two way communication channels for each peer.
        // PeerInfos list each communication channel.
        val connectedPeers = peerInfos.size / 2

        // Get the total active peers including me.
        val totalActivePeers = connectedPeers + 1

        // Do we have enough number of peers? (At least more than two thirds)
        if ( totalActivePeers > maxPeerCount * 2 / 3 ) {
            // Did we receive starting height for all peers?
            if ( peerInfos.filter{ it.startingheight != null }.size == peerInfos.size ) {
                val bestPeer = peerCommunicator.getBestPeer()
                return bestPeer
            } else {
                return null
            }
        } else { // Not enough connected peers.
            return null
        }
    }

    fun updateStatus(): Unit {
        synchronized(this) {
            val bestPeerOption = getBestPeer()
            if (bestPeerOption != null) {
                val bestPeer = bestPeerOption
                if ( (bestPeer.versionOption?.startHeight ?: 0) > chain.getBestBlockHeight() ) {
                    bestPeerForIBD = bestPeer

                    /* Start IBD(Initial block download). Request getblocks message to get inv messages for our missing blocks */
                    val getBlocksMessage = GetBlocksFactory.create(lastBlockHashForIBD)
                    bestPeerForIBD!!.send(getBlocksMessage)

                    logger.info("Initial block download started. Requested getblocks message.")
                }
            }
        }
    }

    fun bestPeerStartHeight() : Int {
        synchronized(this) {
            assert(bestPeerForIBD != null)
            return bestPeerForIBD?.versionOption?.startHeight ?: 0
        }
    }

    fun sendToBestPeer(message : ProtocolMessage) : Unit {
        synchronized(this) {
            assert(bestPeerForIBD != null)
            bestPeerForIBD!!.send(message)
        }
    }

    fun isInitialBlockDownload() : Boolean {
        synchronized(this) {
            return bestPeerForIBD != null
        }
    }

    fun stopInitialBlockDownload(): Unit {
        synchronized(this) {
            bestPeerForIBD = null
        }
    }

    companion object {
        var theNode : Node? = null
        fun create(peerCommunicator: PeerCommunicator, chain : Blockchain) : Node {
            if (theNode == null) {
                theNode = Node(peerCommunicator, chain)
            }
            return theNode!!
        }
        fun get() : Node {
            assert( theNode != null )
            return theNode!!
        }
    }
}
