package io.scalechain.blockchain.cli

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import io.scalechain.blockchain.net.ServerConsumer

/**
  * Created by kangmo on 1/8/16.
  */
object ScaleChainServer extends App {

  val system = ActorSystem("ScaleChainServer", ConfigFactory.load.getConfig("server"))
  val httpConsumer = system.actorOf(Props[ServerConsumer], "httpConsumer")
}
