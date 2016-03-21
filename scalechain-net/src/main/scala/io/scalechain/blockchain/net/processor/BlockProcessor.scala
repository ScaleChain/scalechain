package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.{BlockVerificationException, TransactionVerificationException}
import io.scalechain.blockchain.net.DomainMessageRouter.InventoriesFrom
import io.scalechain.blockchain.net.PeerBroker
import io.scalechain.blockchain.net.PeerBroker.SendToOne
import io.scalechain.blockchain.net.processor.BlockProcessor.GetBestBlockHash
import io.scalechain.blockchain.net.processor.TransactionProcessor.{GetTransactionResult, GetTransaction}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.{GenesisBlock, DiskBlockStorage}
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
  case class GetBlock(blockHash: Hash)
  case class GetBlockResult(blockOption : Option[Block])

  case class GetBestBlockHash()
  case class GetBestBlockHashResult(blockHashOption : Option[Hash])

  case class PutBlock(block : Block)
  case class PutBlockResult(isNewBlock : Boolean)

  case class GetTransaction(txHash : Hash)
  case class GetTransactionResult(transactionOption : Option[Transaction])

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
    case BlockProcessor.GetTransaction(txHash) => {
      sender ! BlockProcessor.GetTransactionResult( blockStorage.getTransaction(txHash) )
      println("processed GetTransaction.")
    }

    case PutBlock(block) => {
      sender ! PutBlockResult( blockStorage.putBlock(block) )
      println("processed PutBlock.")
    }

    case GetBlock(blockHash) => {
      sender ! GetBlockResult( blockStorage.getBlock(blockHash) )
      println("processed GetBlock.")
    }

    case GetBestBlockHash() => {
      sender ! GetBestBlockHashResult( blockStorage.getBestBlockHash() )
      println("processed GetBestBlockHash.")
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
        val lastblockHeaderHash = HashCalculator.blockHeaderHash(headers.headers.last)
        println("Got 2000 headers, Continuing to download block. Header hash : "+ lastblockHeaderHash)

        self ! DownloadBlocksFrom( Hash(lastblockHeaderHash) )
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
    // Step 1 : Validate block
    try {
      // BUGBUG : We hit this issue : TransactionVerificationException(ErrorCode(script_eval_failure)
      // new BlockVerifier(block).verify(DiskBlockStorage.get)

      blockStorage.putBlock(block)
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
        println(s"Block verification failed. block header : ${block.header}, error : $e")
        logger.warn(s"Block verification failed. block header : ${block.header}, error : $e" )
      }
      case e: TransactionVerificationException => {
        println(s"Transaction verification failed. block header : ${block.header}, error : $e")
        logger.warn(s"Transaction verification failed. block header : ${block.header}, error : $e" )
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
