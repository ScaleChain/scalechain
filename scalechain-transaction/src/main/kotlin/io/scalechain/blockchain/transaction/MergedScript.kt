package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.UnlockingScript
import io.scalechain.blockchain.proto.Transaction

data class MergedScript(
             val transaction:Transaction,
             val inputIndex:Int,
             val unlockingScript:UnlockingScript,
             val lockingScript:LockingScript) {
  override fun toString() = "MergedScript(transaction=$transaction, inputIndex=$inputIndex, unlockingScript=$unlockingScript, lockingScript=$lockingScript)"
}
