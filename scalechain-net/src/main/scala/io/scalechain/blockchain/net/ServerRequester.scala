package io.scalechain.blockchain.net

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import io.scalechain.blockchain.proto.{Ping, ProtocolMessage}

import scala.annotation.tailrec

import scala.concurrent.duration._


object ServerRequester {
  val PING_INTERVAL_SECONDS = 1

  case object SendPing
  case object RequestDenied
  case object RequestAccepted

  val props = Props[ServerRequester](new ServerRequester())
}

/** A actor publisher that sends requests to clients from the server for full duplex communication.
  *
  * Code copied from :
  * http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-integrations.html#integrating-with-actors
  */
class ServerRequester extends ActorPublisher[List[ProtocolMessage]] {
  import akka.stream.actor.ActorPublisherMessage._
  import ServerRequester._

  import scala.concurrent.ExecutionContext.Implicits.global
  // BUGBUG : The ping schedule should be created
  val pingSchedule = context.system.scheduler.schedule( initialDelay = 30 second, interval = PING_INTERVAL_SECONDS second, self, ServerRequester.SendPing)

  // We need to canel the ping schedule right before this actor stops.
  // http://doc.akka.io/docs/akka/current/scala/howto.html#Scheduling_Periodic_Messages
  override def postStop() = pingSchedule.cancel


  val MaxBufferSize = 1024
  var buffer = List.empty[ProtocolMessage]

  def receive = {
    case message : ProtocolMessage if buffer.size == MaxBufferSize =>
    //sender ! RequestDenied

    case SendPing if buffer.size == MaxBufferSize =>
    //sender ! RequestDenied

    case SendPing =>
      appendToBuffer(Ping(System.currentTimeMillis()))

    case message : ProtocolMessage =>
      //sender ! RequestAccepted
      appendToBuffer(message)
    case Request(_) => deliverBuffer()
    case Cancel => context.stop(self)
  }

  def appendToBuffer(message:ProtocolMessage) : Unit = {
    if (buffer.isEmpty && totalDemand > 0)
      onNext(List(message))
    else {
      buffer :+= message
      deliverBuffer()
    }
  }

  @tailrec final def deliverBuffer() : Unit = {
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than what buffer.splitAt can accept.
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buffer.splitAt(totalDemand.toInt)
        buffer = keep
        onNext(use)
      } else {
        val (use, keep) = buffer.splitAt(Int.MaxValue)
        buffer = keep

        onNext(use)

        deliverBuffer()
      }
    }
  }
}
