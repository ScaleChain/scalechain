package io.scalechain.blockchain.net

import akka.camel.{CamelMessage, Consumer}

/**
  * Created by kangmo on 1/8/16.
  */
class ServerConsumer extends Consumer {
  def endpointUri = "jetty://http://0.0.0.0:8778"

  def receive = {
    case msg : CamelMessage => {
      val bodyString = msg.bodyAs[String]
      println("Got %s from the client" format bodyString )
      sender ! ("Got %s from the client" format bodyString )
    }
  }
}
