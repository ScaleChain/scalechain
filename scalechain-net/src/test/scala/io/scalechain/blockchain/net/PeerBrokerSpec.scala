package io.scalechain.blockchain.net

import io.scalechain.blockchain.net.processor.BlockProcessor

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.testkit._
import org.scalatest._

class PeerBrokerSpec extends TestKit(ActorSystem("PeerBrokerSpec")) with ImplicitSender
with WordSpecLike with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  var actor : ActorRef = null

  override def beforeEach() {
    // set-up code
    //
    actor = system.actorOf(PeerBroker.props)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "actor" should {
    "do something" in {
      /*
            val echo = system.actorOf(TestActors.echoActorProps)
            echo ! "hello world"
            expectMsg("hello world")
      */
    }
  }
}