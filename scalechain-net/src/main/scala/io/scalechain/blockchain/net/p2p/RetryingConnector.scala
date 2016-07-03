package io.scalechain.blockchain.net.p2p

import com.typesafe.scalalogging.Logger
import io.netty.channel.{Channel, ChannelFutureListener, ChannelFuture}
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.message.VersionFactory
import io.scalechain.blockchain.net.{Peer, NodeClient, PeerSet}
import io.scalechain.blockchain.proto.{IPv6Address, NetworkAddress, Version}
import io.scalechain.util.HexUtil._
import io.scalechain.util.{ExceptionUtil, StackUtil}
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

/**
  * Created by kangmo on 5/26/16.
  */
class RetryingConnector(peerSet : PeerSet, retryIntervalSeconds : Int) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[RetryingConnector]) )

  def connect(address : String, port : Int) : Unit = {
    // TODO : BUGBUG : Need to call nodeClient.close when the connection closes?
    val nodeClient = new NodeClient(peerSet)
    val channelFuture : ChannelFuture = nodeClient.connect(address, port)

    channelFuture.addListener( new ChannelFutureListener() {
      def operationComplete(future : ChannelFuture) : Unit = {
        val channel : Channel = future.channel()
        if ( future.isSuccess() ) {
          logger.info(s"Sending version message to ${channel.remoteAddress()}")

          // Upon successful connection, send the version message.
          channel.writeAndFlush( VersionFactory.create )

          future.channel().closeFuture.addListener( new ChannelFutureListener() {
            def operationComplete(future:ChannelFuture) {
              assert( future.isDone )

              if (future.isSuccess) { // completed successfully
                logger.info(s"Connection closed. Remote address : ${channel.remoteAddress()}")
              }

              if (future.cause() != null) { // completed with failure
                val causeDescription = ExceptionUtil.describe( future.cause.getCause )
                logger.warn(s"Failed to close connection. Remote address : ${channel.remoteAddress()}. Exception : ${future.cause.getMessage}, Stack Trace : ${StackUtil.getStackTrace(future.cause())} ${causeDescription}")
              }

              if (future.isCancelled) { // completed by cancellation
                logger.warn(s"Canceled to close connection. Remote address : ${channel.remoteAddress()}")
              }
/*
              logger.info(s"Connection to ${address}:${port} closed. Will reconnect in a second.")
              Thread.sleep(retryIntervalSeconds*1000)
              // Retry connection.
              connect(address, port)
*/
            }
          })
        } else {
          channel.close()
          nodeClient.close()

          // TODO : Do we need to check future.isCanceled()?
          logger.info(s"Connection to ${address}:${port} failed. Will try in a second.")
          Thread.sleep(retryIntervalSeconds*1000)
          connect(address, port)
        }
      }
    })
  }
}
