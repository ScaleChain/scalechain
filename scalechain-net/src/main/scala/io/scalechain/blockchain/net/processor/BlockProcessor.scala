package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import io.scalechain.blockchain.net.DomainMessageRouter.InventoriesFrom
import io.scalechain.blockchain.net.PeerBroker
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.DiskBlockStorage
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.util.HexUtil._

import java.io.File

object BlockProcessor {
  val ZERO_FILLED_HASH   = Hash( bytes("0000000000000000000000000000000000000000000000000000000000000000") )
  case class DownloadBlocksFrom(hash : Hash)

  def props(peerBroker : ActorRef) = Props[BlockProcessor](new BlockProcessor(peerBroker))
}

/**
  * Created by kangmo on 2/14/16.
  */
class BlockProcessor(peerBroker : ActorRef) extends Actor {
  val blockStorage = new DiskBlockStorage(new File("./target/"))
  import BlockProcessor._

  def receive : Receive = {
    case DownloadBlocksFrom(fromHash) => {
      val getheaders = GetHeaders(70002L, List(fromHash), ZERO_FILLED_HASH)
      peerBroker ! PeerBroker.SendToAny(getheaders)
    }

    case headers : Headers => {
      val MAX_BLOCK_HEADERS_PER_MESSAGE = 2000
      if (headers.headers.count(_ => true) >= MAX_BLOCK_HEADERS_PER_MESSAGE) {
        val lastblockHeaderHash = HashCalculator.blockHeaderHash(headers.headers.last)

        self ! DownloadBlocksFrom( Hash(lastblockHeaderHash) )
      }
      storeBlockHeaders(headers.headers)
      distributeBlockHeaders(headers.headers)
    }

    case block : Block => {
      println("BlockProcessor received Block")
      blockStorage.putBlock(block)
    }

    case InventoriesFrom(from : InetSocketAddress, inventories : List[InvVector]) => {
      //assert(inventory.invType == InvType.MSG_BLOCK)
      println("BlockProcessor received InvVector(MSG_BLOCK)")

      val newBlockInventories = inventories.filter { inventory =>
        ! blockStorage.hasBlock(inventory.hash)
      }

      if (! newBlockInventories.isEmpty) {
        peerBroker ! (null, from, Some(GetData(newBlockInventories)) )
      }
    }

  }

  def storeBlockHeaders(headers : List[BlockHeader]) : Unit = {
    headers.foreach { header =>
      blockStorage.putBlockHeader(header)
    }
  }

  def distributeBlockHeaders(headers : List[BlockHeader]): Unit = {
    val blockHeaderHashes = headers.map { blockHeader : BlockHeader =>
      // Optimize to send all headers to peerBroker, and distribute them to live nodes.
      val blockHeaderHash = HashCalculator.blockHeaderHash(blockHeader)
      Hash(blockHeaderHash)
    }

    val inventories = for {
      blockHeaderHash <- blockHeaderHashes
    } yield(InvVector(InvType.MSG_BLOCK, blockHeaderHash))

    val getdata = GetData( inventories )

    peerBroker ! PeerBroker.SendToAny( getdata )
  }

}
