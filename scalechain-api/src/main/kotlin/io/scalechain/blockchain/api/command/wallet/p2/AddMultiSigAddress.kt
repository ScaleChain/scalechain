package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Adding a 2-of-3 P2SH multisig address to the “test account” by mixing two P2PKH addresses and one full public key:
    bitcoin-cli -testnet addmultisigaddress \
      2 \
      '''
        [
          "mjbLRSidW1MY8oubvs4SMEnHNFXxCcoehQ",
          "02ecd2d250a76d204011de6bc365a56033b9b3a149f679bc17205555d3c2b2854f",
          "mt17cV37fBqZsnMmrHnGCm9pM28R1kQdMG"
        ]
      ''' \
      'test account'

  CLI output :
    2MyVxxgNBk5zHRPRY2iVjGRJHYZEp1pMCSq

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "addmultisigaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** AddMultiSigAddress: adds a P2SH multisig address to the wallet.
  *
  * https://bitcoin.org/en/developer-reference#addmultisigaddress
  */
object AddMultiSigAddress extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """addmultisigaddress nrequired ["key",...] ( "account" )
      |
      |Add a nrequired-to-sign multisignature address to the wallet.
      |Each key is a Bitcoin address or hex-encoded public key.
      |If 'account' is specified (DEPRECATED), assign address to that account.
      |
      |Arguments:
      |1. nrequired        (numeric, required) The number of required signatures out of the n keys or addresses.
      |2. "keysobject"   (string, required) A json array of bitcoin addresses or hex-encoded public keys
      |     [
      |       "address"  (string) bitcoin address or hex-encoded public key
      |       ...,
      |     ]
      |3. "account"      (string, optional) DEPRECATED. An account to assign the addresses to.
      |
      |Result:
      |"bitcoinaddress"  (string) A bitcoin address associated with the keys.
      |
      |Examples:
      |
      |Add a multisig address from 2 addresses
      |> bitcoin-cli addmultisigaddress 2 "[\"16sSauSf5pF2UkUwvKGq4qjNRzBZYqgEL5\",\"171sgjn4YtPu27adkKGrdDwzRTxnRkBfKV\"]"
      |
      |As json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "addmultisigaddress", "params": [2, "[\"16sSauSf5pF2UkUwvKGq4qjNRzBZYqgEL5\",\"171sgjn4YtPu27adkKGrdDwzRTxnRkBfKV\"]"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


