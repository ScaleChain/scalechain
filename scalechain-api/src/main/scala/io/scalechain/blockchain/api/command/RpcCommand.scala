package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

trait RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]]
  def help() : String
}
