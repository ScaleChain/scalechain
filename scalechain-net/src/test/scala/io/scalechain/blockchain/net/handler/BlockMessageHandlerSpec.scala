package io.scalechain.blockchain.net.handler

import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest._

class BlockMessageHandlerSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with HandlerTestTrait {
  this: Suite =>

  var context : MessageHandlerContext = null
  var channel : EmbeddedChannel = null

  override def beforeEach() {
    // initialization code.
    channel = new EmbeddedChannel()
    context = context(channel)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalization code
    context = null
    channel.close()
  }
}