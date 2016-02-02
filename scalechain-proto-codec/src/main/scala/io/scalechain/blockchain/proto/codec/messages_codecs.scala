package io.scalechain.blockchain.proto.codec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{VarList, UInt64Codec}
import io.scalechain.io.{BlockDataInputStream, BlockDataOutputStream}
import scodec.bits.BitVector
import scodec.{DecodeResult, Attempt, Codec}
import scodec.codecs._


/**
  * [Description]
  * The version message provides information about the transmitting node
  * to the receiving node at the beginning of a connection.
  *
  * Until both peers have exchanged version messages, no other messages will be accepted.
  *
  * If a version message is accepted, the receiving node should send a verack message—
  * but no node should send a verack message before initializing its half of the connection
  * by first sending a version message.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#version
  *
  * [Protocol]
  *  72110100 ........................... Protocol version: 70002
  *  0100000000000000 ................... Services: NODE_NETWORK
  *  bc8f5e5400000000 ................... Epoch time: 1415483324
  *
  *  0100000000000000 ................... Receiving node's services
  *  00000000000000000000ffffc61b6409 ... Receiving node's IPv6 address
  *  208d ............................... Receiving node's port number
  *
  *  0100000000000000 ................... Transmitting node's services
  *  00000000000000000000ffffcb0071c0 ... Transmitting node's IPv6 address
  *  208d ............................... Transmitting node's port number
  *
  *  128035cbc97953f8 ................... Nonce
  *
  *  0f ................................. Bytes in user agent string: 15
  *  2f5361746f7368693a302e392e332f ..... User agent: /Satoshi:0.9.2.1/
  *
  *  cf050500 ........................... Start height: 329167
  *  01 ................................. Relay flag: true
  */
object VersionCodec extends ProtocolMessageCodec[Version] {
  val command = "version"
  val clazz = classOf[Version]

  // TODO : Implement
  val codec : Codec[Version] = null
}


/**
  * [Description]
  * The verack message acknowledges a previously-received version message,
  * informing the connecting node that it can begin to send other messages.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#verack
  *
  * [Protocol]
  *  No payload.
  */
object VerackCodec extends ProtocolMessageCodec[Verack] {
  val command = "verack"
  val clazz = classOf[Verack]

  // TODO : Implement
  val codec : Codec[Verack] = null
}


/**
  * [Description]
  * The addr (IP address) message relays connection information for peers on the network.
  * Each peer which wants to accept incoming connections creates an addr message
  * providing its connection information and then sends that message to its peers unsolicited.
  *
  * Some of its peers send that information to their peers (also unsolicited),
  * some of which further distribute it, allowing decentralized peer discovery
  * for any program already on the network.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#addr
  *
  * [Protocol]
  *  fde803 ............................. Address count: 1000
  *
  *  d91f4854 ........................... Epoch time: 1414012889
  *  0100000000000000 ................... Service bits: 01 (network node)
  *  00000000000000000000ffffc0000233 ... IP Address: ::ffff:192.0.2.51
  *  208d ............................... Port: 8333
  *
  *  [...] .............................. (999 more addresses omitted)
  */
object AddrCodec extends ProtocolMessageCodec[Addr] {
  val command = "addr"
  val clazz = classOf[Addr]

  // TODO : Implement
  val codec : Codec[Addr] = null
}

/**
  * [Description]
  * The inv message (inventory message) transmits one or more inventories of objects
  * known to the transmitting peer. It can be sent unsolicited to announce new transactions or blocks,
  * or it can be sent in reply to a getblocks message or mempool message.
  *
  * The receiving peer can compare the inventories from an inv message against
  * the inventories it has already seen, and then use a follow-up message
  * to request unseen objects.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#inv
  *
  * [Protocol]
  *  02 ................................. Count: 2
  *
  *  01000000 ........................... Type: MSG_TX
  *  de55ffd709ac1f5dc509a0925d0b1fc4
  *  42ca034f224732e429081da1b621f55a ... Hash (TXID)
  *
  *  01000000 ........................... Type: MSG_TX
  *  91d36d997037e08018262978766f24b8
  *  a055aaf1d872e94ae85e9817b2c68dc7 ... Hash (TXID)
  */
object InvCodec extends ProtocolMessageCodec[Inv] {
  val command = "inv"
  val clazz = classOf[Inv]

  // TODO : Implement
  val codec : Codec[Inv] = null
}


/**
  * [Description]
  * The getdata message requests one or more data objects from another node.
  * The objects are requested by an inventory, which the requesting node typically
  * previously received by way of an inv message.
  *
  * The response to a getdata message can be a tx message, block message,
  * merkleblock message, or notfound message.
  *
  * This message cannot be used to request arbitrary data,
  * such as historic transactions no longer in the memory pool or relay set.
  * Full nodes may not even be able to provide older blocks
  * if they’ve pruned old transactions from their block database.
  * For this reason, the getdata message should usually only be used
  * to request data from a node which previously advertised
  * it had that data by sending an inv message.
  *
  * [Reference]
  *  https://bitcoin.org/en/developer-reference#getdata
  *
  * [Protocol]
  * ( Identical to Inv message )
  *  02 ................................. Count: 2
  *
  *  01000000 ........................... Type: MSG_TX
  *  de55ffd709ac1f5dc509a0925d0b1fc4
  *  42ca034f224732e429081da1b621f55a ... Hash (TXID)
  *
  *  01000000 ........................... Type: MSG_TX
  *  91d36d997037e08018262978766f24b8
  *  a055aaf1d872e94ae85e9817b2c68dc7 ... Hash (TXID)
  */
object GetDataCodec extends ProtocolMessageCodec[GetData] {
  val command = "getdata"
  val clazz = classOf[GetData]

  // TODO : Implement
  val codec : Codec[GetData] = null
}

/**
  * [Description]
  * The notfound message is a reply to a getdata message
  * which requested an object the receiving node does not have available for relay.
  *
  * (Nodes are not expected to relay historic transactions which are no longer in the memory pool or relay set.
  *  Nodes may also have pruned spent transactions from older blocks,
  *  making them unable to send those blocks.)
  *
  * [Since]
  * Added in protocol version 70001.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#notfound
  *
  * [Protocol]
  * ( Identical to Inv message )
  *  02 ................................. Count: 2
  *
  *  01000000 ........................... Type: MSG_TX
  *  de55ffd709ac1f5dc509a0925d0b1fc4
  *  42ca034f224732e429081da1b621f55a ... Hash (TXID)
  *
  *  01000000 ........................... Type: MSG_TX
  *  91d36d997037e08018262978766f24b8
  *  a055aaf1d872e94ae85e9817b2c68dc7 ... Hash (TXID)
  */
object NotFoundCodec extends ProtocolMessageCodec[NotFound] {
  val command = "notfound"
  val clazz = classOf[NotFound]

  // TODO : Implement
  val codec : Codec[NotFound] = null
}


/**
  * [Description]
  * The getblocks message requests an inv message that provides block header hashes
  * starting from a particular point in the block chain. It allows a peer
  * which has been disconnected or started for the first time to get the data it needs
  * to request the blocks it hasn’t seen.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#getblocks
  *
  * [Protocol]
  *  71110100 ........................... Protocol version: 70001
  *  02 ................................. Hash count: 2
  *
  *  d39f608a7775b537729884d4e6633bb2
  *  105e55a16a14d31b0000000000000000 ... Hash #1
  *
  *  5c3e6403d40837110a2e8afb602b1c01
  *  714bda7ce23bea0a0000000000000000 ... Hash #2
  *
  *  00000000000000000000000000000000
  *  00000000000000000000000000000000 ... Stop hash
  */
object GetBlocksCodec extends ProtocolMessageCodec[GetBlocks] {
  val command = "getblocks"
  val clazz = classOf[GetBlocks]

  // TODO : Implement
  val codec : Codec[GetBlocks] = null
}

/**
  * [Description]
  * The block message transmits a single serialized block in the format
  * described in the serialized blocks section.
  *
  * See that section for an example hexdump.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#block
  *
  * [Protocol]
  *  01000000 ........................... Block version: 1
  *  82bb869cf3a793432a66e826e05a6fc3
  *  7469f8efb7421dc88067010000000000 ... Hash of previous block's header
  *  7f16c5962e8bd963659c793ce370d95f
  *  093bc7e367117b3c30c1f8fdd0d97287 ... Merkle root
  *  76381b4d ........................... Time: 1293629558
  *  4c86041b ........................... nBits: 0x04864c * 256**(0x1b-3)
  *  554b8529 ........................... Nonce
  *
  *  01 ................................. Transaction Count
  *  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Raw transactions ( See TransactionCodec )
  */
object BlockCodec extends ProtocolMessageCodec[Block] {
  val command = "block"
  val clazz = classOf[Block]

  val codec : Codec[Block] = {
    ("blockcheader"          | BlockHeaderCodec.codec                 ) ::
    ("transactions"          | VarList.varList(TransactionCodec.codec))
  }.as[Block]
}

/**
  * [Description]
  * The getheaders message requests a headers message that provides block headers
  * starting from a particular point in the block chain.
  *
  * It allows a peer which has been disconnected or started for the first time
  * to get the headers it hasn’t seen yet.
  *
  * The getheaders message is nearly identical to the getblocks message,
  * with one minor difference: the inv reply to the getblocks message will include
  * no more than 500 block header hashes; the headers reply to the getheaders message will include
  * as many as 2,000 block headers.
  *
  * [Since]
  *  Added in protocol version 31800.
  *
  * [Reference]
  *  https://bitcoin.org/en/developer-reference#getheaders
  *  https://en.bitcoin.it/wiki/Protocol_documentation#getheaders
  *
  * [Protocol]
  *  71110100 ........................... Protocol version: 70001
  *  02 ................................. Hash count: 2
  *
  *  d39f608a7775b537729884d4e6633bb2
  *  105e55a16a14d31b0000000000000000 ... Hash #1
  *
  *  5c3e6403d40837110a2e8afb602b1c01
  *  714bda7ce23bea0a0000000000000000 ... Hash #2
  *
  *  00000000000000000000000000000000
  *  00000000000000000000000000000000 ... Stop hash
  */
object GetHeadersCodec extends ProtocolMessageCodec[GetHeaders] {
  val command = "getheaders"
  val clazz = classOf[GetHeaders]

  // TODO : Implement
  val codec : Codec[GetHeaders] = null
}

/**
  * [Description]
  * The tx message transmits a single transaction in the raw transaction format.
  * It can be sent in a variety of situations
  * 1. Transaction Response
  * 2. MerkleBlock Response
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#raw-transaction-format
  *
  * [Protocol]
  *  01000000 ................................... Version
  *
  *  01 ......................................... Number of inputs
  *  |
  *  | 7b1eabe0209b1fe794124575ef807057
  *  | c77ada2138ae4fa8d6c4de0398a14f3f ......... Outpoint TXID
  *  | 00000000 ................................. Outpoint index number
  *  |
  *  | 49 ....................................... Bytes in sig. script: 73
  *  | | 48 ..................................... Push 72 bytes as data
  *  | | | 30450221008949f0cb400094ad2b5eb3
  *  | | | 99d59d01c14d73d8fe6e96df1a7150de
  *  | | | b388ab8935022079656090d7f6bac4c9
  *  | | | a94e0aad311a4268e082a725f8aeae05
  *  | | | 73fb12ff866a5f01 ..................... Secp256k1 signature
  *  |
  *  | ffffffff ................................. Sequence number: UINT32_MAX
  *
  *  01 ......................................... Number of outputs
  *  | f0ca052a01000000 ......................... Satoshis (49.99990000 BTC)
  *  |
  *  | 19 ....................................... Bytes in pubkey script: 25
  *  | | 76 ..................................... OP_DUP
  *  | | a9 ..................................... OP_HASH160
  *  | | 14 ..................................... Push 20 bytes as data
  *  | | | cbc20a7664f2f69e5355aa427045bc15
  *  | | | e7c6c772 ............................. PubKey hash
  *  | | 88 ..................................... OP_EQUALVERIFY
  *  | | ac ..................................... OP_CHECKSIG
  *
  *  00000000 ................................... locktime: 0 (a block height)
  *
  */
object TransactionCodec extends ProtocolMessageCodec[Transaction] {
  val command = "tx"
  val clazz = classOf[Transaction]

  val codec : Codec[Transaction] = {
    ("version"             | int32L                                        ) ::
    ("input_transactions"  | VarList.varList(TransactionInputCodec.codec ) ) ::
    ("output_transactions" | VarList.varList(TransactionOutputCodec.codec) ) ::
    ("lock_time"           | uint32L                                       )
  }.as[Transaction]
}

/**
  * [Description]
  * The headers message sends one or more block headers to a node
  * which previously requested certain headers with a getheaders message.
  *
  * [Since]
  * Added in protocol version 31800.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#headers
  *
  * [Protocol]
  *
  *  01 ................................. Header count: 1
  *
  *  02000000 ........................... Block version: 2
  *  b6ff0b1b1680a2862a30ca44d346d9e8
  *  910d334beb48ca0c0000000000000000 ... Hash of previous block's header
  *  9d10aa52ee949386ca9385695f04ede2
  *  70dda20810decd12bc9b048aaab31471 ... Merkle root
  *  24d95a54 ........................... Unix time: 1415239972
  *  30c31b18 ........................... Target (bits)
  *  fe9f0864 ........................... Nonce
  *
  *  00 ................................. Transaction count (0x00)
  */
object HeadersCodec extends ProtocolMessageCodec[Headers] {
  val command = "headers"
  val clazz = classOf[Headers]

  // TODO : Implement
  val codec : Codec[Headers] = null
}

/**
  * [Description]
  * The getaddr message requests an addr message from the receiving node,
  * preferably one with lots of IP addresses of other receiving nodes.
  *
  * The transmitting node can use those IP addresses to quickly update
  * its database of available nodes rather than waiting for
  * unsolicited addr messages to arrive over time.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#getaddr
  *
  * [Protocol]
  *  No Payload.
  */
object GetAddrCodec extends ProtocolMessageCodec[GetAddr] {
  val command = "getaddr"
  val clazz = classOf[GetAddr]

  // TODO : Implement
  val codec : Codec[GetAddr] = null
}

/**
  * [Description]
  * The mempool message requests the TXIDs of transactions that the receiving node has verified as valid
  * but which have not yet appeared in a block. That is, transactions which are
  * in the receiving node’s memory pool. The response to the mempool message is
  * one or more inv messages containing the TXIDs in the usual inventory format.
  *
  * Sending the mempool message is mostly useful when a program first connects to the network.
  * Full nodes can use it to quickly gather most or all of the unconfirmed transactions
  * available on the network; this is especially useful for miners trying to gather transactions
  * for their transaction fees.
  *
  * SPV clients can set a filter before sending a mempool to only receive transactions
  * that match that filter; this allows a recently-started client to get most or
  * all unconfirmed transactions related to its wallet.
  *
  * The inv response to the mempool message is, at best, one node’s view of the network—
  * not a complete list of unconfirmed transactions on the network.
  *
  * [Since]
  * Added in protocol version 60002.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#mempool
  *
  * [Protocol]
  * No payload.
  */
object MempoolCodec extends ProtocolMessageCodec[Mempool] {
  val command = "mempool"
  val clazz = classOf[Mempool]

  // TODO : Implement
  val codec : Codec[Mempool] = null
}


/**
  * [Description]
  * The ping message helps confirm that the receiving peer is still connected.
  * If a TCP/IP error is encountered when sending the ping message (such as a connection timeout),
  * the transmitting node can assume that the receiving node is disconnected.
  *
  * The response to a ping message is the pong message.
  *
  * Before protocol version 60000, the ping message had no payload.
  * As of protocol version 60001 and all later versions, the message includes a single field, the nonce.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#ping
  *
  * [Protocol]
  *  0094102111e2af4d ... Nonce
  */
object PingCodec extends ProtocolMessageCodec[Ping] {
  val command = "ping"
  val clazz = classOf[Ping]

  val codec : Codec[Ping] = {
    ( "nonce" | UInt64Codec.codec )
  }.as[Ping]
}

/**
  * [Description]
  *
  * Added in protocol version 60001 as described by BIP31.
  *
  * The pong message replies to a ping message, proving to the pinging node
  * that the ponging node is still alive. Bitcoin Core will, by default,
  * disconnect from any clients which have not responded
  * to a ping message within 20 minutes.
  *
  * To allow nodes to keep track of latency, the pong message sends back
  * the same nonce received in the ping message it is replying to.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#pong
  *
  * [Protocol]
  *  0094102111e2af4d ... Nonce
  */
object PongCodec extends ProtocolMessageCodec[Pong] {
  val command = "pong"
  val clazz = classOf[Pong]

  val codec : Codec[Pong] = {
    ( "nonce" | UInt64Codec.codec )
  }.as[Pong]
}

/**
  * [Description]
  * Added in protocol version 70002 as described by BIP61.
  *
  * The reject message informs the receiving node that
  * one of its previous messages has been rejected.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#reject
  *
  * [Protocol]
  *  02 ................................. Number of bytes in message: 2
  *  7478 ............................... Type of message rejected: tx
  *  12 ................................. Reject code: 0x12 (duplicate)
  *  15 ................................. Number of bytes in reason: 21
  *  6261642d74786e732d696e707574732d
  *  7370656e74 ......................... Reason: bad-txns-inputs-spent
  *  394715fcab51093be7bfca5a31005972
  *  947baf86a31017939575fb2354222821 ... TXID
  */
object RejectCodec extends ProtocolMessageCodec[Reject] {
  val command = "reject"
  val clazz = classOf[Reject]

  // TODO : Implement
  val codec : Codec[Reject] = null
}

/**
  * [Description]
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filterload message tells the receiving peer to filter all relayed transactions
  * and requested merkle blocks through the provided filter.
  *
  * This allows clients to receive transactions relevant to their wallet
  * plus a configurable rate of false positive transactions
  * which can provide plausible-deniability privacy.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#filterload
  *
  * [Protocol]
  *  02 ......... Filter bytes: 2
  *  b50f ....... Filter: 1010 1101 1111 0000
  *  0b000000 ... nHashFuncs: 11
  *  00000000 ... nTweak: 0/none
  *  00 ......... nFlags: BLOOM_UPDATE_NONE
  */
object FilterLoadCodec extends ProtocolMessageCodec[FilterLoad] {
  val command = "filterload"
  val clazz = classOf[FilterLoad]

  // TODO : Implement
  val codec : Codec[FilterLoad] = null
}

/**
  * [Description]
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filteradd message tells the receiving peer to add a single element
  * to a previously-set bloom filter, such as a new public key.
  *
  * The element is sent directly to the receiving peer;
  * the peer then uses the parameters set in the filterload message
  * to add the element to the bloom filter.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#filteradd
  *
  * [Protocol]
  *  20 ................................. Element bytes: 32
  *  fdacf9b3eb077412e7a968d2e4f11b9a
  *  9dee312d666187ed77ee7d26af16cb0b ... Element (A TXID)
  *
  */
object FilterAddCodec extends ProtocolMessageCodec[FilterAdd] {
  val command = "filteradd"
  val clazz = classOf[FilterAdd]

  // TODO : Implement
  val codec : Codec[FilterAdd] = null
}

/**
  * [Description]
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filterclear message tells the receiving peer to remove a previously-set bloom filter.
  * This also undoes the effect of setting the relay field in the version message to 0,
  * allowing unfiltered access to inv messages announcing new transactions.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#filterclear
  *
  * [Protocol]
  * No Payload.
  */
object FilterClearCodec extends ProtocolMessageCodec[FilterClear] {
  val command = "filterclear"
  val clazz = classOf[FilterClear]

  // TODO : Implement
  val codec : Codec[FilterClear] = null
}

/**
  * [Description]
  * The merkleblock message is a reply to a getdata message
  * which requested a block using the inventory type MSG_MERKLEBLOCK.
  *
  * It is only part of the reply: if any matching transactions are found,
  * they will be sent separately as tx messages.
  *
  * If a filter has been previously set with the filterload message,
  * the merkleblock message will contain the TXIDs of any transactions
  * in the requested block that matched the filter,
  * as well as any parts of the block’s merkle tree
  * necessary to connect those transactions to the block header’s merkle root.
  *
  * The message also contains a complete copy of the block header
  * to allow the client to hash it and confirm its proof of work.
  *
  * [Since]
  * Added in protocol version 70001 as described by BIP37.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#merkleblock
  *
  * [Protocol]
  *  01000000 ........................... Block version: 1
  *  82bb869cf3a793432a66e826e05a6fc3
  *  7469f8efb7421dc88067010000000000 ... Hash of previous block's header
  *  7f16c5962e8bd963659c793ce370d95f
  *  093bc7e367117b3c30c1f8fdd0d97287 ... Merkle root
  *  76381b4d ........................... Time: 1293629558
  *  4c86041b ........................... nBits: 0x04864c * 256**(0x1b-3)
  *  554b8529 ........................... Nonce
  *
  *  07000000 ........................... Transaction count: 7
  *  04 ................................. Hash count: 4
  *
  *  3612262624047ee87660be1a707519a4
  *  43b1c1ce3d248cbfc6c15870f6c5daa2 ... Hash #1
  *  019f5b01d4195ecbc9398fbf3c3b1fa9
  *  bb3183301d7a1fb3bd174fcfa40a2b65 ... Hash #2
  *  41ed70551dd7e841883ab8f0b16bf041
  *  76b7d1480e4f0af9f3d4c3595768d068 ... Hash #3
  *  20d2a7bc994987302e5b1ac80fc425fe
  *  25f8b63169ea78e68fbaaefa59379bbf ... Hash #4
  *
  *  01 ................................. Flag bytes: 1
  *  1d ................................. Flags: 1 0 1 1 1 0 0 0
  *
  */
object MerkleBlockCodec extends ProtocolMessageCodec[MerkleBlock] {
  val command = "merkleblock"
  val clazz = classOf[MerkleBlock]

  // TODO : Implement
  val codec : Codec[MerkleBlock] = null
}

/**
  * [Description]
  * Added in protocol version 311.
  *
  * The alert message warns nodes of problems that may affect them or the rest of the network.
  * Each alert message is signed using a key controlled by respected community members,
  * mostly Bitcoin Core developers.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#alert
  *
  * [Protocol]
  *
  *  73 ................................. Bytes in encapsulated alert: 115
  *  01000000 ........................... Version: 1
  *  3766404f00000000 ................... RelayUntil: 1329620535
  *  b305434f00000000 ................... Expiration: 1330917376
  *
  *  f2030000 ........................... ID: 1010
  *  f1030000 ........................... Cancel: 1009
  *  00 ................................. setCancel count: 0
  *
  *  10270000 ........................... MinVer: 10000
  *  48ee0000 ........................... MaxVer: 61000
  *  00 ................................. setUser_agent bytes: 0
  *  64000000 ........................... Priority: 100
  *
  *  00 ................................. Bytes In Comment String: 0
  *  46 ................................. Bytes in StatusBar String: 70
  *  53656520626974636f696e2e6f72672f
  *  666562323020696620796f7520686176
  *  652074726f75626c6520636f6e6e6563
  *  74696e67206166746572203230204665
  *  627275617279 ....................... Status Bar String: "See [...]"
  *  00 ................................. Bytes In Reserved String: 0
  *
  *  47 ................................. Bytes in signature: 71
  *  30450221008389df45f0703f39ec8c1c
  *  c42c13810ffcae14995bb648340219e3
  *  53b63b53eb022009ec65e1c1aaeec1fd
  *  334c6b684bde2b3f573060d5b70c3a46
  *  723326e4e8a4f1 ..................... Signature
  *
  */
object AlertCodec extends ProtocolMessageCodec[Alert] {
  val command = "alert"
  val clazz = classOf[Alert]

  // TODO : Implement
  val codec : Codec[Alert] = null
}

/**
  * [Description]
  * The sendheaders message tells the receiving peer to send new block announcements
  * using a headers message rather than an inv message.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#sendheaders
  *
  * [Protocol]
  *  No payload.
  */
object SendHeadersCodec extends ProtocolMessageCodec[SendHeaders] {
  val command = "sendheaders"
  val clazz = classOf[SendHeaders]

  // TODO : Implement
  val codec : Codec[SendHeaders] = null
}

