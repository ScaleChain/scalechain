package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*
import io.scalechain.blockchain.*

internal val HashListCodec = Codecs.variableList( HashCodec )

/**
  * <Description>
  * The version message provides information about the transmitting node
  * to the receiving node at the beginning of a connection.
  *
  * Until both peers have exchanged version messages, no other messages will be accepted.
  *
  * If a version message is accepted, the receiving node should send a verack message—
  * but no node should send a verack message before initializing its half of the connection
  * by first sending a version message.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#version
  *
  * <Protocol>
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
object VersionCodec : ProtocolMessageCodec<Version> {
  override val command = "version"
  override val clazz = Version::class.java

  override fun transcode(io : CodecInputOutputStream, obj : Version? ) : Version? {
    val version       = Codecs.Int32L.transcode(io, obj?.version)
    val services      = Codecs.UInt64L.transcode(io, obj?.services)
    val timestamp     = Codecs.Int64L.transcode(io, obj?.timestamp)
    val destAddress   = NetworkAddressCodec.transcode(io, obj?.destAddress)
    val sourceAddress = NetworkAddressCodec.transcode(io, obj?.sourceAddress)
    val nonce         = Codecs.UInt64L.transcode(io, obj?.nonce)
    val userAgent     = Codecs.VariableString.transcode(io, obj?.userAgent)
    val startHeight   = Codecs.Int32L.transcode(io, obj?.startHeight)
    val relay         = Codecs.Boolean.transcode(io, obj?.relay)

    if (io.isInput) {
      return Version(
          version!!,
          services!!,
          timestamp!!,
          destAddress!!,
          sourceAddress!!,
          nonce!!,
          userAgent!!,
          startHeight!!,
          relay!!
      )
    }
    return null
  }
}


/**
  * <Description>
  * The verack message acknowledges a previously-received version message,
  * informing the connecting node that it can begin to send other messages.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#verack
  *
  * <Protocol>
  *  No payload.
  */
object VerackCodec : ProtocolMessageCodec<Verack> {
  override val command = "verack"
  override val clazz = Verack::class.java

  private val codec = Codecs.provide(Verack())
  override fun transcode(io : CodecInputOutputStream, obj : Verack? ) : Verack? {
    return codec.transcode(io, obj)
  }
}


/**
  * <Description>
  * The addr (IP address) message relays connection information for peers on the network.
  * Each peer which wants to accept incoming connections creates an addr message
  * providing its connection information and then sends that message to its peers unsolicited.
  *
  * Some of its peers send that information to their peers (also unsolicited),
  * some of which further distribute it, allowing decentralized peer discovery
  * for any program already on the network.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#addr
  *
  * <Protocol>
  *  fde803 ............................. Address count: 1000
  *
  *  d91f4854 ........................... Epoch time: 1414012889
  *  0100000000000000 ................... Service bits: 01 (network node)
  *  00000000000000000000ffffc0000233 ... IP Address: ::ffff:192.0.2.51
  *  208d ............................... Port: 8333
  *
  *  <...> .............................. (999 more addresses omitted)
  */
object AddrCodec : ProtocolMessageCodec<Addr> {
  override val command = "addr"
  override val clazz = Addr::class.java

  private val networkAddressWithTimestampListCodec = Codecs.variableList(
      NetworkAddressWithTimestampCodec
  )

  override fun transcode(io : CodecInputOutputStream, obj : Addr? ) : Addr? {
    val addresses = networkAddressWithTimestampListCodec.transcode(io, obj?.addresses)

    if (io.isInput) {
      return Addr(
        addresses!!
      )
    }
    return null
  }
}

internal val InvTypeCodec : Codec<InvType> =
  Codecs.mappedEnum(Codecs.UInt32L,
    mapOf(
      InvType.ERROR to 0L,
      InvType.MSG_TX to 1L,
      InvType.MSG_BLOCK to 2L,
      InvType.MSG_FILTERED_BLOCK to 3L
    )
  )

internal object InvVectorCodec : Codec<InvVector> {
  override fun transcode(io : CodecInputOutputStream, obj : InvVector? ) : InvVector? {
    val invType = InvTypeCodec.transcode(io, obj?.invType)
    val hash  = HashCodec.transcode(io, obj?.hash)

    if (io.isInput) {
      return InvVector(
        invType!!,
        hash!!
      )
    }
    return null
  }
}

internal val InvVectorListCodec : Codec<List<InvVector>> =
    Codecs.variableList( InvVectorCodec )

/**
  * <Description>
  * The inv message (inventory message) transmits one or more inventories of objects
  * known to the transmitting peer. It can be sent unsolicited to announce transactions or blocks,
  * or it can be sent in reply to a getblocks message or mempool message.
  *
  * The receiving peer can compare the inventories from an inv message against
  * the inventories it has already seen, and then use a follow-up message
  * to request unseen objects.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#inv
  *
  * <Protocol>
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
object InvCodec : ProtocolMessageCodec<Inv> {
  override val command = "inv"
  override val clazz = Inv::class.java

  override fun transcode(io : CodecInputOutputStream, obj : Inv? ) : Inv? {
    val inventories = InvVectorListCodec.transcode(io, obj?.inventories)

    if (io.isInput) {
      return Inv(
        inventories!!
      )
    }
    return null
  }
}

/**
  * <Description>
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
  * <Reference>
  *  https://bitcoin.org/en/developer-reference#getdata
  *
  * <Protocol>
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
object GetDataCodec : ProtocolMessageCodec<GetData> {
  override val command = "getdata"
  override val clazz = GetData::class.java

  override fun transcode(io : CodecInputOutputStream, obj : GetData? ) : GetData? {
    val inventories = InvVectorListCodec.transcode(io, obj?.inventories)

    if (io.isInput) {
      return GetData(
          inventories!!
      )
    }
    return null
  }
/*
  val codec : Codec<GetData> {
    ( "inventories" | VarList.varList(InvCodec.invVectorCodec) )
  }.as<GetData>
*/
}

/**
  * <Description>
  * The notfound message is a reply to a getdata message
  * which requested an object the receiving node does not have available for relay.
  *
  * (Nodes are not expected to relay historic transactions which are no longer in the memory pool or relay set.
  *  Nodes may also have pruned spent transactions from older blocks,
  *  making them unable to send those blocks.)
  *
  * <Since>
  * Added in protocol version 70001.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#notfound
  *
  * <Protocol>
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
object NotFoundCodec : ProtocolMessageCodec<NotFound> {
  override val command = "notfound"
  override val clazz = NotFound::class.java

  override fun transcode(io : CodecInputOutputStream, obj : NotFound? ) : NotFound? {
    val inventories = InvVectorListCodec.transcode(io, obj?.inventories)

    if (io.isInput) {
      return NotFound(
          inventories!!
      )
    }
    return null
  }

/*
  val codec : Codec<NotFound> {
    ( "inventories" | VarList.varList(InvCodec.invVectorCodec) )
  }.as<NotFound>
*/
}


/**
  * <Description>
  * The getblocks message requests an inv message that provides block header hashes
  * starting from a particular point in the block chain. It allows a peer
  * which has been disconnected or started for the first time to get the data it needs
  * to request the blocks it hasn’t seen.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#getblocks
  *
  * <Protocol>
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
object GetBlocksCodec : ProtocolMessageCodec<GetBlocks> {
  override val command = "getblocks"
  override val clazz = GetBlocks::class.java

  override fun transcode(io : CodecInputOutputStream, obj : GetBlocks? ) : GetBlocks? {
    val version            = Codecs.UInt32L.transcode(io, obj?.version)
    val blockLocatorHashes = HashListCodec.transcode(io, obj?.blockLocatorHashes)
    val hashStop           = HashCodec.transcode(io, obj?.hashStop)

    if (io.isInput) {
      return GetBlocks(
        version!!,
        blockLocatorHashes!!,
        hashStop!!
      )
    }
    return null
  }
}

/**
  * <Description>
  * The block message transmits a single serialized block in the format
  * described in the serialized blocks section.
  *
  * See that section for an example hexdump.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#block
  *
  * <Protocol>
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
object BlockCodec : ProtocolMessageCodec<Block> {
  override val command = "block"
  override val clazz = Block::class.java

  private val TransactionListCodec = Codecs.variableList( TransactionCodec )
  override fun transcode(io : CodecInputOutputStream, obj : Block? ) : Block? {
    val header = BlockHeaderCodec.transcode(io, obj?.header)
    val transactions = TransactionListCodec.transcode(io, obj?.transactions)

    if (io.isInput) {
      return Block(
        header!!,
        transactions!!
      )
    }
    return null
  }
}

/**
  * <Description>
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
  * <Since>
  *  Added in protocol version 31800.
  *
  * <Reference>
  *  https://bitcoin.org/en/developer-reference#getheaders
  *  https://en.bitcoin.it/wiki/Protocol_documentation#getheaders
  *
  * <Protocol>
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
object GetHeadersCodec : ProtocolMessageCodec<GetHeaders> {
  override val command = "getheaders"
  override val clazz = GetHeaders::class.java

  override fun transcode(io : CodecInputOutputStream, obj : GetHeaders? ) : GetHeaders? {
    val version            = Codecs.UInt32L.transcode(io, obj?.version)
    val blockLocatorHashes = HashListCodec.transcode(io, obj?.blockLocatorHashes)
    val hashStop           = HashCodec.transcode(io, obj?.hashStop)

    if (io.isInput) {
      return GetHeaders(
        version!!,
        blockLocatorHashes!!,
        hashStop!!
      )
    }
    return null
  }
}



/**
  * <Description>
  * The tx message transmits a single transaction in the raw transaction format.
  * It can be sent in a variety of situations
  * 1. Transaction Response
  * 2. MerkleBlock Response
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#raw-transaction-format
  *
  * <Protocol>
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
object TransactionCodec : ProtocolMessageCodec<Transaction> {
  override val command = "tx"
  override val clazz = Transaction::class.java

  private val TransactionInputListCodec = Codecs.variableList( TransactionInputCodec )
  private val TransactionOutputListCodec = Codecs.variableList( TransactionOutputCodec )

  override fun transcode(io : CodecInputOutputStream, obj : Transaction? ) : Transaction? {
    val version  = Codecs.Int32L.transcode(io, obj?.version)
    val inputs   = TransactionInputListCodec.transcode(io, obj?.inputs)
    val outputs  = TransactionOutputListCodec.transcode(io, obj?.outputs)
    val lockTime = Codecs.UInt32L.transcode(io, obj?.lockTime)

    if (io.isInput) {
      return Transaction(
        version!!,
        inputs!!,
        outputs!!,
        lockTime!!
      )
    }
    return null
  }
}

internal val BlockHeaderListCodec : Codec<List<BlockHeader>> =
  Codecs.variableList(BlockHeaderCodec)
/**
  * <Description>
  * The headers message sends one or more block headers to a node
  * which previously requested certain headers with a getheaders message.
  *
  * <Since>
  * Added in protocol version 31800.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#headers
  *
  * <Protocol>
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
object HeadersCodec : ProtocolMessageCodec<Headers> {
  override val command = "headers"
  override val clazz = Headers::class.java

  override fun transcode(io : CodecInputOutputStream, obj : Headers? ) : Headers? {
    val headers = BlockHeaderListCodec.transcode(io, obj?.headers)

    if (io.isInput) {
      return Headers(
          headers!!
      )
    }
    return null
  }

/*
  val ZERO = ByteArray(0)

  data class BlockHeaderWithDummy (
     header : BlockHeader,
     dummy : Unit )

  private val BlockHeaderWithDummyCodec : Codec<BlockHeaderWithDummy> {
    ("block_header" | BlockHeaderCodec.codec) ::
    ("transaction_count" | constant(ByteVector.view(ZERO)))
  }.as<BlockHeaderWithDummy>

  val blockHeaderCoodec : Codec<BlockHeader> = BlockHeaderWithDummyCodec.xmap(
    _.header, BlockHeaderWithDummy(_:BlockHeader, ())
  )

  val codec : Codec<Headers> {
    ("headers" | VarList.varList(blockHeaderCoodec))
  }.as<Headers>
*/
}

/**
  * <Description>
  * The getaddr message requests an addr message from the receiving node,
  * preferably one with lots of IP addresses of other receiving nodes.
  *
  * The transmitting node can use those IP addresses to quickly update
  * its database of available nodes rather than waiting for
  * unsolicited addr messages to arrive over time.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#getaddr
  *
  * <Protocol>
  *  No Payload.
  */
object GetAddrCodec : ProtocolMessageCodec<GetAddr> {
  override val command = "getaddr"
  override val clazz = GetAddr::class.java

  private val codec = Codecs.provide(GetAddr())
  override fun transcode(io : CodecInputOutputStream, obj : GetAddr? ) : GetAddr? {
    return codec.transcode(io, obj)
  }
}

/**
  * <Description>
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
  * <Since>
  * Added in protocol version 60002.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#mempool
  *
  * <Protocol>
  * No payload.
  */
object MempoolCodec : ProtocolMessageCodec<Mempool> {
  override val command = "mempool"
  override val clazz = Mempool::class.java

  private val codec = Codecs.provide(Mempool())
  override fun transcode(io : CodecInputOutputStream, obj : Mempool? ) : Mempool? {
    return codec.transcode(io, obj)
  }
}


/**
  * <Description>
  * The ping message helps confirm that the receiving peer is still connected.
  * If a TCP/IP error is encountered when sending the ping message (such as a connection timeout),
  * the transmitting node can assume that the receiving node is disconnected.
  *
  * The response to a ping message is the pong message.
  *
  * Before protocol version 60000, the ping message had no payload.
  * As of protocol version 60001 and all later versions, the message includes a single field, the nonce.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#ping
  *
  * <Protocol>
  *  0094102111e2af4d ... Nonce
  */
object PingCodec : ProtocolMessageCodec<Ping> {
  override val command = "ping"
  override val clazz = Ping::class.java

  override fun transcode(io : CodecInputOutputStream, obj : Ping? ) : Ping? {
    val nonce = Codecs.UInt64L.transcode(io, obj?.nonce)

    if (io.isInput) {
      return Ping(
        nonce!!
      )
    }
    return null
  }
}

/**
  * <Description>
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
  * <Reference>
  * https://bitcoin.org/en/developer-reference#pong
  *
  * <Protocol>
  *  0094102111e2af4d ... Nonce
  */
object PongCodec : ProtocolMessageCodec<Pong> {
  override val command = "pong"
  override val clazz = Pong::class.java

  override fun transcode(io : CodecInputOutputStream, obj : Pong? ) : Pong? {
    val nonce = Codecs.UInt64L.transcode(io, obj?.nonce)

    if (io.isInput) {
      return Pong(
        nonce!!
      )
    }
    return null
  }
}

internal val RejectTypeCodec : Codec<RejectType> =
  Codecs.mappedEnum(Codecs.Byte, // BUGBUG : Codec type changed UInt8 to Byte
    mapOf(
      RejectType.REJECT_MALFORMED to 0x01.toByte(),
      RejectType.REJECT_INVALID to 0x10.toByte(),
      RejectType.REJECT_OBSOLETE to 0x11.toByte(),
      RejectType.REJECT_DUPLICATE to 0x12.toByte(),
      RejectType.REJECT_NONSTANDARD to 0x40.toByte(),
      RejectType.REJECT_DUST to 0x41.toByte(),
      RejectType.REJECT_INSUFFICIENTFEE to 0x42.toByte(),
      RejectType.REJECT_CHECKPOINT to 0x43.toByte()
    )
  )

/**
  * <Description>
  * Added in protocol version 70002 as described by BIP61.
  *
  * The reject message informs the receiving node that
  * one of its previous messages has been rejected.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#reject
  *
  * <Protocol>
  *  02 ................................. Number of bytes in message: 2
  *  7478 ............................... Type of message rejected: tx
  *  12 ................................. Reject code: 0x12 (duplicate)
  *  15 ................................. Number of bytes in reason: 21
  *  6261642d74786e732d696e707574732d
  *  7370656e74 ......................... Reason: bad-txns-inputs-spent
  *  394715fcab51093be7bfca5a31005972
  *  947baf86a31017939575fb2354222821 ... TXID
  */
object RejectCodec : ProtocolMessageCodec<Reject> {
  override val command = "reject"
  override val clazz = Reject::class.java

  private val DataCodec = Codecs.fixedByteBuf(32)

  override fun transcode(io : CodecInputOutputStream, obj : Reject? ) : Reject? {
    val message = Codecs.VariableString.transcode(io, obj?.message)
    val rejectType = RejectTypeCodec.transcode(io, obj?.rejectType)
    val reason = Codecs.VariableString.transcode(io, obj?.reason)
    val data = DataCodec.transcode(io, obj?.data)

    if (io.isInput) {
      return Reject(
        message!!,
        rejectType!!,
        reason!!,
        data!!
      )
    }
    return null
  }
}


/**
  * <Description>
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filterload message tells the receiving peer to filter all relayed transactions
  * and requested merkle blocks through the provided filter.
  *
  * This allows clients to receive transactions relevant to their wallet
  * plus a configurable rate of false positive transactions
  * which can provide plausible-deniability privacy.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#filterload
  *
  * <Protocol>
  *  02 ......... Filter bytes: 2
  *  b50f ....... Filter: 1010 1101 1111 0000
  *  0b000000 ... nHashFuncs: 11
  *  00000000 ... nTweak: 0/none
  *  00 ......... nFlags: BLOOM_UPDATE_NONE
  */
object FilterLoadCodec : ProtocolMessageCodec<FilterLoad> {
  override val command = "filterload"
  override val clazz = FilterLoad::class.java

  // TODO : Implement
  override fun transcode(io : CodecInputOutputStream, obj : FilterLoad? ) : FilterLoad? {
    throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
  }
}


/**
  * <Description>
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filteradd message tells the receiving peer to add a single element
  * to a previously-set bloom filter, such as a public key.
  *
  * The element is sent directly to the receiving peer;
  * the peer then uses the parameters set in the filterload message
  * to add the element to the bloom filter.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#filteradd
  *
  * <Protocol>
  *  20 ................................. Element bytes: 32
  *  fdacf9b3eb077412e7a968d2e4f11b9a
  *  9dee312d666187ed77ee7d26af16cb0b ... Element (A TXID)
  *
  */
object FilterAddCodec : ProtocolMessageCodec<FilterAdd> {
  override val command = "filteradd"
  override val clazz = FilterAdd::class.java

  // TODO : Implement
  override fun transcode(io : CodecInputOutputStream, obj : FilterAdd? ) : FilterAdd? {
    throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
  }
}

/**
  * <Description>
  * Added in protocol version 70001 as described by BIP37.
  *
  * The filterclear message tells the receiving peer to remove a previously-set bloom filter.
  * This also undoes the effect of setting the relay field in the version message to 0,
  * allowing unfiltered access to inv messages announcing transactions.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#filterclear
  *
  * <Protocol>
  * No Payload.
  */
object FilterClearCodec : ProtocolMessageCodec<FilterClear> {
  override val command = "filterclear"
  override val clazz = FilterClear::class.java

  private val Codec = Codecs.provide(FilterClear())
  override fun transcode(io : CodecInputOutputStream, obj : FilterClear? ) : FilterClear? {
    return Codec.transcode(io, obj)
  }
}

/**
  * <Description>
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
  * <Since>
  * Added in protocol version 70001 as described by BIP37.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#merkleblock
  *
  * <Protocol>
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
object MerkleBlockCodec : ProtocolMessageCodec<MerkleBlock> {
  override val command = "merkleblock"
  override val clazz = MerkleBlock::class.java

  // TODO : Implement
  override fun transcode(io : CodecInputOutputStream, obj : MerkleBlock? ) : MerkleBlock? {
    throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
  }
}

/**
  * <Description>
  * Added in protocol version 311.
  *
  * The alert message warns nodes of problems that may affect them or the rest of the network.
  * Each alert message is signed using a key controlled by respected community members,
  * mostly Bitcoin Core developers.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#alert
  *
  * <Protocol>
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
  *  627275617279 ....................... Status Bar String: "See <...>"
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
object AlertCodec : ProtocolMessageCodec<Alert> {
  override val command = "alert"
  override val clazz = Alert::class.java

  // TODO : Implement
  override fun transcode(io : CodecInputOutputStream, obj : Alert? ) : Alert? {
    throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
  }
}

/**
  * <Description>
  * The sendheaders message tells the receiving peer to send block announcements
  * using a headers message rather than an inv message.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#sendheaders
  *
  * <Protocol>
  *  No payload.
  */
object SendHeadersCodec : ProtocolMessageCodec<SendHeaders> {
  override val command = "sendheaders"
  override val clazz = SendHeaders::class.java

  private val Codec = Codecs.provide(SendHeaders())
  override fun transcode(io : CodecInputOutputStream, obj : SendHeaders? ) : SendHeaders? {
    return Codec.transcode(io, obj)
  }
}

