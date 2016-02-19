package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** BackupWallet: safely copies wallet.dat to the specified file, which can be a directory or a path with filename.
  *
  * https://bitcoin.org/en/developer-reference#backupwallet
  */
object BackupWallet extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


