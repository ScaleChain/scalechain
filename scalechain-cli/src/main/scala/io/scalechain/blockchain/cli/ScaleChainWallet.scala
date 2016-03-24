package io.scalechain.blockchain.cli

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.scalechain.blockchain.api.JsonRpc
import io.scalechain.blockchain.storage.Storage
import io.scalechain.util.Config

/**
  * Created by mijeong on 2016. 3. 25..
  */
object ScaleChainWallet extends JsonRpc {

  def main(args: Array[String]) = {
    runService()

    def runService() = {
      implicit val system = ActorSystem("wallet-system")
      implicit val materializer = ActorMaterializer()
      implicit val ec = system.dispatcher

      val port = Config.scalechain.getInt("scalechain.api.port")
      val bindingFuture = Http().bindAndHandle(routes, "localhost", port)

      Storage.initialize()

      println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
      Console.readLine() // for the future transformations
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ ⇒ system.shutdown()) // and shutdown when done
    }
  }
}
