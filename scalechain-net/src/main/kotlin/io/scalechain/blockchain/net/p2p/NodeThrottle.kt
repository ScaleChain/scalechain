package io.scalechain.blockchain.net.p2p

import org.slf4j.Logger


/**
  * Created by kangmo on 8/7/16.
  */
object NodeThrottle {
  /**
    * Netty uses direct memory for receiving messages. If peers send messages faster than this node can process,
    * The direct memory fills up. We need to wait for messages to be handled so that the direct memory is freed.
    *
    * @param logger The logger of the module that is calling this method.
    */
  fun throttle(logger : Logger): Unit {

    // BUGBUG : Update the code to log direct memory.

    var maxMemory : Long = Runtime.getRuntime().totalMemory()
    var freeMemory : Long = Runtime.getRuntime().freeMemory()
    logger.trace("Checking Memory Usage. Max memory : ${maxMemory}, Free memory : ${freeMemory}")

  /*
    var maxDiectMemory : Long = sun.misc.VM.maxDirectMemory()
    var directMemoryUsed : Long = sun.misc.SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed()
    logger.trace("Checking Direct Memory Usage. Max memory : ${maxDiectMemory}, Used memory : ${directMemoryUsed}")
 */
    // http://stackoverflow.com/questions/20058489/is-there-a-way-to-measure-direct-memory-usage-in-java

    /*
    while(directMemoryUsed >= maxDiectMemory - 256000000) {
      Thread.sleep(1000)

      logger.warn("Not enough direct memory for receiving messages. Sleeping 1 second. Max memory : ${maxDiectMemory}, Used memory : ${directMemoryUsed}")

      maxDiectMemory = sun.misc.VM.maxDirectMemory()

      directMemoryUsed = sun.misc.SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed()
    }*/
  }
}
