package io.scalechain.blockchain.api.command.rawtx

import io.scalechain.blockchain.api.RpcSubSystem
import io.scalechain.blockchain.api.command.TransactionDecoder
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.*
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.HashFormat
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionVerifier

/*
  CLI command :
    # Broadcast a signed transaction
    bitcoin-cli -testnet sendrawtransaction 01000000011da9283b4ddf8d\
      89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a4730\
      4402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a\
      0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405\
      ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a\
      2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc84198\
      53b34d5e072ad37d1a5159f58488ac0000000

  CLI output :
    f5a5ce5988cc72b9b90e8d1d6c910cda53c88d2175177357cc2f2cf0899fbaad

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "sendrawtransaction", "params": ["01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a47304402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac0000000"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SendRawTransaction: validates a transaction and broadcasts it to the peer-to-peer network.
  *
  * Parameter #1 : Transaction (String;hex, Required)
  *   The serialized transaction to broadcast encoded as hex.
  *
  * Parameter #2 : Allow High Fees (Boolean, Optional)
  *   Set to true to allow the transaction to pay a high transaction fee.
  *   Set to false (the default) to prevent ScaleChain from broadcasting the transaction if it includes a high fee.
  *   Transaction fees are the sum of the inputs minus the sum of the outputs,
  *   so this high fees check helps ensures user including a change address
  *   to return most of the difference back to themselves.
  *
  * Result: (String;hex)
  *   If the transaction was accepted by the node for broadcast,
  *   this will be the TXID of the transaction encoded as hex in RPC byte order.
  *
  * Result: (null)
  *   If the transaction was rejected by the node, this will set to null,
  *   the JSON-RPC error field will be set to a code,
  *   and the JSON-RPC message field may contain an informative error message.
  *
  * https://bitcoin.org/en/developer-reference#sendrawtransaction
  */
object SendRawTransaction : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {

    handlingException {
      val serializedTransaction  : String  = request.params.get<String>("Transaction", 0)
      val allowHighFees: Boolean = request.params.getOption<Boolean>("Allow High Fees", 1).getOrElse(false)

      // Step 1 : Decode the transaction and run validation.
      val transactions : List<Transaction> = TransactionDecoder.decodeTransactions(serializedTransaction)

      // If the transaction already exists, the tx hash is put into the txHashes list as Left(hash)
      // If the transaction successfully sent, the tx hash is put into the txHashes list as Right(hash)
      val txHashes : List< Either<Hash,Hash> > = transactions.map { tx: Transaction =>

        RpcSubSystem.get.verifyTransaction(tx)

        // Step 2 : Check if the transaction already exists.
        val txHash = tx.hash
        val transactionOption = RpcSubSystem.get.getTransaction(txHash)

        if (transactionOption.isDefined) {
          Right(txHash)
        } else {
          RpcSubSystem.get.sendRawTransaction(tx, allowHighFees)
          Right(txHash)
        }
      }
      if (txHashes.count( _.isLeft ) > 0) {
        // BUGBUG : check bitcoin core code to make sure the error code matches.
        val txIds = txHashes.filter(_.isLeft).mkString(",")
        Left(RpcError(
              RpcError.RPC_INVALID_PARAMETER.code,
              RpcError.RPC_INVALID_PARAMETER.messagePrefix,
              "The transaction already exists. Transaction ID(s): " + txIds))
      } else {
        if (txHashes.length == 1) {
          // To keep the response compatible with bitcoind,
          // return as a single StringResult if only one transaction was provided.
          val txHash : Hash = txHashes.map(_.right.get).head
          Right(Some(StringResult(ByteArray.byteArrayToString(txHash.value))))
        } else {
          // return as a StringListResult if more than one transaction was privided.
          val txHashStringList = txHashes.map(_.right.get).map{ txHash : Hash =>
            ByteArray.byteArrayToString(txHash.value)
          }
          Right(Some(StringListResult(txHashStringList)))
        }
      }
    }
  }

  fun help() : String =
    """sendrawtransaction "hexstring" ( allowhighfees )
      |
      |Submits raw transaction (serialized, hex-encoded) to local node and network.
      |
      |Also see createrawtransaction and signrawtransaction calls.
      |
      |Arguments:
      |1. "hexstring"    (string, required) The hex string of the raw transaction)
      |2. allowhighfees    (boolean, optional, default=false) Allow high fees
      |
      |Result:
      |"hex"             (string) The transaction hash in hex
      |
      |Examples:
      |
      |Create a transaction
      |> bitcoin-cli createrawtransaction "[{\"txid\" : \"mytxid\",\"vout\":0}]" "{\"myaddress\":0.01}"
      |Sign the transaction, and get back the hex
      |> bitcoin-cli signrawtransaction "myhex"
      |
      |Send the transaction (signed hex)
      |> bitcoin-cli sendrawtransaction "signedhex"
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "sendrawtransaction", "params": ["signedhex"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin

}


