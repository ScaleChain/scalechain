package io.scalechain.blockchain.api.command.mining.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # CLI command parameters are from https://en.bitcoin.it/wiki/Getblocktemplate
    bitcoin-cli -testnet getblocktemplate '{"capabilities": ["coinbasetxn", "workid", "coinbase/append"]}'

  CLI output :
    {
      "coinbasetxn": {
       "data": "0100000001000000000000000000000000000000000000000000000000000000
      0000000000ffffffff1302955d0f00456c6967697573005047dc66085fffffffff02fff1052a01
      0000001976a9144ebeb1cd26d6227635828d60d3e0ed7d0da248fb88ac01000000000000001976
      a9147c866aee1fa2f3b3d5effad576df3dbf1f07475588ac00000000"
      },
      "previousblockhash": "000000004d424dec1c660a68456b8271d09628a80cc62583e5904f5894a2483c",
      "transactions": [],
      "expires": 120,
      "target": "00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "longpollid": "some gibberish",
      "height": 23957,
      "version": 2,
      "curtime": 1346886758,
      "mutable": ["coinbase/append"],
      "bits": "ffff001d"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblocktemplate", "params": [{"capabilities": ["coinbasetxn", "workid", "coinbase/append"]}] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBlockTemplate: gets a block template or proposal for use with mining software.
  *
  * https://bitcoin.org/en/developer-reference#getblocktemplate
  */
object GetBlockTemplate extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


