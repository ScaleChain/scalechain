package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props, Actor}
import io.scalechain.blockchain.net.processor.{TransactionProcessor, PeerClientProcessor, BlockProcessor}
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

object DomainMessageRouter {
  def props(peerBroker : ActorRef ) = Props[DomainMessageRouter]( new DomainMessageRouter(peerBroker))
  case class InventoriesFrom(address: InetSocketAddress, inventories:List[InvVector])
  case class VersionFrom(address: InetSocketAddress, version: Version)
}

/**
  * Created by kangmo on 2/14/16.
  */
class DomainMessageRouter(peerBroker : ActorRef ) extends Actor {

  val blockProcessor = BlockProcessor.create( context.system, peerBroker)

  val peerClientProcessor = context.system.actorOf(PeerClientProcessor.props)

  val transactionProcessor = TransactionProcessor.create(context.system, peerBroker)

  import DomainMessageRouter._

  def receive() : Receive = {
    case versionFrom : VersionFrom => {
      peerBroker forward versionFrom
    }
    case verack : Verack => {
      blockProcessor ! BlockProcessor.StartInitialBlockDownload()
      println("Sent StartInitialBlockDownload to the block processor." )
    }
    case message : Addr => {
      println("[DomainMessageRouter] received address : " + message)
      peerClientProcessor forward message
    }
    case message @ InventoriesFrom(address, inventories) => {
      val blockInventories = inventories.filter( _.invType == InvType.MSG_BLOCK)
      if (!blockInventories.isEmpty) {
        blockProcessor forward InventoriesFrom(address, blockInventories)
        println("[DomainMessageRouter] forwarded block inventories.")
      }

      val transactionInventories = inventories.filter( _.invType == InvType.MSG_TX)
      if (!transactionInventories.isEmpty) {
        transactionProcessor forward InventoriesFrom(address, transactionInventories)
        //println("[DomainMessageRouter] forwarded transaction inventories.")
      }

      // Inventory vectors whose InvType(s) are InvType.ERROR and InvType.MSG_FILTERED_BLOCK are ignored.
    }
    case message : Headers => {
      println("[DomainMessageRouter] forwarded block headers. ")
      blockProcessor forward message
    }
    case message : Transaction => {
      //println("[DomainMessageRouter] forwarded transaction. ")
      transactionProcessor forward message
    }
    case message : Block => {
      println("[DomainMessageRouter] forwarded block. ")
      blockProcessor forward message
    }
  }
}


