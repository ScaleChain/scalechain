package io.scalechain.blockchain.net.p2p

import java.util.TimerTask
import java.util.Timer
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelFuture
import io.scalechain.blockchain.net.message.VersionFactory
import io.scalechain.blockchain.net.NodeClient
import io.scalechain.blockchain.net.PeerSet
import io.scalechain.util.ExceptionUtil
import io.scalechain.util.StackUtil
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 5/26/16.
  */
class RetryingConnector(private val peerSet : PeerSet, private val retryIntervalSeconds : Int) {
  private val logger = LoggerFactory.getLogger(RetryingConnector::class.java)

  fun connect(address : String, port : Int) : Unit {
    // TODO : BUGBUG : Need to call nodeClient.close when the connection closes?
    val nodeClient = NodeClient(peerSet)
    val channelFuture : ChannelFuture = nodeClient.connect(address, port)

    channelFuture.addListener( object : ChannelFutureListener {
      override fun operationComplete(future : ChannelFuture) : Unit {
        val channel : Channel = future.channel()
        if ( future.isSuccess() ) {
          logger.info("Sending version message to ${channel.remoteAddress()}")

          // Upon successful connection, send the version message.
          channel.writeAndFlush( VersionFactory.create() )


          future.channel().closeFuture().addListener( object : ChannelFutureListener {
            override fun operationComplete(future:ChannelFuture) {
              assert( future.isDone )

              if (future.isSuccess) { // completed successfully
                logger.info("Connection closed. Remote address : ${channel.remoteAddress()}")
              }

              if (future.cause() != null) { // completed with failure
                val causeDescription = ExceptionUtil.describe( future.cause().cause )
                logger.warn("Failed to close connection. Remote address : ${channel.remoteAddress()}. Exception : ${future.cause().message}, Stack Trace : ${StackUtil.getStackTrace(future.cause())} ${causeDescription}")
              }

              if (future.isCancelled) { // completed by cancellation
                logger.warn("Canceled to close connection. Remote address : ${channel.remoteAddress()}")
              }

              peerSet.remove(channel.remoteAddress())
            }
          })
        } else {
          channel.close()
          nodeClient.close()

          // TODO : Do we need to check future.isCanceled()?
          logger.info("Connection to ${address}:${port} failed. Will try in a second.")
          val timer = Timer(true);
          timer.schedule( object : TimerTask() {
            override fun run(): Unit  {
              connect(address, port)
            }
          }, (retryIntervalSeconds * 1000).toLong());
        }
      }
    })
  }
}
