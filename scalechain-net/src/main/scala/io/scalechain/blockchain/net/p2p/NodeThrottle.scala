package io.scalechain.blockchain.net.p2p

import com.typesafe.scalalogging.Logger

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
  def throttle(logger : Logger): Unit = {
    var maxDiectMemory : Long = sun.misc.VM.maxDirectMemory()
    var directMemoryUsed : Long = sun.misc.SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed()
    logger.trace(s"Checking Direct Memory Usage. Max memory : ${maxDiectMemory}, Used memory : ${directMemoryUsed}")
    // http://stackoverflow.com/questions/20058489/is-there-a-way-to-measure-direct-memory-usage-in-java
    while(directMemoryUsed >= maxDiectMemory - 256000000) {
      Thread.sleep(1000)

      logger.warn(s"Not enough direct memory for receiving messages. Sleeping 1 second. Max memory : ${maxDiectMemory}, Used memory : ${directMemoryUsed}")

      maxDiectMemory = sun.misc.VM.maxDirectMemory()

      directMemoryUsed = sun.misc.SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed()
    }
  }
}
