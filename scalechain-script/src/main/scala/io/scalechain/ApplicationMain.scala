package io.scalechain

import java.math.BigInteger

import akka.actor.ActorSystem
import io.scalechain.blockchain.proto.PingMessage

object ApplicationMain extends App {
  
  val system = ActorSystem("MyActorSystem")
  val pingActor = system.actorOf(PingActor.props, "pingActor")

  pingActor ! PingMessage(BigInteger.ONE)

  // This example app will ping pong 3 times and thereafter terminate the ActorSystem - 
  // see counter logic in PingActor
  system.awaitTermination()

}