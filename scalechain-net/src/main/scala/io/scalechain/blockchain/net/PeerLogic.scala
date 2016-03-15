package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.stream.FlowShape
import akka.stream.scaladsl.{Merge, GraphDSL, Flow, Source}
import akka.util.ByteString
import io.scalechain.blockchain.proto.{ProtocolMessage}

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


