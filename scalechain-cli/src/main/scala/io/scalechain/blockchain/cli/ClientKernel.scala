package io.scalechain.blockchain.cli

import akka.actor.{ActorSystem, Props}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import io.scalechain.blockchain.net.{ClientProducer, Mediator}

/**
  * Created by kangmo on 1/8/16.
  */
class ClientKernel extends Bootable {
  val system = ActorSystem("ScaleChainClient", ConfigFactory.load.getConfig("client"))
  val clientProducer = system.actorOf(Props[ClientProducer])
  val mediator = system.actorOf(Props[Mediator])


  def startup(): Unit = {
  }

  def shutdown(): Unit = {
    system.shutdown()
  }
}
