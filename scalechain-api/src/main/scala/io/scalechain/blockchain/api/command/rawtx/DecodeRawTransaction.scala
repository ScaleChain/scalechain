package io.scalechain.blockchain.api.command.rawtx

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash._
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.{HashFormat, Hash}
import spray.json._
import spray.json.DefaultJsonProtocol._

/*
  CLI command :
    bitcoin-cli -testnet decoderawtransaction 0100000001268a9ad7bfb2\
      1d3c086f0ff28f73a064964aa069ebb69a9e437da85c7e55c7d7000000006b48\
      3045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560f\
      eb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd4\
      5a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b\
      3118f3db16cbbcf8f326ffffffff0350ac6002000000001976a91456847befbd\
      2360df0e35b4e3b77bae48585ae06888ac80969800000000001976a9142b1495\
      0b8d31620c6cc923c5408a701b1ec0a02088ac002d3101000000001976a9140d\
      fc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000

  CLI output :
    {
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
        ]
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "decoderawtransaction", "params": ["0100000001268a9ad7bfb21d3c086f0ff28f73a064964aa069ebb69a9e437da85c7e55c7d7000000006b483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326ffffffff0350ac6002000000001976a91456847befbd2360df0e35b4e3b77bae48585ae06888ac80969800000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac002d3101000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class RawScriptSig(
  // The signature script in decoded form with non-data-pushing opcodes listed
  asm : String, // "3045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc001 03a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326",
  // The signature script encoded as hex
  hex : String  // "483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326"
)

trait RawTransactionInput

case class RawNormalTransactionInput(
  // The TXID of the outpoint being spent, encoded as hex in RPC byte order. Not present if this is a coinbase transaction
  txid      : Hash,          // "d7c7557e5ca87d439e9ab6eb69a04a9664a0738ff20f6f083c1db2bfd79a8a26",

  // The output index number (vout) of the outpoint being spent.
  // The first output in a transaction has an index of 0.
  // Not present if this is a coinbase transaction
  vout      : Int,          // 0,

  // An object describing the signature script of this input.
  // Not present if this is a coinbase transaction
  scriptSig : RawScriptSig,

  // The input sequence number
  sequence  : Long          // 4294967295
) extends RawTransactionInput


case class RawGenerationTransactionInput(
  // The coinbase (similar to the hex field of a scriptSig) encoded as hex. Only present if this is a coinbase transaction
  coinbase  : String,

  // The input sequence number
  sequence  : Long          // 4294967295
) extends RawTransactionInput

object RawTransactionInputJsonFormat {
  import HashFormat._

  implicit val implicitRawScriptSig                  = jsonFormat2(RawScriptSig.apply)

  implicit val implicitRawGenerationTransactionInput = jsonFormat2(RawGenerationTransactionInput.apply)
  implicit val implicitRawNormalTransactionInput     = jsonFormat4(RawNormalTransactionInput.apply)

  implicit object rawTransactionInputJsonFormat extends RootJsonFormat[RawTransactionInput] {
    def write(txInput : RawTransactionInput) = txInput match {
      case tx : RawGenerationTransactionInput => tx.toJson
      case tx : RawNormalTransactionInput     => tx.toJson
    }

    // Not used.
    def read(value:JsValue) = {
      assert(false)
      null
    }
  }
}


case class RawScriptPubKey(
  // The pubkey script in decoded form with non-data-pushing opcodes listed
  asm       : String,      // "OP_DUP OP_HASH160 56847befbd2360df0e35b4e3b77bae48585ae068 OP_EQUALVERIFY OP_CHECKSIG",

  // The pubkey script encoded as hex
  hex       : String,      // "76a91456847befbd2360df0e35b4e3b77bae48585ae06888ac",

  // The number of signatures required; this is always 1 for P2PK, P2PKH,
  // and P2SH (including P2SH multisig because the redeem script is not available in the pubkey script).
  // It may be greater than 1 for bare multisig.
  // This value will not be returned for nulldata or nonstandard script types
  reqSigs   : Option[Int], // 1,

  // The type of script. This will be one of the following:
  // • pubkey for a P2PK script
  // • pubkeyhash for a P2PKH script
  // • scripthash for a P2SH script
  // • multisig for a bare multisig script
  // • nulldata for nulldata scripts
  // • nonstandard for unknown scripts
  `type`    : Option[String],  //"pubkeyhash",

  // The P2PKH or P2SH addresses used in this transaction, or the computed P2PKH address of any pubkeys in this transaction.
  // This array will not be returned for nulldata or nonstandard script types
  //
  // addresses item : A P2PKH or P2SH address
  addresses : List[String] //["moQR7i8XM4rSGoNwEsw3h4YEuduuP6mxw7"]
)




case class RawTransactionOutput(
  // The number of bitcoins paid to this output. May be 0
  value        : scala.math.BigDecimal, // 0.39890000
  // The output index number of this output within this transaction
  n            : Int,                   // 0
  // An object describing the pubkey script
  scriptPubKey : RawScriptPubKey
)

case class DecodedRawTransaction(
  // The transaction’s TXID encoded as hex in RPC byte order
  txid     : Hash, // "ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e",

  // The transaction format version number
  version  : Int, // 1,

  // The transaction’s locktime: either a Unix epoch date or block height; see the Locktime parsing rules
  locktime : Long, // 0,

  // An array of objects with each object being an input vector (vin) for this transaction.
  // Input objects will have the same order within the array as they have in the transaction,
  // so the first input listed will be input 0
  // vin item : An object describing one of this transaction’s inputs. May be a regular input or a coinbase
  vin      : List[RawTransactionInput],

  // An array of objects each describing an output vector (vout) for this transaction.
  // Output objects will have the same order within the array as they have in the transaction,
  // so the first output listed will be output 0
  // vout item : An object describing one of this transaction’s outputs
  vout     : List[RawTransactionOutput]
) extends RpcResult


/** DecodeRawTransaction: decodes a serialized transaction hex string into a JSON object describing the transaction.
  *
  * Parameter #1 : Serialized Transaction (String;hex, Required)
  *   The transaction to decode in serialized transaction format.
  *
  * Result: (Object)
  *   An object describing the decoded transaction, or JSON null if the transaction could not be decoded.
  *
  * https://bitcoin.org/en/developer-reference#decoderawtransaction
  */
object DecodeRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      // Convert request.params.paramValues, which List[JsValue] to SignRawTransactionParams instance.
      val serializedTransaction: String = request.params.get[String]("Serialized Transaction", 0)

      // TODO : Implement
      Right(
        Some(
          DecodedRawTransaction(
            Hash("ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e"),
            1,
            0L,
            List(
              RawGenerationTransactionInput(
                "Kangmo's transaction",
                4294967295L
              ),
              RawNormalTransactionInput(
                Hash("d7c7557e5ca87d439e9ab6eb69a04a9664a0738ff20f6f083c1db2bfd79a8a26"),
                0,
                RawScriptSig(
                  "3045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc001 03a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326",
                  "483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326"
                ),
                4294967295L
              )
            ),
            List(
              RawTransactionOutput(
                0.39890000,
                0,
                RawScriptPubKey(
                  "OP_DUP OP_HASH160 56847befbd2360df0e35b4e3b77bae48585ae068 OP_EQUALVERIFY OP_CHECKSIG",
                  "76a91456847befbd2360df0e35b4e3b77bae48585ae06888ac",
                  Some(1),
                  Some("pubkeyhash"),
                  List("moQR7i8XM4rSGoNwEsw3h4YEuduuP6mxw7")
                )
              )
            )
          )
        )
      )
    }
  }
  def help() : String =
    """decoderawtransaction "hexstring"
      |
      |Return a JSON object representing the serialized, hex-encoded transaction.
      |
      |Arguments:
      |1. "hex"      (string, required) The transaction hex string
      |
      |Result:
      |{
      |  "txid" : "id",        (string) The transaction id
      |  "size" : n,           (numeric) The transaction size
      |  "version" : n,        (numeric) The version
      |  "locktime" : ttt,     (numeric) The lock time
      |  "vin" : [             (array of json objects)
      |     {
      |       "txid": "id",    (string) The transaction id
      |       "vout": n,       (numeric) The output number
      |       "scriptSig": {   (json object) The script
      |         "asm": "asm",  (string) asm
      |         "hex": "hex"   (string) hex
      |       },
      |       "sequence": n     (numeric) The script sequence number
      |     }
      |     ,...
      |  ],
      |  "vout" : [             (array of json objects)
      |     {
      |       "value" : x.xxx,          (numeric) The value in BTC
      |       "n" : n,                  (numeric) index
      |       "scriptPubKey" : {        (json object)
      |         "asm" : "asm",            (string) the asm
      |         "hex" : "hex",            (string) the hex
      |         "reqSigs" : n,            (numeric) The required sigs
      |         "type" : "pubkeyhash",    (string) The type, eg 'pubkeyhash'
      |         "addresses" : [           (json array of string)
      |           "12tvKAXCxZjSmdNbao16dKXC8tRWfcF5oc"   (string) bitcoin address
      |           ,...
      |         ]
      |       }
      |     }
      |     ,...
      |  ],
      |}
      |
      |Examples:
      |> bitcoin-cli decoderawtransaction "hexstring"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "decoderawtransaction", "params": ["hexstring"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


