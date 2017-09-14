package io.scalechain.blockchain.net.controller

import io.scalechain.blockchain.net.handler.MessageHandlerContext
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Inv
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Version

/**
 * The controller in charge of handling various messages for the IBD(initial block download)
 *
 * Why have message handlers for a subject such as IBD?
 * Having handler codes spread along various messages for a subject such as IBD make it hard to understand code and debug.
 *
 * Also, for unit testing it is better to have code snippets for a subject grouped in a controller.
 * We can register these code snippets for each message handler in ProtocolMessageHandler.kt.
 */
open class InitialBlockDownloadController {
  /**
   * Handle Version message in the context of IBD.
   */
  val versionMessageHandler = object: MessageHandler<Version> {
    override fun handle(context : MessageHandlerContext, message : Version) : Boolean {
      return true
    }
  }

  /**
   * Handle Block message in the context of IBD.
   */
  val blockMessageHandler = object: MessageHandler<Block> {
    override fun handle(context : MessageHandlerContext, message : Block) : Boolean {
      return true
    }
  }

  /**
   * Handle Transaction message in the context of IBD.
   */
  val transactionMessageHandler = object: MessageHandler<Transaction> {
    override fun handle(context : MessageHandlerContext, message : Transaction) : Boolean {
      return true
    }
  }

  /**
   * Handle Inv message in the context of IBD.
   */
  val invMessageHandler = object: MessageHandler<Inv> {
    override fun handle(context : MessageHandlerContext, message : Inv) : Boolean {
      return true
    }
  }

  companion object : InitialBlockDownloadController() {
  }
}