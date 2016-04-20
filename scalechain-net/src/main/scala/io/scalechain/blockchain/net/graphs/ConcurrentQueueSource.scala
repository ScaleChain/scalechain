package io.scalechain.blockchain.net.graphs

import java.util.concurrent.ConcurrentLinkedQueue

import akka.stream.{Graph, Attributes, Outlet, SourceShape}
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStageWithMaterializedValue, OutHandler, GraphStageLogic}


object ConcurrentQueueSource {
  def create[T] : Source[T, ConcurrentLinkedQueue[T]] = {

    val sourceGraph : Graph[SourceShape[T], ConcurrentLinkedQueue[T]] = new ConcurrentQueueSource[T]()

    Source.fromGraph(sourceGraph)
  }
}

/** A source than materializes a concurrent queue.
  * All elements put into the queue is emit by the source.
  */
class ConcurrentQueueSource[T](outletName : String = "ConcurrentQueueSource")
  extends GraphStageWithMaterializedValue[SourceShape[T], ConcurrentLinkedQueue[T]] {

  val out : Outlet[T] = Outlet(outletName)

  override val shape : SourceShape[T] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes : Attributes) : (GraphStageLogic, ConcurrentLinkedQueue[T]) = {
    val logic = new GraphStageLogic(shape) {
      val queue = new ConcurrentLinkedQueue[T]()
      setHandler( out, new OutHandler {
        override def onPull() : Unit = {
          if (!queue.isEmpty) {
            val value = queue.poll()
            push(out, value)
          }
        }
        // No need to override onDownstreamFinish.
        //
        // onDownstreamFinish() is called once the downstream has cancelled and
        // no longer allows messages to be pushed to it.
        // No more onPull() will arrive after this event.
        // If not overridden this will default to stopping the stage.
      })
    }
    (logic, logic.queue)
  }

}

