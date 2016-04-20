package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

import akka.stream.FlowShape
import akka.stream.scaladsl.{Merge, GraphDSL, Flow, Source}
import akka.util.ByteString
import io.scalechain.blockchain.net.graphs.ConcurrentQueueSource
import io.scalechain.blockchain.proto.{ProtocolMessage}

object PeerLogic {
  def flow(remoteAddress: InetSocketAddress) = new PeerLogic().getFlow( remoteAddress )
}
/**
  * Created by kangmo on 2/14/16.
  */
class PeerLogic {
  def getFlow(remoteAddress: InetSocketAddress) : Flow[ByteString, ByteString, LinkedBlockingQueue[ProtocolMessage]] = {

    val requester : Source[ProtocolMessage, LinkedBlockingQueue[ProtocolMessage]]
      = ConcurrentQueueSource.create[ProtocolMessage]

    val protocolDecoder = new ProtocolDecoder()

    val peerFlow = Flow.fromGraph(GraphDSL.create(requester) { implicit builder =>
      requesterShape : requester.Shape => {
        import GraphDSL.Implicits._


        val decoder = builder.add( Flow[ByteString]
          .map(protocolDecoder.decode(_))
        )

        val messageHandler = new ProtocolMessageHandler()

        // Transforms a list of request messages to response messages in the stream.
        val transformer = builder.add( Flow[List[ProtocolMessage]]
          // Step 1 : Flatten List[ProtocolMessage]. Stream items are changed from List[ProtocolMessage] to ProtocolMessage.
          .mapConcat(identity)
          // Step 2 : Handle each message. Stream items are changed from ProtocolMessage to Option[ProtocolMessage]
          //          If we need to respond, we will have Some(message). None otherwise.
          .map(messageHandler.handle)
          // Step 3 : Get rid of None items. Convert Some(message) to message.
          .collect{ case Some(message) => message }
        )

        val merger = builder.add(Merge[ProtocolMessage](2))

        val encoder = builder.add( Flow[ProtocolMessage]
          .map(ProtocolEncoder.encode(_))
        )

        requesterShape ~> merger.in(0)

        decoder.out ~> transformer ~> merger.in(1)

        merger.out ~> encoder

        FlowShape(decoder.in, encoder.out)
      }
    })

    peerFlow
  }
}


