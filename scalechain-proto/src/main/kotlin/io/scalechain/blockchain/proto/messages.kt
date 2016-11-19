package io.scalechain.blockchain.proto

import io.scalechain.util.*
import java.math.BigInteger

/** protocols.scala ; Define protocol data classes that are aware of serialization and deserialization about the message.
 * Each data class itself consists of protocol fields.
 *
 * Case class names and comments are copied from https://en.bitcoin.it/wiki/Protocol_documentation .
 * The license of Bitcoin Wiki is Creative Commons Attribution 3.0.
 */

/** Version ; When a node creates an outgoing connection, it will immediately advertise its version.
  *  The remote node will respond with its version. No further communication is possible until both peers have exchanged their version.
 */
data class Version( val version : Int,
                    val services : java.math.BigInteger,
                    val timestamp : Long,
                    val destAddress : NetworkAddress,
                    val sourceAddress : NetworkAddress,
                    val nonce : java.math.BigInteger,
                    val userAgent : String,
                    val startHeight : Int,
                    val relay : Boolean )
: ProtocolMessage {
  override fun toString() = """Version($version, ${BigIntUtil.bint(services)}, ${timestamp}L, $destAddress, $sourceAddress, ${BigIntUtil.bint(nonce)}, \"${userAgent}\", $startHeight, $relay)"""
}


/** Verack ; The verack message is sent in reply to version.
 * This message consists of only a message header with the command string "verack".
 */
data class Verack(private val dummy : Int = 0) : ProtocolMessage {
  override fun toString() = "Verack()"
}

/** Addr ; Provide information on known nodes of the network.
 *  Non-advertised nodes should be forgotten after typically 3 hours
 */
data class Addr(val addresses : List<NetworkAddressWithTimestamp>) : ProtocolMessage {
  override fun toString() = "Addr(List(${addresses.joinToString(",")}))"
}

enum class InvType {
  ERROR, MSG_TX, MSG_BLOCK, MSG_FILTERED_BLOCK
}

data class InvVector(val invType:InvType, val hash : Hash) : ProtocolMessage {
  override fun toString() = "InvVector(InvType.$invType, $hash)"
}


/** Inv ; Allows a node to advertise its knowledge of one or more objects.
 *  It can be received unsolicited, or in reply to getblocks.
 */
data class Inv(val inventories:List<InvVector>) : ProtocolMessage {
  override fun toString() = "Inv(List(${inventories.joinToString(",")}))"
}

/** GetData ; getdata is used in response to inv, to retrieve the content of a specific object,
 * and is usually sent after receiving an inv packet, after filtering known elements.
 * It can be used to retrieve transactions, but only if they are in the memory pool or
 * relay set - arbitrary access to transactions in the chain is not allowed
 * to avoid having clients start to depend on nodes having full transaction indexes
 * (which modern nodes do not).
 */
data class GetData(val inventories:List<InvVector>) : ProtocolMessage {
  override fun toString() = "GetData(List(${inventories.joinToString(",")}))"
}


/** NotFound ; notfound is a response to a getdata, sent if any requested data items could not be relayed.
 * For example, because the requested transaction was not in the memory pool or relay set.
 */
data class NotFound(val inventories:List<InvVector>) : ProtocolMessage {
  override fun toString() = "NotFound(List(${inventories.joinToString(",")}))"
}


/** GetBlocks; Return an inv packet containing the list of blocks
 * starting right after the last known hash in the block locator object,
 * up to hash_stop or 500 blocks, whichever comes first.
 * The locator hashes are processed by a node in the order as they appear in the message.
 * If a block hash is found in the node's main chain, the list of its children is returned back via the inv message and
 * the remaining locators are ignored, no matter if the requested limit was reached, or not.
 *
 * To receive the next blocks hashes, one needs to issue getblocks again with a new block locator object.
 * Keep in mind that some clients may provide blocks which are invalid
 * if the block locator object contains a hash on the invalid branch.
 */
data class GetBlocks( val version : Long,
                      val blockLocatorHashes : List<Hash>,
                      val hashStop : Hash
                    ) : ProtocolMessage  {
  override fun toString() = "GetBlocks(${version}L, List(${blockLocatorHashes.joinToString(",")}), $hashStop)"
}


/** GetHeaders ; Return a headers packet containing the headers of blocks starting right after the last known hash
 * in the block locator object, up to hash_stop or 2000 blocks, whichever comes first.
 * To receive the next block headers, one needs to issue getheaders again with a new block locator object.
 * The getheaders command is used by thin clients to quickly download the block chain where the contents
 * of the transactions would be irrelevant (because they are not ours).
 * Keep in mind that some clients may provide headers of blocks which are invalid
 * if the block locator object contains a hash on the invalid branch.
 */
data class GetHeaders(  val version : Long,
                        val blockLocatorHashes : List<Hash>,
                        val hashStop : Hash
                     ) : ProtocolMessage {
  override fun toString() = "GetHeaders(${version}L, List(${blockLocatorHashes.joinToString(",")}), $hashStop)"
}


/** Headers ; The headers packet returns block headers in response to a getheaders packet.
 */
data class Headers(val headers:List<BlockHeader>) : ProtocolMessage {
  override fun toString() = "Headers(List(${headers.joinToString(",")}))"
}


/** GetAddr ; The getaddr message sends a request to a node asking
 * for information about known active peers to help with finding potential nodes in the network.
 * The response to receiving this message is to transmit one or more addr messages with one or
 * more peers from a database of known active peers.
 * The typical presumption is that a node is likely to be active* if it has been sending a message within the last three hours.
 *
 * No additional data is transmitted with this message.
 */
data class GetAddr(private val dummy : Int = 0) : ProtocolMessage {
  override fun toString() = "GetAddr()"
}


/** Mempool ; The mempool message sends a request to a node asking for information
 * about transactions it has verified but which have not yet confirmed. The response to receiving this message is an inv message containing the transaction hashes for all the transactions in the node's mempool.
 * No additional data is transmitted with this message.
 * It is specified in BIP 35. Since BIP 37, if a bloom filter is loaded, only transactions matching the filter are replied.
 *
 */
data class Mempool(private val dummy : Int = 0) : ProtocolMessage {
  override fun toString() = "Mempool()"
}


/** Ping ; The ping message is sent primarily to confirm that the TCP/IP connection is still valid.
 * An error in transmission is presumed to be a closed connection and the address is removed as a current peer.
 *
 * Field Size,  Description,  Data type,  Comments
 * ================================================
 *          8,        nonce,   uint64_t,  random nonce
 */
data class Ping(val nonce : java.math.BigInteger) : ProtocolMessage {
  override fun toString() = "Ping(${BigIntUtil.bint(nonce)})"
}

/** Pong ; The pong message is sent in response to a ping message.
 * In modern protocol versions, a pong response is generated using a nonce included in the ping.

  * Field Size,  Description,  Data type,  Comments
  * ================================================
  *          8,        nonce,   uint64_t,  random nonce

 */
data class Pong(val nonce : java.math.BigInteger) : ProtocolMessage {
  override fun toString() = "Pong(${BigIntUtil.bint(nonce)})"
}

enum class RejectType {
  REJECT_MALFORMED, REJECT_INVALID, REJECT_OBSOLETE, REJECT_DUPLICATE, REJECT_NONSTANDARD, REJECT_DUST, REJECT_INSUFFICIENTFEE, REJECT_CHECKPOINT
}

/** Reject ; The reject message is sent when messages are rejected.
 */
data class Reject(val message:String,
                  val rejectType:RejectType,
                  val reason : String,
                  val data : ByteArray ) : ProtocolMessage {
  override fun toString() = """Reject(\"${message}\", $rejectType, \"${reason}\", $data)"""
}


/** These messages are related to Bloom filtering of connections and are funined in BIP 0037.
 * Details : https://en.bitcoin.it/wiki/BIP_0037
 */
data class FilterLoad(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "FilterLoad()"
}


/** FilterAdd; TODO : Copy & paste description from Bitcoin protocol wiki.
 *
 */
data class FilterAdd(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "FilterAdd()"
}


/** FilterClear; TODO : Copy & paste description from Bitcoin protocol wiki.
  *
  */
data class FilterClear(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "FilterClear()"
}


/** FilterBlock; TODO : Copy & paste description from Bitcoin protocol wiki.
  *
  */
// TODO : Implement it.
data class MerkleBlock(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "MerkleBlock()"
}


/** An alert is sent between nodes to send a general notification message throughout the network.
 * If the alert can be confirmed with the signature as having come from the core development group of the Bitcoin software,
 * the message is suggested to be displayed for end-users. Attempts to perform transactions,
 * particularly automated transactions through the client, are suggested to be halted.
 *
 * The text in the Message string should be relayed to log files and any user interfaces.
 */
// TODO : Implement it.
data class Alert(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "Alert()"
}


/** SendHeaders; The sendheaders message tells the receiving peer
  * to send new block announcements using a headers message rather than an inv message.
  */
data class SendHeaders(private val dummy : Int = 1) : ProtocolMessage {
  override fun toString() = "SendHeaders()"
}

