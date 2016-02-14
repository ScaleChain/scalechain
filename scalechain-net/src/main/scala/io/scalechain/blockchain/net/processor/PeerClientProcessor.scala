package io.scalechain.blockchain.net.processor

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import io.scalechain.blockchain.proto.{Addr, NetworkAddressWithTimestamp}

import scala.collection.mutable



object PeerClientProcessor {
  val props = Props[PeerClientProcessor]
}

/**
  * Created by kangmo on 2/14/16.
  */
class PeerClientProcessor extends Actor {
  val addresses = mutable.HashMap[InetSocketAddress, NetworkAddressWithTimestamp]()

  def receive : Receive = {
    case addr : Addr => {
      addr.addresses.foreach { n : NetworkAddressWithTimestamp =>
        val sockAddress = new InetSocketAddress( n.address.ipv6.inetAddress, n.address.port )
        addresses.put(sockAddress, n )
        // TODO : create a new connection to the discovered peer.
        // TODO : save peers to a persistent storage.
      }
    }
  }
}
