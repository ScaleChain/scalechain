package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{LockingScript, UnlockingScript, Transaction}

case class MergedScript(
             transaction:Transaction,
             inputIndex:Int,
             unlockingScript:UnlockingScript,
             lockingScript:LockingScript) {
  override def toString = s"MergedScript(transaction=$transaction, inputIndex=$inputIndex, unlockingScript=$unlockingScript, lockingScript=$lockingScript)"
}
