package io.scalechain.blockchain.api

import io.scalechain.blockchain.net.PeerSet
import io.scalechain.blockchain.net.service.{MempoolService, PeerService, BlockDatabaseService}
import io.scalechain.blockchain.proto.{Transaction, Hash}

/**
  * Created by kangmo on 3/15/16.
  */
object SubSystem {
  val blockDatabaseService = new BlockDatabaseService()
  val mempoolService = new MempoolService()
  val peerService = new PeerService(PeerSet.get)

  def getTransaction(txHash : Hash ) : Option[Transaction] = {
    // Step 1 : Search mempool.
    val mempoolTransactionOption = SubSystem.mempoolService.getTransaction(txHash)

    // Step 2 : Search block database.
    val dbTransactionOption = SubSystem.blockDatabaseService.getTransaction(txHash)

    // Step 3 : Run validation.

    //BUGBUG : Transaction validation fails because the transaction hash on the outpoint does not exist.
    //mempoolTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )
    //dbTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )

    if ( mempoolTransactionOption.isDefined ) {
      Some(mempoolTransactionOption.get)
    } else if (dbTransactionOption.isDefined){
      Some(dbTransactionOption.get)
    } else {
      None
    }
  }
}


