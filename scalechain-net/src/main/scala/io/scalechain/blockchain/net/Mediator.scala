package io.scalechain.blockchain.net

import akka.actor.{ActorRef, Actor}
import akka.camel.CamelMessage
/**
  * Created by kangmo on 1/8/16.
  */
class Mediator extends Actor {
  def receive = {
    case (actor : ActorRef, msg : String) => {
      actor ! CamelMessage(msg, Map.empty)
    }
    case msg : CamelMessage => {
      println("Got camel message: %s" format msg.body)
    }
    case transformedMsg : String => {
      println("Got transformed msg: %s" format transformedMsg)
    }
    case obj : Any=> {
      println("Got something else:" + obj)
    }
  }
}
