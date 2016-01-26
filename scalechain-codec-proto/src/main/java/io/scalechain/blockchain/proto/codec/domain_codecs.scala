package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.io.InputOutputStream

object BlockCodec extends ProtocolMessageCodec[Block] {
  val command = "block"
  val prototype = Block(size=0L, header=null, transactions=null)
  def processImpl(stream : InputOutputStream, message : Block) : Block = {
    //message.copy( x =  )
    assert(false);
    null
  }
}
/*

object BlockHeaderCodec {

}

object TransactionsCodec {

}

object TransactionCodec {

}

object TransactionInputsCodec {

}

object TransactionOutputsCodec {

}

object TransactionInputCodec {

}

object GenerationTransactionInputCodec {

}

object NormalTransactionInputCodec {

}

object TransactionOutputCodec {

}

object LockingScriptCodec {

}

object UnlockingScriptCodec {

}
*/
