package io.scalechain.blockchain.cli

import io.scalechain.blockchain.proto.{IPv6Address, NetworkAddress, Version, Ping}
import io.scalechain.util.HexUtil._

import scala.concurrent.duration._
import akka.util
import akka.pattern.ask

/** A testing app for checking if a ScaleChain server is working well.
  */
object ScaleChainClient extends App {
val clientKernel = new ClientKernel
implicit val timeout = util.Timeout(5 seconds)
//  clientKernel.mediator ? (clientKernel.clientProducer, Ping(123))

// Send Version message
//val version = Version(70012, BigInt("5"), 1454764863L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff42807623")), 36128), NetworkAddress(BigInt("5"), IPv6Address(bytes("00000000000000000000ffff00000000")), 36128), BigInt("11546941380556780170"), "/Kimtoshi:0.12.99/", 396491, true)
val version = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

  clientKernel.mediator ? (clientKernel.clientProducer, version)
}
