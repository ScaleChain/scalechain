package io.scalechain.util

import com.typesafe.config.{ConfigFactory}
import scala.collection.JavaConverters._

object Config extends Config(ConfigFactory.load("scalechain")) {
}

case class PeerAddress(address : String, port : Int)

class Config(config : com.typesafe.config.Config) {
  def hasPath(path : String) = config.hasPath(path)
  def getInt(path: String) = config.getInt(path)
  def getString(path: String) = config.getString(path)
  def getConfigList(path : String) = config.getConfigList(path)
  var privateCache : Option[Boolean] = None

  def isPrivate : Boolean = {
    if (privateCache.isDefined) {
      privateCache.get
    }
    else {
      val configValue =
        if ( config.hasPath("scalechain.private") ) {
          true
        } else {
          false
        }
      privateCache = Some(configValue)
      configValue
    }
  }

  def peerAddresses() : List[PeerAddress] = {
    io.scalechain.util.Config.getConfigList("scalechain.p2p.peers").asScala.toList.map { peer =>
      PeerAddress(peer.getString("address"), peer.getInt("port"))
    }
  }

  // reaching InitialSetupBlocks height, a node in the private blockchain take turns to mine coins to use for block signing.
  val InitialSetupBlocks = 32L
}