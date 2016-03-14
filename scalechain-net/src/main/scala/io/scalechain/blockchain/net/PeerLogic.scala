package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{Props, ActorRef}
import akka.stream.FlowShape
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Merge, GraphDSL, Flow, Source}
import akka.util.ByteString
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

object PeerLogic {
  def flow(messageReceiver : ActorRef, remoteAddress: InetSocketAddress) = new PeerLogic().getFlow( messageReceiver, remoteAddress )
}
/**
  * Created by kangmo on 2/14/16.
  */
class PeerLogic {
  def getFlow(protocolMessageReceiver : ActorRef, remoteAddress: InetSocketAddress) : (Flow[ByteString, ByteString, ActorRef], ProtocolMessageTransformer) = {

    val requester : Source[List[ProtocolMessage], akka.actor.ActorRef] =
      Source.actorPublisher[List[ProtocolMessage]](ServerRequester.props)

    val protocolDecoder = new ProtocolDecoder()
    val messageTransformer = new ProtocolMessageTransformer(protocolMessageReceiver, remoteAddress)

    val peerFlow = Flow.fromGraph(GraphDSL.create(requester) { implicit builder =>
      requesterShape : requester.Shape => {
        import GraphDSL.Implicits._


        val decoder = builder.add( Flow[ByteString]
          .map(protocolDecoder.decode(_))
        )

        val transformer = builder.add( Flow[List[ProtocolMessage]]
          .transform(() => messageTransformer)
        )

        val merger = builder.add(Merge[List[ProtocolMessage]](2))

        val encoder = builder.add( Flow[List[ProtocolMessage]]
          .map(ProtocolEncoder.encode(_))
        )


        requesterShape ~> merger.in(0)

        decoder.out ~> transformer ~> merger.in(1)

        merger.out ~> encoder

        FlowShape(decoder.in, encoder.out)
      }
    })

    (peerFlow, messageTransformer)
  }
}


