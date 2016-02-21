package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcError, RpcResult, RpcRequest}

trait RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult]
  def help() : String
}
