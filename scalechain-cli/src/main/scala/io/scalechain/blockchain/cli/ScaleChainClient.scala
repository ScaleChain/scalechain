package io.scalechain.blockchain.cli

import scala.concurrent.duration._
import akka.util
import akka.pattern.ask

/** A testing app for checking if a ScaleChain server is working well.
  */
object ScaleChainClient extends App {
  val clientKernel = new ClientKernel
  implicit val timeout = util.Timeout(55 seconds)
  clientKernel.mediator ? (clientKernel.clientProducer, "HELLO")
}
