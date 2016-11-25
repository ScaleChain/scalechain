package io.scalechain.blockchain.net

import java.io._

import bftsmart.consensus.messages.ConsensusMessage
import bftsmart.tom.{MessageContext, ServiceReplica}
import bftsmart.tom.server.defaultservices.{DefaultSingleRecoverable, DefaultRecoverable}
import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.codec.{BlockHeaderCodec, HashCodec}
import io.scalechain.blockchain.script.HashSupported
import org.slf4j.LoggerFactory
import HashSupported._
import collection.JavaConverters._

class BlockConsensusServer(id: Int) : DefaultSingleRecoverable {
  private val logger = LoggerFactory.getLogger(BlockConsensusServer.javaClass)

  private var replica: ServiceReplica = ServiceReplica(id, this, this)

  override fun appExecuteUnordered(command: ByteArray, msgCtx: MessageContext): ByteArray {
    // No support for the unordered command.
    assert(false)
    null
  }

  override fun appExecuteOrdered(command: ByteArray, msgCtx: MessageContext): ByteArray {
    try {
      logger.trace(s"appExecuteOrdered invoked : ${msgCtx}")

      val blockHeader = BlockHeaderCodec.parse(command)

      logger.trace(s"appExecuteOrdered : received block header : ${blockHeader}")

      BlockGateway.putConsensualHeader(blockHeader)

      /*
      val consensusMesssages : java.util.Set<ConsensusMessage> = msgCtx.getProof()
      consensusMesssages.asScala.map { message =>
        message.getProof()
      }*/

      if (msgCtx != null) {
        if (msgCtx.getConsensusId == -1) {
          logger.trace(s"New consensual header : ${blockHeader.hash}, ${blockHeader}")
        }
        else {
          logger.trace(s"<${msgCtx.getConsensusId}> New consensual header : ${blockHeader.hash}, ${blockHeader}")
        }
      }
      else {
        logger.trace(s"New consensual header : ${blockHeader.hash}, ${blockHeader}")
      }
      // Nothing to reply.
      ByteArray()
    }
    catch {
      case ex: IOException => {
        logger.error("Invalid request received!")
        ByteArray()
      }
    }
  }

  @SuppressWarnings(Array("unchecked"))
  override fun installSnapshot(state: ByteArray) {
    try {
      logger.trace("setState called")
      val bestBlockHash = HashCodec.parse(state)

      // See if we have the best block hash.
      val chain = Blockchain.get
      if (chain.hasBlock(bestBlockHash)(chain.db)) {
        // We have the hash. We are ok.
      } else {
        // We don't have the best block hash. Need to start IBD(Initial block download).
        // TODO : Switch to initial block download mode.
      }
    }
    catch {
      case e: Exception => {
        logger.error("<ERROR> Error deserializing state: " + e.getMessage)
      }
    }
  }

  override fun getSnapshot: ByteArray {
    try {
      System.out.println("getState called")
      // TODO : Rethink syncrhonization.
      val chain = Blockchain.get
      val rawBestBlockHash = HashCodec.serialize( chain.getBestBlockHash()(chain.db).get )
      rawBestBlockHash
    }
    catch {
      case ioe: IOException => {
        logger.error("<ERROR> Error serializing state: " + ioe.getMessage)
        return "ERROR".getBytes
      }
    }
  }
}
