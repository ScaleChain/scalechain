package io.scalechain.blockchain.proto

// A transcodable class is both encodable and decodable
// * Interface name suggestion by b0c1
interface Transcodable

/** The super class for all protocol messages.
 * The interface of protocol encoder and decoder will use ProtocolMessage type.
 * (Protocol encoder and decoder serialize and deserialize the message.)
 *
 * See also : BitcoinProtocolEncoder, BitcoinProtocolDecoder.
 */
interface ProtocolMessage : Transcodable