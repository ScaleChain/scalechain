package io.scalechain.blockchain.cli.command

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTestRPC
import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTestLayers
import io.scalechain.blockchain.cli.command.stresstests.GenerateAddress
import io.scalechain.blockchain.cli.command.stresstests.GenerateRawTransactions

/**
  * Created by kangmo on 7/28/16.
  */
object Commands {
  val all = listOf (
    GenerateAddress,
    GenerateRawTransactions,
    MultiThreadTestLayers,
    MultiThreadTestRPC
  )

  // The map from the command to the service object.
  val commandMap = (all.map{ it.descriptor.command} zip all).toMap()
}
