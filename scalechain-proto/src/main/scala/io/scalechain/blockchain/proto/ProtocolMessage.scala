package io.scalechain.blockchain.proto

/** The super class for all protocol messages.
  * The interface of protocol encoder and decoder will use ProtocolMessage type.
  * (Protocol encoder and decoder serialize and deserialize the message.)
  *
  * See also : BitcoinProtocolEncoder, BitcoinProtocolDecoder.
  */
abstract class ProtocolMessage