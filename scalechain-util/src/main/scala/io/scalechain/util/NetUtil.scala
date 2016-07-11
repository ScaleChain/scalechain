package io.scalechain.util

import java.net.{InetAddress, NetworkInterface}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by kangmo on 7/11/16.
  */
object NetUtil {
  def getLocalAddresses() : List[String] = {
    val addresses = new ArrayBuffer[String]()
    val e = NetworkInterface.getNetworkInterfaces()
    while(e.hasMoreElements())
    {
      val n : NetworkInterface = e.nextElement()
      val ee = n.getInetAddresses()
      while (ee.hasMoreElements())
      {
        val i : InetAddress = ee.nextElement()
        addresses.append(i.getHostAddress())
      }
    }
    addresses.toList
  }
}
