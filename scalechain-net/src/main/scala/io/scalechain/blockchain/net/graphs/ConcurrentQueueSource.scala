package io.scalechain.blockchain.net.graphs

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}

import akka.stream.{Graph, Attributes, Outlet, SourceShape}
import akka.stream.scaladsl._
import akka.stream.stage.{AsyncCallback, GraphStageWithMaterializedValue, OutHandler, GraphStageLogic}

import scala.concurrent.{Future}
import io.scalechain.util.ExecutionContextUtil

object ConcurrentQueueSource {
  // TODO : Tune the number of threads in the execution contexts
  val executionContext = ExecutionContextUtil.createContext(threadCount = 16)

  def create[T] : Source[T, LinkedBlockingQueue[T]] = {

    val sourceGraph : Graph[SourceShape[T], LinkedBlockingQueue[T]] = new ConcurrentQueueSource[T]()

    Source.fromGraph(sourceGraph)
  }
}
/** A source than materializes a concurrent queue.
  * All elements put into the queue is emit by the source.
  */
class ConcurrentQueueSource[T](outletName : String = "ConcurrentQueueSource")
  extends GraphStageWithMaterializedValue[SourceShape[T], LinkedBlockingQueue[T]] {

  val out : Outlet[T] = Outlet(outletName)

  override val shape : SourceShape[T] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes : Attributes) : (GraphStageLogic, LinkedBlockingQueue[T]) = {

    val logic = new GraphStageLogic(shape) {
      var onQueueItem : AsyncCallback[T] = null

      override def preStart(): Unit = {
        onQueueItem = getAsyncCallback[T]({ value : T =>
          push(out, value)
        })
      }

      // BUGBUG : The queue is unbound. Should we limit the size of the queue to avoid NotEnoughMemoryException?
      val queue = new LinkedBlockingQueue[T]()

      setHandler( out, new OutHandler {
        override def onPull() : Unit = {
          if (queue.isEmpty) { // The queue is empty.
            implicit val ec = ConcurrentQueueSource.executionContext
            Future {
              // Wait for an item forever.
              queue.poll(Integer.MAX_VALUE, TimeUnit.DAYS )
            } onSuccess {
              // When an item was arrived, invoke the onQueueItem callback, which pushes the queue item.
              case value => onQueueItem.invoke(value)
            }
          } else {
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

