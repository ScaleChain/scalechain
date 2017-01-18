package io.scalechain.blockchain.api.command.rawtx

import io.scalechain.blockchain.net.RpcSubSystem
import io.scalechain.blockchain.api.command.TransactionFormatter
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBlock
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import io.scalechain.util.Either
import io.scalechain.util.Either.Right

/*
  CLI command :
    bitcoin-cli -testnet getrawtransaction \
      ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e

  CLI output(wrapped) :
    0100000001268a9ad7bfb21d3c086f0ff28f73a064964aa069ebb69a9e437da8\
    5c7e55c7d7000000006b483045022100ee69171016b7dd218491faf6e13f53d4\
    0d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed28\
    8d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc\
    8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326ffffffff0350ac60020000\
    00001976a91456847befbd2360df0e35b4e3b77bae48585ae06888ac80969800\
    000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac002d\
    3101000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac\
    00000000

  CLI command :
    bitcoin-cli -testnet getrawtransaction \
      ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e 1

  CLI output :
    {
        "hex" : "0100000001268a9ad7bfb21d3c086f0ff28f73a064964aa069ebb69a9e437da85c7e55c7d7000000006b483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326ffffffff0350ac6002000000001976a91456847befbd2360df0e35b4e3b77bae48585ae06888ac80969800000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac002d3101000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000",
        "txid" : "ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e",
        "version" : 1,
        "locktime" : 0,
        "vin" : [
            {
                "txid" : "d7c7557e5ca87d439e9ab6eb69a04a9664a0738ff20f6f083c1db2bfd79a8a26",
                "vout" : 0,
                "scriptSig" : {
                    "asm" : "3045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc001 03a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326",
                    "hex" : "483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326"
                },
                "sequence" : 4294967295
            }
        ],
        "vout" : [
            {
                "value" : 0.39890000,
                "n" : 0,
                "scriptPubKey" : {
                    "asm" : "OP_DUP OP_HASH160 56847befbd2360df0e35b4e3b77bae48585ae068 OP_EQUALVERIFY OP_CHECKSIG",
                    "hex" : "76a91456847befbd2360df0e35b4e3b77bae48585ae06888ac",
                    "reqSigs" : 1,
                    "type" : "pubkeyhash",
                    "addresses" : [
                        "moQR7i8XM4rSGoNwEsw3h4YEuduuP6mxw7"
                    ]
                }
            },
            {
                "value" : 0.10000000,
                "n" : 1,
                "scriptPubKey" : {
                    "asm" : "OP_DUP OP_HASH160 2b14950b8d31620c6cc923c5408a701b1ec0a020 OP_EQUALVERIFY OP_CHECKSIG",
                    "hex" : "76a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac",
                    "reqSigs" : 1,
                    "type" : "pubkeyhash",
                    "addresses" : [
                        "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN"
                    ]
                }
            },
            {
                "value" : 0.20000000,
                "n" : 2,
                "scriptPubKey" : {
                    "asm" : "OP_DUP OP_HASH160 0dfc8bafc8419853b34d5e072ad37d1a5159f584 OP_EQUALVERIFY OP_CHECKSIG",
                    "hex" : "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
                    "reqSigs" : 1,
                    "type" : "pubkeyhash",
                    "addresses" : [
                        "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"
                    ]
                }
            }
        ],
        "blockhash" : "00000000103e0091b7d27e5dc744a305108f0c752be249893c749e19c1c82317",
        "confirmations" : 88192,
        "time" : 1398734825,
        "blocktime" : 1398734825
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getrawtransaction", "params": ["ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e"] }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getrawtransaction", "params": ["ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e", 1] }


  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

data class RawTransaction(
  // The serialized, hex-encoded data for 'txid'
  val hex           : String, // "0100000001268a9ad7bfb21d3c086f0ff28f73a064964aa069ebb69a9e437da85c7e55c7d7000000006b483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326ffffffff0350ac6002000000001976a91456847befbd2360df0e35b4e3b77bae48585ae06888ac80969800000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac002d3101000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000"
  // The transaction’s TXID encoded as hex in RPC byte order
  val txid          : Hash,   // "ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e",
  // The transaction format version number
  val version       : Int,    // 1,
  // The transaction’s locktime: either a Unix epoch date or block height; see the Locktime parsing rules
  val locktime      : Long,   // 0,
  // An array of objects with each object being an input vector (vin) for this transaction.
  // Input objects will have the same order within the array as they have in the transaction,
  // so the first input listed will be input 0
  val vin           : List<RawTransactionInput>,
  // An array of objects each describing an output vector (vout) for this transaction.
  // Output objects will have the same order within the array as they have in the transaction,
  // so the first output listed will be output 0
  val vout          : List<RawTransactionOutput>,
  // If the transaction has been included in a block on the local best block chain,
  // this is the hash of that block encoded as hex in RPC byte order
  val blockhash     : Hash?,   // "00000000103e0091b7d27e5dc744a305108f0c752be249893c749e19c1c82317",
  // If the transaction has been included in a block on the local best block chain,
  // this is how many confirmations it has. Otherwise, this is 0
  val confirmations : Long,   // 88192,
  // If the transaction has been included in a block on the local best block chain,
  // this is the block header time of that block (may be in the future)
  val time          : Long?,   // 1398734825,
  // This field is currently identical to the time field described above
  val blocktime     : Long?   // 1398734825
) : RpcResult

/** GetRawTransaction: gets a hex-encoded serialized transaction or a JSON object describing the transaction.
  * By default, Bitcoin Core only stores complete transaction data for UTXOs and your own transactions,
  * so the RPC may fail on historic transactions unless you use the non-default txindex=1 in your Bitcoin Core startup settings.
  *
  * Parameter #1 : TXID (String;hex, Required)
  *   The TXID of the transaction to get, encoded as hex in RPC byte order.
  *
  * Parameter #1 : Verbose (Number;int[0,1], Optional)
  *   Set to 0 (the default) to return the serialized transaction as hex.
  *   Set to 1 to return a decoded transaction.
  *
  * Result: (null)
  *   If the transaction wasn’t found, the result will be JSON null.
  *   This can occur because the transaction doesn’t exist in the block chain or memory pool,
  *   or because it isn’t part of the transaction index. See the help entry for -txindex
  *
  * Result: (String;hex) (if Verbose = 0 )
  *   If the transaction was found, this will be the serialized transaction encoded as hex.
  *
  * Result: (Object) (if Verbose = 1 )
  *   If the transaction was found, this will be an object describing it.
  *
  * https://bitcoin.org/en/developer-reference#getrawtransaction
  */
object GetRawTransaction : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
      return handlingException {
          val txHashString: String = request.params.get<String>("TXID", 0)
          val verbose: java.math.BigDecimal = request.params.getOption<java.math.BigDecimal>("Verbose", 1) ?: java.math.BigDecimal(0)

          val txHash = getHash(txHashString, 32)

          val transactionOption = RpcSubSystem.get().getTransaction(txHash)
          val bestBlockHeight: Long = RpcSubSystem.get().getBestBlockHeight()
          val blockInfoOption = RpcSubSystem.get().getTransactionBlockInfo(txHash)

          val rawTransactionOption =
              if (transactionOption == null) null
              else {
                  if (verbose != java.math.BigDecimal(0)) {
                      TransactionFormatter.getRawTransaction(transactionOption, bestBlockHeight, blockInfoOption)
                  } else {
                      StringResult(TransactionFormatter.getSerializedTranasction(transactionOption))
                  }
              }

          // If the transaction wasn’t found, the result will be JSON null.
          // This can occur because the transaction doesn’t exist in the block chain or memory pool,
          // or because it isn’t part of the transaction index. See the Bitcoin Core -help entry for -txindex

          Right(rawTransactionOption)
      }
  }

  override fun help() : String =
    """getrawtransaction "txid" ( verbose )
      |
      |NOTE: By default this function only works sometimes. This is when the tx is in the mempool
      |or there is an unspent output in the utxo for this transaction. To make it always work,
      |you need to maintain a transaction index, using the -txindex command line option.
      |
      |Return the raw transaction data.
      |
      |If verbose=0, returns a string that is serialized, hex-encoded data for 'txid'.
      |If verbose is non-zero, returns an Object with information about 'txid'.
      |
      |Arguments:
      |1. "txid"      (string, required) The transaction id
      |2. verbose       (numeric, optional, default=0) If 0, return a string, other return a json object
      |
      |Result (if verbose is not set or set to 0):
      |"data"      (string) The serialized, hex-encoded data for 'txid'
      |
      |Result (if verbose > 0):
      |{
      |  "hex" : "data",       (string) The serialized, hex-encoded data for 'txid'
      |  "txid" : "id",        (string) The transaction id (same as provided)
      |  "size" : n,             (numeric) The transaction size
      |  "version" : n,          (numeric) The version
      |  "locktime" : ttt,       (numeric) The lock time
      |  "vin" : [               (array of json objects)
      |     {
      |       "txid": "id",    (string) The transaction id
      |       "vout": n,         (numeric)
      |       "scriptSig": {     (json object) The script
      |         "asm": "asm",  (string) asm
      |         "hex": "hex"   (string) hex
      |       },
      |       "sequence": n      (numeric) The script sequence number
      |     }
      |     ,...
      |  ],
      |  "vout" : [              (array of json objects)
      |     {
      |       "value" : x.xxx,            (numeric) The value in BTC
      |       "n" : n,                    (numeric) index
      |       "scriptPubKey" : {          (json object)
      |         "asm" : "asm",          (string) the asm
      |         "hex" : "hex",          (string) the hex
      |         "reqSigs" : n,            (numeric) The required sigs
      |         "type" : "pubkeyhash",  (string) The type, eg 'pubkeyhash'
      |         "addresses" : [           (json array of string)
      |           "bitcoinaddress"        (string) bitcoin address
      |           ,...
      |         ]
      |       }
      |     }
      |     ,...
      |  ],
      |  "blockhash" : "hash",   (string) the block hash
      |  "confirmations" : n,      (numeric) The confirmations
      |  "time" : ttt,             (numeric) The transaction time in seconds since epoch (Jan 1 1970 GMT)
      |  "blocktime" : ttt         (numeric) The block time in seconds since epoch (Jan 1 1970 GMT)
      |}
      |
      |Examples:
      |> bitcoin-cli getrawtransaction "mytxid"
      |> bitcoin-cli getrawtransaction "mytxid" 1
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getrawtransaction", "params": ["mytxid", 1] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


