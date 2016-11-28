package io.scalechain.blockchain.net.p2p

import java.util.{TimerTask, Timer}

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.typesafe.scalalogging.Logger
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelFuture
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.message.VersionFactory
import io.scalechain.blockchain.net.Peer
import io.scalechain.blockchain.net.NodeClient
import io.scalechain.blockchain.net.PeerSet
import io.scalechain.blockchain.proto.IPv6Address
import io.scalechain.blockchain.proto.NetworkAddress
import io.scalechain.blockchain.proto.Version
import io.scalechain.util.HexUtil.
import io.scalechain.util.Config
import io.scalechain.util.ExceptionUtil
import io.scalechain.util.StackUtil
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by kangmo on 5/26/16.
  */
class RetryingConnector(peerSet : PeerSet, retryIntervalSeconds : Int) {
  private val logger = LoggerFactory.getLogger(RetryingConnector::class.java)

  fun connect(address : String, port : Int) : Unit {
    // TODO : BUGBUG : Need to call nodeClient.close when the connection closes?
    val nodeClient = NodeClient(peerSet)
    val channelFuture : ChannelFuture = nodeClient.connect(address, port)

    channelFuture.addListener( ChannelFutureListener() {
      fun operationComplete(future : ChannelFuture) : Unit {
        val channel : Channel = future.channel()
        if ( future.isSuccess() ) {
          logger.info(s"Sending version message to ${channel.remoteAddress()}")

          // Upon successful connection, send the version message.
          channel.writeAndFlush( VersionFactory.create )


          future.channel().closeFuture.addListener( ChannelFutureListener() {
            fun operationComplete(future:ChannelFuture) {
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

              logger.info(s"Connection to ${address}:${port} closed. Will reconnect in a second.")

              val timer = new Timer(true);
              timer.schedule( new TimerTask {
                override def run() : Unit = {
                  connect(address, port)
                }
              }, 1000);

              peerSet.remove(channel.remoteAddress())
            }
          })
        } else {
          channel.close()
          nodeClient.close()

          // TODO : Do we need to check future.isCanceled()?
          logger.info(s"Connection to ${address}:${port} failed. Will try in a second.")
          val timer = new Timer(true);
          timer.schedule( new TimerTask {
            override def run(): Unit = {
              connect(address, port)
            }
          }, 1000);
        }
      }
    })
  }
}
