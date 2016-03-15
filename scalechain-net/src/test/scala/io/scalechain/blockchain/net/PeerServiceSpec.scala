package io.scalechain.blockchain.net

import io.scalechain.blockchain.net.processor.TransactionProcessor

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.testkit._
import org.scalatest._

class PeerServiceSpec extends TestKit(ActorSystem("PeerServiceSpec")) with ImplicitSender
with WordSpecLike with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  var peerService : PeerService = null
  var peerBrokerProbe : TestProbe = null

  override def beforeEach() {
    // set-up code
    //
    peerBrokerProbe = TestProbe()
    peerService = new PeerService(peerBrokerProbe.ref)

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