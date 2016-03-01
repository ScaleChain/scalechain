package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.TransactionHash
import spray.json.JsObject

case class PeerInfo(
  // (Since : 0.10.0) The node’s index number in the local node address database.
  id : Int, // 9
  // The IP address and port number used for the connection to the remote node.
  addr : String, // "192.0.2.113:18333"
  // Our IP address and port number according to the remote node. M
  // May be incorrect due to error or lying. Many SPV nodes set this to 127.0.0.1:8333
  addrlocal : Option[String], // "192.0.2.51:18333"
  // The services advertised by the remote node in its version message
  services : String, // "0000000000000002"
  // The Unix epoch time when we last successfully sent data to the TCP socket for this node
  lastsend : Long, // 1419277992
  // The Unix epoch time when we last received data from this node
  lastrecv : Long, // 1419277992
  // The total number of bytes we’ve sent to this node
  bytessent : Long, // 4968
  // The total number of bytes we’ve received from this node
  bytesrecv : Long,  // 105078
  // The Unix epoch time when we connected to this node
  conntime : Long, // 1419265985
  // The number of seconds this node took to respond to our last P2P ping message
  pingtime : scala.math.BigDecimal, // 0.05617800
  // The number of seconds we’ve been waiting for this node to respond to a P2P ping message.
  // Only shown if there’s an outstanding ping message
  pingwait : Option[scala.math.BigDecimal], // 0.04847123
  // The protocol version number used by this node. See the protocol versions section for more information
  version : Int, // 70001
  // The user agent this node sends in its version message.
  // This string will have been sanitized to prevent corrupting the JSON results. May be an empty string
  subver : String, // "/Satoshi:0.8.6/"
  // Set to true if this node connected to us; set to false if we connected to this node
  inbound : Boolean, // false
  // The height of the remote node’s block chain when it connected to us as reported in its version message
  startingheight : Long, // 315280
  // The ban score we’ve assigned the node based on any misbehavior it’s made.
  // By default, Bitcoin Core disconnects when the ban score reaches 100
  banscore : Int,  // 0
  // ( Since : 0.10.0 ) The highest-height header we have in common with this node based the last P2P headers message it sent us.
  // If a headers message has not been received, this will be set to -1
  synced_headers : Long,  // -1
  // ( Since : 0.10.0 ) The highest-height block we have in common with this node based on P2P inv messages this node sent us.
  // If no block inv messages have been received from this node, this will be set to -1
  synced_blocks : Long, // -1
  // ( Since : 0.10.0 ) An array of blocks which have been requested from this peer. May be empty
  // inflight item : The height of a block being requested from the remote peer.
  inflight : List[Long], // [],
  // ( Since : 0.10.0 )
  // Set to true if the remote peer has been whitelisted; otherwise, set to false.
  // Whitelisted peers will not be banned if their ban score exceeds the maximum (100 by default).
  // By default, peers connecting from localhost are whitelisted
  whitelisted : Boolean // false
)


// [Net Layer] Send requests to peers, get information about peers.
class PeerService(broker : PeerBroker) {
  /** List of responses for submitblock RPC.
    */
  object SubmitBlockResult extends Enumeration {
    val DUPLICATE         = new Val(nextId, "duplicate")
    val DUPLICATE_INVALID = new Val(nextId, "duplicate-invalid")
    val INCONCLUSIVE      = new Val(nextId, "inconclusive")
    val REJECTED          = new Val(nextId, "rejected")
  }

  /** Accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
    *
    * Used by : submitblock RPC.
    *
    * @param serializedBlock The serialized string that has block data.
    * @param parameters The JsObject we got from the second parameter of submitblock RPC. A common parameter is a workid string.
    * @return
    */
  def submitBlock(serializedBlock : String, parameters : JsObject) : SubmitBlockResult.Value = {
    // TODO : Implement
    assert(false)
    SubmitBlockResult.DUPLICATE_INVALID
  }

  /** Validates a transaction and broadcasts it to the peer-to-peer network.
    *
    * Used by : sendrawtransaction RPC.
    *
    * @param transaction The serialized transaction.
    * @param allowHighFees Whether to allow the transaction to pay a high transaction fee.
    * @return
    */
  def sendRawTransaction(transaction : String, allowHighFees : Boolean) : TransactionHash = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  def getPeerInfos() : List[PeerInfo] = {
    // TODO : Implement
    assert(false)
    null
  }
}
