package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props, Actor}
import io.scalechain.blockchain.net.processor.{TransactionProcessor, PeerClientProcessor, BlockProcessor}
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

object DomainMessageRouter {
  def props(peerBroker : ActorRef ) = Props[DomainMessageRouter]( new DomainMessageRouter(peerBroker))
}

/**
  * Created by kangmo on 2/14/16.
  */
class DomainMessageRouter(peerBroker : ActorRef ) extends Actor {

  val blockProcessor = context.system.actorOf(BlockProcessor.props(peerBroker))
  val peerClientProcessor = context.system.actorOf(PeerClientProcessor.props)
  val transactionProcessor = context.system.actorOf(TransactionProcessor.props(peerBroker))

  // Need to move the genesis hash to a configuration case class.
  val GENESIS_BLOCK_HASH = Hash( bytes("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f") )

  // BUGBUG : Send from the latest block header intead of the genesis block.
  // Get the latest block header from BlockStorage
  blockProcessor ! BlockProcessor.DownloadBlocksFrom( GENESIS_BLOCK_HASH )

  import DomainMessageRouter._

  def receive() : Receive = {
    case message : Addr => {
      println("ProtocolMessageReceiver received : " + message)
      peerClientProcessor forward message
    }
    case (from: InetSocketAddress, message : Inv) => {
      val blockInventories = message.inventories.filter( _.invType == InvType.MSG_BLOCK)
      if (!blockInventories.isEmpty) {
        blockProcessor forward (from, blockInventories)
      }

      val transactionInventories = message.inventories.filter( _.invType == InvType.MSG_TX)
      if (!transactionInventories.isEmpty) {
        transactionProcessor forward (from, transactionInventories)
      }

      // Inventory vectors whose InvType(s) are InvType.ERROR and InvType.MSG_FILTERED_BLOCK are ignored.
      println("ProtocolMessageReceiver received : " + message)
    }
    case message : Headers => {
      println("ProtocolMessageReceiver received : " + message)
      blockProcessor forward message
    }
    case message : Transaction => {
      println("ProtocolMessageReceiver received : " + message)
      transactionProcessor forward message
    }
    case message : Block => {
      println("ProtocolMessageReceiver received : " + message)
      blockProcessor forward message
    }
  }
}


