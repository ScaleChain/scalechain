package io.scalechain.blockchain.cli.command

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTestRPC
import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTestLayers
import io.scalechain.blockchain.cli.command.stresstests.GenerateAddress
import io.scalechain.blockchain.cli.command.stresstests.GenerateRawTransactions

/**
  * Created by kangmo on 7/28/16.
  */
object Commands {
  val all = Seq (
    GenerateAddress,
    GenerateRawTransactions,
    MultiThreadTestLayers,
    MultiThreadTestRPC
  )

  // The map from the command to the service object.
  // The command equals to the class name (lower case) of the command object.
  // For example, service GenerateAddress has command "generateaddress"
  //
  // Following steps are the necessary to get the command string from the object singleton.
  //
  // getSimpleName => GenerateAddress$
  // toLowerCase   => generateaddress$
  // dropRight(1)  => generateaddress
  val commandMap = (all.map(_.getClass.getSimpleName.toLowerCase.dropRight(1)) zip all).toMap
}
