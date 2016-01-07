package io.scalechain.blockchain.net

import akka.actor.Actor
import akka.camel.{CamelMessage, Producer}

/**
  * Created by kangmo on 1/8/16.
  */
class ClientProducer extends Actor with Producer {
  def endpointUri = "http://localhost:8778"
  override protected def transformResponse(msg:Any) = {
    msg match {
      case cm : CamelMessage => {
        cm.bodyAs[String]
      }
      case _ => {
        super.transformResponse(msg)
      }
    }
  }
}
