package io.scalechain.util

import com.typesafe.config.{ConfigFactory}


object Config extends Config(ConfigFactory.load("scalechain")) {
}

class Config(config : com.typesafe.config.Config) {
  def hasPath(path : String) = config.hasPath(path)
  def getInt(path: String) = config.getInt(path)
  def getString(path: String) = config.getString(path)
  def getConfigList(path : String) = config.getConfigList(path)
  var privateCache : Option[Boolean] = None
  def isPrivate : Boolean = {
    if (privateCache.isDefined)
      privateCache.get
    else {
      val configValue =
        if ( config.hasPath("private") ) {
          true
        } else {
          false
        }
      privateCache = Some(configValue)
      configValue
    }
  }
}