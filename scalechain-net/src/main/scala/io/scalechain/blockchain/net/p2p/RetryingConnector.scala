package io.scalechain.blockchain.net.p2p

import io.netty.channel.{Channel, ChannelFutureListener, ChannelFuture}
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
  private val logger = LoggerFactory.getLogger(classOf[RetryingConnector])

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
          val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

          channel.writeAndFlush(versionMessage)

          future.channel().closeFuture.addListener( new ChannelFutureListener() {
            def operationComplete(future:ChannelFuture) {
              assert( future.isDone )

              if (future.isSuccess) { // completed successfully
                logger.info(s"Connection closed. Remote address : ${channel.remoteAddress()}")
              }

              if (future.cause() != null) { // completed with failure
                val causeDescription = ExceptionUtil.describe( future.cause.getCause )
                logger.info(s"Failed to close connection. Remote address : ${channel.remoteAddress()}. Exception : ${future.cause.getMessage}, Stack Trace : ${StackUtil.getStackTrace(future.cause())} ${causeDescription}")
              }

              if (future.isCancelled) { // completed by cancellation
                logger.info(s"Canceled to close connection. Remote address : ${channel.remoteAddress()}")
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
