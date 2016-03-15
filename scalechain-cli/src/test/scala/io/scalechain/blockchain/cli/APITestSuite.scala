package io.scalechain.blockchain.cli

import org.scalatest.ShouldMatchers

class PeerRunner extends Runnable {
  def run() : Unit = {
    ScaleChainPeer.main( Array("-a", "localhost", "-x", "8333") )
  }
}

/**
  * Created by kangmo on 3/15/16.
  */
trait APITestSuite extends ShouldMatchers{
  val peerThread = new Thread( new PeerRunner)

  def startNode(): Unit = {
    peerThread.start()
  }

  def stopNode() : Unit = {
    if ( peerThread.isAlive ) {
      peerThread.stop()
    }
  }
}
