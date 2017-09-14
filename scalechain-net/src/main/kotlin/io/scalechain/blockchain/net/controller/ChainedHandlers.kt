package io.scalechain.blockchain.net.controller

import io.scalechain.blockchain.net.handler.MessageHandlerContext
import io.scalechain.blockchain.proto.Addr

/**
 * A message handler for a specific message.
 * The handler is called whenever a new message arrives at the node.
 *
 * Multiple message handlers can be registers for a message and form a chain of handlers.
 * Upon a receival of a message, each handler in the chain is called one by one until any of the handler returns true,
 * which means 'the message was processed by the handler and should not be propagated to the following handlers in the handler chain'.
 *
 * Type parameter:
 * MessageType The type of the message to handle.
 */
interface MessageHandler<in MessageType> {
  /**
   * The message handler which is called whenever a message is received.
   *
   * @param context The context where handlers handling different messages for a peer can use to store state data.
   * @param message The received message to handle.
   * @return True if the message was processed and calling hanlder should stop propagating messages to the next message handler.
   */
  fun handle(context : MessageHandlerContext, message : MessageType) : Boolean
}

/**
 * A node controller in charge of sending/receiving P2P messages for a specific logic.
 * For example, InitialBlockDownloadController is in charge of sending/receiving messages about the initial block download when a node starts up.
 */
//interface NodeController


/**
 * Maintain a list of handlers, handle each of them in order until any of handlers returns true.
 *
 * @param handlers An array of handlers passed as a vararg to this object.
 */
class ChainedHandlers<MessageType>(vararg val handlers: MessageHandler<MessageType>) : MessageHandler<MessageType> {
  /**
   * Visit each of handlers in the handlers chain until any of them returns true.
   *
   * @param context The context where handlers handling different messages for a peer can use to store state data.
   * @param message The received message to handle.
   * @return True if the message was processed and calling hanlder should stop propagating messages to the next message handler.
   */
  override fun handle(context : MessageHandlerContext, message : MessageType) : Boolean {
    handlers.map { handler ->
      if ( handler.handle(context, message) ) {
        return true
      }
    }
    return false
  }
}
