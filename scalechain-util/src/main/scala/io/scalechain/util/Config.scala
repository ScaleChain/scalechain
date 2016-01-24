package io.scalechain.util

import com.typesafe.config.ConfigFactory


object Config {
  val scalechain = ConfigFactory.load("scalechain")
}

