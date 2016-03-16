package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, ActorRef, Actor, Props}
import io.scalechain.blockchain.net.DomainMessageRouter.InventoriesFrom
import io.scalechain.blockchain.net.PeerBroker.SendToOne
import io.scalechain.blockchain.net.processor.TransactionProcessor.{GetTransactionResult, GetTransaction}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.TransientTransactionStorage

object TransactionProcessor {
  def props(peerBroker : ActorRef) = Props[TransactionProcessor](new TransactionProcessor(peerBroker))
  case class GetTransaction(txHash : Hash)
  case class GetTransactionResult(transactionOption : Option[Transaction])


  var theTxProcessor : ActorRef = null

  def create(system : ActorSystem, peerBroker : ActorRef) : ActorRef = {
    assert(theTxProcessor == null)
    theTxProcessor = system.actorOf(TransactionProcessor.props(peerBroker))
    theTxProcessor
  }

  /** Get the transaction processor actor. This actor is a singleton, used by API layer to access block database.
    *
    * @return The transaction processor actor.
    */
  def get() : ActorRef = {
    assert(theTxProcessor != null)
    theTxProcessor
  }
}

/**
  *
  */
class TransactionProcessor(peerBroker : ActorRef) extends Actor {
  val transactionStorage = new TransientTransactionStorage()
  def receive : Receive = {
    case GetTransaction(txHash) => {
      sender ! GetTransactionResult( transactionStorage.get(txHash) )
    }

    case transaction : Transaction => {
      //println("TransactionProcessor received Transaction")

      // TOOD : Optimize : transactionHash is calculated twice. (1) in HashCalculator (2) in storeTransaction.
      val hash = Hash(HashCalculator.transactionHash(transaction))
      if (! transactionStorage.exists(hash)) {

        // BUGBUG : We hit this issue : TransactionVerificationException(ErrorCode(script_eval_failure)
        // new TransactionVerifier(transaction).verify(DiskBlockStorage.get)

        //println("The transaction was stored : " + hash)
        transactionStorage.put(transaction)
      }
    }

    case InventoriesFrom(address, inventories) => {
      //assert(inventory.invType == InvType.MSG_TRANSACTION)
      //println(s"TransactionProcessor received InvVector(MSG_TRANSACTION). from : ${address}")

      val newTransactionInventories = inventories.filter { inventory =>
        // BUGBUG : We also need to check if the transaction exists on the DiskBlockStorage
        ! transactionStorage.exists(inventory.hash)
      }

      if (! newTransactionInventories.isEmpty) {
        peerBroker ! SendToOne( address, GetData(newTransactionInventories) )
        //println(s"Sent GetData(transaction inventories). to : ${address}")
      }
    }

  }
}
