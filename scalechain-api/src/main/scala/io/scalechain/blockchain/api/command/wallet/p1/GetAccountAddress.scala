package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetAccountAddress: returns the current Bitcoin address for receiving payments to this account.
  * If the account doesn’t exist, it creates both the account and a new address for receiving payment.
  * Once a payment has been received to an address, future calls to this RPC for the same account will return a different address.
  *
  * https://bitcoin.org/en/developer-reference#getaccountaddress
  */
object GetAccountAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


