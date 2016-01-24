package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcResult, RpcRequest}

trait RpcCommand {
  def invoke(request : RpcRequest) : RpcResult
}
