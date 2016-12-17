package io.scalechain.blockchain.net

import java.io.*

import bftsmart.consensus.messages.ConsensusMessage
import bftsmart.tom.MessageContext
import bftsmart.tom.ServiceReplica
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable
import bftsmart.tom.server.defaultservices.DefaultRecoverable
import bftsmart.tom.server.defaultservices.DefaultReplier
import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.script.*
import org.slf4j.LoggerFactory

class BlockConsensusServer(id: Int, configHome : String) : DefaultRecoverable() {
  private val logger = LoggerFactory.getLogger(BlockConsensusServer::class.java)

  private var replica: ServiceReplica = ServiceReplica(id, configHome, this, this, null, DefaultReplier())

  override fun appExecuteUnordered(command: ByteArray?, msgCtx: MessageContext?): ByteArray {
    // No support for the unordered command.
    throw UnsupportedOperationException()
  }

  override fun appExecuteBatch(commands: Array<out ByteArray>, msgCtxs: Array<out MessageContext?>): Array<out ByteArray> {
    return (commands zip msgCtxs).map{ pair ->
      val command = pair.first
      val ctx = pair.second
      appExecuteOrdered(command, ctx)
    }.toTypedArray()
  }

  private fun appExecuteOrdered(command: ByteArray, msgCtx: MessageContext?): ByteArray {
    try {
      logger.trace("appExecuteOrdered invoked : ${msgCtx}")

      val blockHeader = BlockHeaderCodec.decode(command)!!

      logger.trace("appExecuteOrdered : received block header : ${blockHeader}")

      BlockGateway.putConsensualHeader(blockHeader)

      /*
      val consensusMesssages : java.util.Set<ConsensusMessage> = msgCtx.getProof()
      consensusMesssages.asScala.map { message =>
        message.getProof()
      }*/

      if (msgCtx != null) {
        if (msgCtx.getConsensusId() == -1) {
          logger.trace("New consensual header : ${blockHeader.hash()}, ${blockHeader}")
        }
        else {
          logger.trace("<${msgCtx.getConsensusId()}> New consensual header : ${blockHeader.hash()}, ${blockHeader}")
        }
      }
      else {
        logger.trace("New consensual header : ${blockHeader.hash()}, ${blockHeader}")
      }
      // Nothing to reply.
      return byteArrayOf()
    }
    catch( ex: IOException ) {
      logger.error("Invalid request received!")
      return byteArrayOf()
    }
  }

  override fun installSnapshot(state: ByteArray) {
    try {
      logger.trace("setState called")
      val bestBlockHash = HashCodec.decode(state)!!

      // See if we have the best block hash.
      val chain = Blockchain.get()
      if (chain.hasBlock(chain.db, bestBlockHash)) {
        // We have the hash. We are ok.
      } else {
        logger.info("Setting the snapshot block hash. ${bestBlockHash}")

        Node.get().lastBlockHashForIBD = bestBlockHash
        // We don't have the best block hash. Need to start IBD(Initial block download).
        // TODO : Switch to initial block download mode.
      }
    }
    catch(e: Exception) {
      logger.error("<ERROR> Error deserializing state: " + e.message)
    }
  }

  override fun getSnapshot(): ByteArray {
    try {
      System.out.println("getState called")
      // TODO : Rethink syncrhonization.
      val chain = Blockchain.get()
      val rawBestBlockHash = HashCodec.encode( chain.getBestBlockHash(chain.db)!! )
      return rawBestBlockHash
    }
    catch (ioe: IOException){
      logger.error("<ERROR> Error serializing state: " + ioe.message)
      return "ERROR".toByteArray()
    }
  }
}
