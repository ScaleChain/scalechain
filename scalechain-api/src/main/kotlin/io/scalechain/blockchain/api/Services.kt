package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.command.blockchain.*
import io.scalechain.blockchain.api.command.wallet.*
import io.scalechain.blockchain.api.command.control.*
import io.scalechain.blockchain.api.command.generating.*
import io.scalechain.blockchain.api.command.help.*
import io.scalechain.blockchain.api.command.mining.*
import io.scalechain.blockchain.api.command.network.*
import io.scalechain.blockchain.api.command.rawtx.*
import io.scalechain.blockchain.api.command.utility.*
import io.scalechain.blockchain.api.command.wallet.p3.GetTransaction

/** Services has a list of all services currently supported.
  * The list is used for creating a map from the service command (such as "getblock")
  * to the actual Service object (such as GetBlock)
  */
object Services {
  val all = Seq (
    GetBestBlockHash,
    GetBlock,
    GetBlockHash,
    Help,
//    SubmitBlock,
    GetPeerInfo,
    DecodeRawTransaction,
    GetRawTransaction,
    SendRawTransaction,
    ImportAddress,
    GetAccount,
    GetAccountAddress,
    GetNewAddress,
    GetReceivedByAddress,
    ListTransactions,
    ListUnspent,
    SignRawTransaction

    /*
        GetTransaction,
    */
  )

  // The map from the command to the service object.
  // The command equals to the class name (lower case) of the service object.
  // For example, service GetBestBlockHash has command "getbestblockhash"
  //
  // Following steps are the necessary to get the command string from the object singleton.
  //
  // getSimpleName => GetBestBlockHash$
  // toLowerCase   => getbestblockhash$
  // dropRight(1)  => getbestblockhash
  val serviceByCommand = (all.map(_.getClass.getSimpleName.toLowerCase.dropRight(1)) zip all).toMap
}

