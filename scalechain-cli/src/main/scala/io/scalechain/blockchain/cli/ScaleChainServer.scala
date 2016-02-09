package io.scalechain.blockchain.cli

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import io.scalechain.blockchain.net.{PeerBroker, ServerConsumer}

/**
  * Created by kangmo on 1/8/16.
  */
object ScaleChainServer extends App {
  val DEFAULT_PORT = 8778

  val system = ActorSystem("ScaleChainServer", ConfigFactory.load.getConfig("server"))

  val peerBroker = system.actorOf(Props[PeerBroker], "peerBroker")

  val consumer = system.actorOf(ServerConsumer(DEFAULT_PORT, peerBroker), "consumer")
}
