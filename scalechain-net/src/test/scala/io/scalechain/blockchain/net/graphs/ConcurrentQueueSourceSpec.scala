package io.scalechain.blockchain.net.graphs

import java.util.concurrent.LinkedBlockingQueue

import akka.actor._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit._
import org.scalatest._

import scala.concurrent.Future

class ConcurrentQueueSourceSpec extends TestKit(ActorSystem("TestTemplateSpec")) with ImplicitSender
with WordSpecLike with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  implicit val mat = ActorMaterializer()

  var source : Source[Int, LinkedBlockingQueue[Int]] = null
  var queue : LinkedBlockingQueue[Int] = null
  var subscriber : TestSubscriber.Probe[Int] = null


  override def beforeEach() {
    // set-up code
    //
    source = ConcurrentQueueSource.create[Int]
    val (aQueue, aSubscriber) = source.toMat(TestSink.probe[Int])(Keep.both).run()
    queue = aQueue
    subscriber = aSubscriber

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
    source = null
    queue = null
    subscriber = null
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "source" should {
    "emit existing elements on queue" in {
      queue.offer(1) shouldBe true
      queue.offer(2) shouldBe true
      subscriber.request(2)
      subscriber.expectNext(1,2)
    }
  }

  "source" should {
    "wait for elements on queue to emit them" in {
      subscriber.request(2)

      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        Thread.sleep(50)
        queue.offer(1) shouldBe true
        queue.offer(2) shouldBe true
      }

      subscriber.expectNext(1,2)
    }
  }

}