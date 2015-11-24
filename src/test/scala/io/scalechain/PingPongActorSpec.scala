package io.scalechain

import java.math.BigInteger

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import io.scalechain.blockchain.proto.{PingMessage, PongMessage}
import org.scalatest.{FlatSpec, WordSpecLike, Matchers, BeforeAndAfterAll}

class PingPongActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("MySpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "A Ping actor" must {
    "send back a ping on a pong" in {
      val pingActor = system.actorOf(PingActor.props)
      pingActor ! PongMessage(BigInteger.valueOf(1))
      expectMsg(PingMessage(BigInteger.valueOf(2)))
    }
  }

  "A Pong actor" must {
    "send back a pong on a ping" in {
      val pongActor = system.actorOf(PongActor.props)
      pongActor ! PingMessage(BigInteger.valueOf(1))
      expectMsg(PongMessage(BigInteger.valueOf(1)))
    }
  }

}

