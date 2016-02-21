package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Actor, Props}
import io.scalechain.blockchain.proto.{Hash, GetData, InvVector, Transaction}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.TransientTransactionStorage

object TransactionProcessor {
  def props(peerBroker : ActorRef) = Props[TransactionProcessor](new TransactionProcessor(peerBroker))
}

/**
  *
  */
class TransactionProcessor(peerBroker : ActorRef) extends Actor {
  val transactionStorage = new TransientTransactionStorage()
  def receive : Receive = {
    case transaction : Transaction => {
      println("TransactionProcessor received Transaction")

      // TOOD : Optimize : transactionHash is calculated twice. (1) in HashCalculator (2) in storeTransaction.
      val hash = Hash(HashCalculator.transactionHash(transaction))
      if (! transactionStorage.hasTransaction(hash)) {
        transactionStorage.storeTransaction(transaction)
      }
    }

    // BUGBUG : Change to case class
    case (from:InetSocketAddress, inventories : List[InvVector]) => {
      //assert(inventory.invType == InvType.MSG_TRANSACTION)
      println("TransactionProcessor received InvVector(MSG_TRANSACTION)")

      val newTransactionInventories = inventories.filter { inventory =>
        ! transactionStorage.hasTransaction(inventory.hash)
      }

      if (! newTransactionInventories.isEmpty) {
        peerBroker ! (null, from, Some(GetData(newTransactionInventories)) )
      }

    }

  }
}
