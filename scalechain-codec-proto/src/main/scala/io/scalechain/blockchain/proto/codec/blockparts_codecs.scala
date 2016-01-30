package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.io.InputOutputStream
import scodec.Codec


trait MessagePartCodec[T] {
  val codec : Codec[T]
}

object BlockHashCodec extends MessagePartCodec[BlockHash]{
  // TODO : Implement
  val codec : Codec[BlockHash] = null
}

object CoinbaseDataCodec extends MessagePartCodec[CoinbaseData] {
  // TODO : Implement
  val codec : Codec[CoinbaseData] = null
}

object GenerationTransactionInputCodec extends MessagePartCodec[GenerationTransactionInput] {
  // TODO : Implement
  val codec : Codec[GenerationTransactionInput] = null
}

object LockingScriptCodec extends MessagePartCodec[LockingScript] {
  // TODO : Implement
  val codec : Codec[LockingScript] = null
}

object MerkleRootHashCodec extends MessagePartCodec[MerkleRootHash] {
  // TODO : Implement
  val codec : Codec[MerkleRootHash] = null
}

object NormalTransactionInputCodec extends MessagePartCodec[NormalTransactionInput] {
  // TODO : Implement
  val codec : Codec[NormalTransactionInput] = null
}

object TransactionHashCodec extends MessagePartCodec[TransactionHash] {
  // TODO : Implement
  val codec : Codec[TransactionHash] = null
}

object TransactionInputCodec extends MessagePartCodec[TransactionInput] {
  // TODO : Implement
  val codec : Codec[TransactionInput] = null
}

object TransactionOutputCodec extends MessagePartCodec[TransactionOutput] {
  // TODO : Implement
  val codec : Codec[TransactionOutput] = null
}

object UnlockingScriptCodec extends MessagePartCodec[UnlockingScript] {
  // TODO : Implement
  val codec : Codec[UnlockingScript] = null
}
