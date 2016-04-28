package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.{BlockVerificationException, TransactionVerificationException}
import io.scalechain.blockchain.net.DomainMessageRouter.InventoriesFrom
import io.scalechain.blockchain.net.PeerBroker
import io.scalechain.blockchain.net.PeerBroker.SendToOne
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.{DiskBlockStorage, GenesisBlock}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil._
import io.scalechain.blockchain.transaction.BlockVerifier

import java.io.File

import org.slf4j.LoggerFactory

object BlockProcessor {
  val ZERO_FILLED_HASH   = Hash( bytes("0"*64) )

  case class StartInitialBlockDownload()
  case class DownloadBlocksFrom(hash : Hash)

  case class PutBlock(block : Block)
  case class PutBlockResult(isNewBlock : Boolean)

  def props(peerBroker : ActorRef) = Props[BlockProcessor](new BlockProcessor(peerBroker))

  var theBlockProcessor : ActorRef = null

  def create(system : ActorSystem, peerBroker : ActorRef) : ActorRef = {
    assert(theBlockProcessor == null)
    theBlockProcessor = system.actorOf(BlockProcessor.props(peerBroker))
    theBlockProcessor
  }

  /** Get the block processor actor. This actor is a singleton, used by API layer to access block database.
    *
    * @return The block processor actor.
    */
  def get() : ActorRef = {
    assert(theBlockProcessor != null)
    theBlockProcessor
  }
}

/**
  * Created by kangmo on 2/14/16.
  */
class BlockProcessor(peerBroker : ActorRef) extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[BlockProcessor])

  var blockDownloadStarted = false

  val blockStorage = DiskBlockStorage.create(new File("./target/blockdata/"))
  import BlockProcessor._

  def receive : Receive = {
    case PutBlock(block) => {
      sender ! PutBlockResult( blockStorage.putBlock(block) )
      println("processed PutBlock.")
    }

    case StartInitialBlockDownload() => {
      if ( blockDownloadStarted ) {
        // TODO : Check if all blocks are downloaded.
        // TOOD : Check if we did not receive a response after sending get headers.
        println("Block download already started.")
      } else {
        val startBlockHash = blockStorage.getBestBlockHash().getOrElse(GenesisBlock.HASH)
        self ! DownloadBlocksFrom(startBlockHash)
        println("[BlockProcessor] Sent DownloadBlocksFrom to myself..")
        blockDownloadStarted = true
      }
    }

    case DownloadBlocksFrom(fromHash) => {
      println("Starting to download block. Header hash : "+ fromHash)
      val getheaders = GetHeaders(70002L, List(fromHash), ZERO_FILLED_HASH)
      peerBroker ! PeerBroker.SendToAny(getheaders)
      println("Received DownloadBlocksFrom. Hash : " + fromHash)
    }

    case headers : Headers => {
      val MAX_BLOCK_HEADERS_PER_MESSAGE = 2000
      if (headers.headers.size >= MAX_BLOCK_HEADERS_PER_MESSAGE) {
        val lastblockHeaderHash = Hash( HashCalculator.blockHeaderHash(headers.headers.last) )
        println("Got 2000 headers, Continuing to download block. Header hash : "+ lastblockHeaderHash)

        self ! DownloadBlocksFrom( lastblockHeaderHash )
      } else {
        println(s"Got ${headers.headers.size} headers. Stopping.")
      }
      storeBlockHeaders(headers.headers)

      // BUGBUG : Need to download all headers, and then download block data.
      distributeBlockHeaders(headers.headers)
    }

    case block : Block => {
      storeBlock(block)
    }

    case InventoriesFrom(address : InetSocketAddress, inventories : List[InvVector]) => {
      //assert(inventory.invType == InvType.MSG_BLOCK)
      println(s"BlockProcessor received InvVector(MSG_BLOCK). from : ${address}")

      val newBlockInventories = inventories.filter { inventory =>
        ! blockStorage.hasBlock(inventory.hash)
      }

      if (! newBlockInventories.isEmpty) {
        peerBroker ! SendToOne( address, GetData(newBlockInventories) )
        println(s"Sent GetData(block inventories). to : ${address}")
      }
    }
  }

  def storeBlock(block : Block) : Unit = {
    try {

      // Step 1 : Check if the previous block header and data exists.
      val prevBlockIsComplete =
        blockStorage.getBlock(block.header.hashPrevBlock) match {
          case Some((_, _)) => {
            true
          }
          case _ => {
            false
          }
        }

      // Step 2 : Verify the block and put the block only if the previous block exists.
      if (prevBlockIsComplete) {
        // TODO : Put the transactions into mempool. Verify the block first before putting into the chain.

        // Put the block.
        blockStorage.putBlock(block)

        // Verify the block.
//        new BlockVerifier(block).verify(DiskBlockStorage.get)

      } else {
        logger.warn(s"The previous block data is not found yet. Discarding block. Header : ${block.header}")
      }

      /*
      // BUGBUG : this is too slow. remove it after we are confident.
      try {
        val Some((blockInfo, foundBlock)) = blockStorage.getBlock(Hash(HashCalculator.blockHeaderHash(block.header)))
        assert( foundBlock == block )
      } catch {
        case e: Throwable => {
          println("block not decodable : " + block)
          assert(false)
        }
      }
      */


/*
      println("Stored a block with following transactions")
      for (tx <- block.transactions) {
        println(s"transaction : $tx")
        println(s"serialized : ${HexUtil.hex(TransactionCodec.serialize(tx))}")
      }
*/
      // println("BlockProcessor received Block : The block was stored.")
    } catch {
      case e: BlockVerificationException => {
        logger.warn(s"Block verification failed. block header : ${block.header}, error : $e" )
      }
    }
  }

  def storeBlockHeaders(headers : List[BlockHeader]) : Unit = {
    headers.foreach { header =>
      blockStorage.putBlockHeader(header)
    }
    println(s"Stored ${headers.size} headers.")
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
