package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.command.blockchain._
import io.scalechain.blockchain.api.command.wallet._
import io.scalechain.blockchain.api.command.control._
import io.scalechain.blockchain.api.command.generating._
import io.scalechain.blockchain.api.command.help._
import io.scalechain.blockchain.api.command.mining._
import io.scalechain.blockchain.api.command.network._
import io.scalechain.blockchain.api.command.rawtx._
import io.scalechain.blockchain.api.command.utility._

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
    SubmitBlock,
    GetPeerInfo,
    DecodeRawTransaction,
    GetRawTransaction,
    SendRawTransaction,
    SignRawTransaction,
    GetAccount,
    GetAccountAddress,
    GetNewAddress,
    GetReceivedByAddress,
    GetTransaction,
    ListTransactions,
    ListUnspent,
    SendFrom
  )

  // The map from the command to the service object.
  // The command equals to the class name (lower case) of the service object.
  // For example, service GetBestBlockHash has command "getbestblockhash"
  val serviceByCommand = (all.map(_.getClass.getSimpleName.toLowerCase) zip all).toMap
}

