package io.scalechain.blockchain.net

import java.net.{InetAddress, InetSocketAddress}

import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.testkit._
import org.scalatest._

class TestTemplateSpec extends TestKit(ActorSystem("TestTemplateSpec")) with ImplicitSender
with WordSpecLike with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //
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

  "method" should {
    "do something" in {
      /*
            val echo = system.actorOf(TestActors.echoActorProps)
            echo ! "hello world"
            expectMsg("hello world")
      */
    }
  }
}