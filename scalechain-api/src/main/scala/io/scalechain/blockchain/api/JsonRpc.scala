package io.scalechain.blockchain.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import spray.json._


// domain model
/*
final case class Item(name: String, id: Long)
final case class Order(items: List[Item])
*/
// by kangmo
final case class TestResponse(message : String)


// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
//  implicit val itemFormat = jsonFormat2(Item)
//  implicit val orderFormat = jsonFormat1(Order) // contains List[Item]
}

// use it wherever json (un)marshalling is needed
trait JsonRpc extends Directives with JsonSupport {

  implicit val testResponseFormat = jsonFormat1(TestResponse.apply)

  // format: OFF
  val routes = {
    pathPrefix("") {
      get {
        complete {
          TestResponse("hello world")
        }
      }
    }
    /*
    ~ get {
      pathSingleSlash {
        complete {
          Item("thing", 42) // will render as JSON
        }
      }
    } ~
    post {
      entity(as[Order]) { order => // will unmarshal JSON to Order
        val itemsCount = order.items.size
        val itemNames = order.items.map(_.name).mkString(", ")
        complete(s"Ordered $itemsCount items: $itemNames")
      }
    }
    */
  // format: ON
  }
}


object JsonRpcMicroservice extends App with JsonRpc {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  Console.readLine() // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.shutdown()) // and shutdown when done
}

