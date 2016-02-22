package io.scalechain.blockchain.api.command.help

import io.scalechain.blockchain.api.Services
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{StringResult, RpcError, RpcRequest, RpcResult}

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
  * Parameter #1 : RPC (String, Optional)
  *   The name of the RPC to get help for.
  *   If omitted, display a categorized list of commands.
  *
  * Result: (String)
  *   The help text for the specified RPC or the list of commands.
  *   The scalechain-cli command will parse this text and format it as human-readable text.
  *
  * https://bitcoin.org/en/developer-reference#help
  */
object Help extends RpcCommand {
  val helpForAllCommands =
    """
      |== Blockchain ==
      |getbestblockhash
      |getblock "hash" ( verbose )
      |getblockhash index
      |
      |== Control ==
      |help ( "command" )
      |
      |== Mining ==
      |submitblock "hexdata" ( "jsonparametersobject" )
      |
      |== Network ==
      |getpeerinfo
      |
      |== Rawtransactions ==
      |decoderawtransaction "hexstring"
      |getrawtransaction "txid" ( verbose )
      |sendrawtransaction "hexstring" ( allowhighfees )
      |signrawtransaction "hexstring" ( [{"txid":"id","vout":n,"scriptPubKey":"hex","redeemScript":"hex"},...] ["privatekey1",...] sighashtype )
      |
      |== Wallet ==
      |getaccount "bitcoinaddress"
      |getaccountaddress "account"
      |getnewaddress ( "account" )
      |getrawchangeaddress
      |getreceivedbyaddress "bitcoinaddress" ( minconf )
      |gettransaction "txid" ( includeWatchonly )
      |listtransactions ( "account" count from includeWatchonly)
      |listunspent ( minconf maxconf  ["address",...] )
      |sendfrom "fromaccount" "tobitcoinaddress" amount ( minconf "comment" "comment-to" )
    """.stripMargin

  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    if (request.params.paramValues.length == 0) {
      Right( Some( StringResult(helpForAllCommands) ) )
    } else if (request.params.paramValues.length == 1) {
      val command = request.params.paramValues(0).toString

      val serviceOption = Services.serviceByCommand.get(command)
      if (serviceOption.isDefined) {
        Right( Some( StringResult( serviceOption.get.help ) ) )
      } else {
        Left(RpcError(0, "Invalid command", command))
      }
    } else {
      Left(RpcError(0, "Need a specific command on the rpc parameter", ""))
    }
  }

  def help() : String =
    """help ( "command" )
      |
      |List all commands, or get help for a specified command.
      |
      |Arguments:
      |1. "command"     (string, optional) The command to get help on
      |
      |Result:
      |"text"     (string) The help text
    """.stripMargin
}


