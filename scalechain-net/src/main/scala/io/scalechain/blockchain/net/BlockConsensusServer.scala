package io.scalechain.blockchain.net

import java.io._

import bftsmart.tom.{MessageContext, ServiceReplica}
import bftsmart.tom.server.defaultservices.{DefaultSingleRecoverable, DefaultRecoverable}
import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.codec.{BlockHeaderCodec, HashCodec}
import io.scalechain.blockchain.script.HashSupported
import org.slf4j.LoggerFactory
import HashSupported._

class BlockConsensusServer(id: Int) extends DefaultSingleRecoverable {
  private lazy val logger = Logger( LoggerFactory.getLogger(classOf[BlockConsensusServer]) )

  private var replica: ServiceReplica = new ServiceReplica(id, this, this)

  override def appExecuteUnordered(command: Array[Byte], msgCtx: MessageContext): Array[Byte] = {
    // No support for the unordered command.
    assert(false)
    null
  }

  override def appExecuteOrdered(command: Array[Byte], msgCtx: MessageContext): Array[Byte] = {
    try {
      val blockHeader = BlockHeaderCodec.parse(command)

      BlockGateway.putConsensualHeader(blockHeader)

      if (msgCtx != null) {
        if (msgCtx.getConsensusId == -1) {
          logger.trace(s"New consensual header : ${blockHeader.hash}, ${blockHeader}")
        }
        else {
          logger.trace(s"[${msgCtx.getConsensusId}] New consensual header : ${blockHeader.hash}, ${blockHeader}")
        }
      }
      else {
        logger.trace(s"New consensual header : ${blockHeader.hash}, ${blockHeader}")
      }
      // Nothing to reply.
      Array[Byte]()
    }
    catch {
      case ex: IOException => {
        logger.error("Invalid request received!")
        Array[Byte]()
      }
    }
  }

  @SuppressWarnings(Array("unchecked"))
  override def installSnapshot(state: Array[Byte]) {
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
        logger.error("[ERROR] Error deserializing state: " + e.getMessage)
      }
    }
  }

  override def getSnapshot: Array[Byte] = {
    try {
      System.out.println("getState called")
      // TODO : Rethink syncrhonization.
      val chain = Blockchain.get
      val rawBestBlockHash = HashCodec.serialize( chain.getBestBlockHash()(chain.db).get )
      rawBestBlockHash
    }
    catch {
      case ioe: IOException => {
        logger.error("[ERROR] Error serializing state: " + ioe.getMessage)
        return "ERROR".getBytes
      }
    }
  }
}
