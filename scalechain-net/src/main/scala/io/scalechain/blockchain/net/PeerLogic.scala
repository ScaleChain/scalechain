package io.scalechain.blockchain.net

import akka.stream.FlowShape
import akka.stream.scaladsl.{Merge, GraphDSL, Flow, Source}
import akka.util.ByteString
import io.scalechain.blockchain.proto.ProtocolMessage


object PeerLogic {
  def flow() = new PeerLogic().getFlow()
}
/**
  * Created by kangmo on 2/14/16.
  */
class PeerLogic {
  def getFlow() = {
    /*
    Source.actorPublisher :
      def actorPublisher[T](props : akka.actor.Props) : akka.stream.scaladsl.Source[T, akka.actor.ActorRef]


     */

    /*
      final class Source[+Out, +Mat](private[stream] override val module : akka.stream.impl.StreamLayout.Module)
        extends scala.AnyRef with akka.stream.scaladsl.FlowOpsMat[Out, Mat]
        with akka.stream.Graph[akka.stream.SourceShape[Out], Mat]
     */
    val requester : Source[List[ProtocolMessage], akka.actor.ActorRef] =
      Source.actorPublisher[List[ProtocolMessage]](ServerRequester.props)

    /*
    GraphDSL.create :
      def create[S <: akka.stream.Shape, Mat](g1 : akka.stream.Graph[akka.stream.Shape, Mat])
      (buildBlock : scala.Function1[akka.stream.scaladsl.GraphDSL.Builder[Mat], scala.Function1[g1.Shape, S]]) : akka.stream.Graph[S, Mat] = { /* compiled code */ }
    */

    /*
        val g : akka.stream.Graph[ByteString, ActorRef] =
        GraphDSL.create(requester) { builder : akka.stream.scaladsl.GraphDSL.Builder[ActorRef] => {
            requesterShape : requester.Shape =>
            import GraphDSL.Implicits._
            requesterShape
          }
        }
    */


    /*
    RunnableGraph.fromGraph :
      def fromGraph[Mat](
        g : akka.stream.Graph[akka.stream.ClosedShape, Mat]) :
          akka.stream.scaladsl.RunnableGraph[Mat] = { /* compiled code */ }


    Flow.fromGraph :
      def fromGraph[I, O, M](
        g : akka.stream.Graph[akka.stream.FlowShape[I, O], M]) :
          akka.stream.scaladsl.Flow[I, O, M] = { /* compiled code */ }
    */

    val protocolDecoder = new ProtocolDecoder()
    val messageTransformer = new ProtocolMessageTransformer()

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

    peerFlow
  }
}


