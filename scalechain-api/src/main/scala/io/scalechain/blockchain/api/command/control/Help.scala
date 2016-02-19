package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet help help

  CLI output :
    help ( "command" )

    List all commands, or get help for a specified command.

    Arguments:
    1. "command"     (string, optional) The command to get help on

    Result:
    "text"     (string) The help text

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "help", "params": [] }

  Json-RPC response :
    {
      "result": << The CLI output as a string >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Help: lists all available public RPC commands, or gets help for the specified RPC.
  *
  * Commands which are unavailable will not be listed, such as wallet RPCs if wallet support is disabled.
  *
  * https://bitcoin.org/en/developer-reference#help
  */
object Help extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


