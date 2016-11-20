package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{LockingScript, UnlockingScript, Transaction}

data class MergedScript(
             transaction:Transaction,
             inputIndex:Int,
             unlockingScript:UnlockingScript,
             lockingScript:LockingScript) {
  override fun toString = s"MergedScript(transaction=$transaction, inputIndex=$inputIndex, unlockingScript=$unlockingScript, lockingScript=$lockingScript)"
}
