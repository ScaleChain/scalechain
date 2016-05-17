package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.Hash

/** An unspent transaction output class, which is used as an input parameter of signrawtransaction RPC.
  *
  * @param txid The TXID of the transaction the output appeared in. The TXID must be encoded in hex in RPC byte order.
  * @param vout The index number of the output (vout) as it appeared in its transaction, with the first output being 0.
  * @param scriptPubKey The output’s pubkey script encoded as hex.
  * @param redeemScript If the pubkey script was a script hash, this must be the corresponding redeem script.
  */
case class UnspentTransactionOutput(
                                     txid      : Hash,
                                     vout      : Int,
                                     scriptPubKey : String,
                                     redeemScript : Option[String]
                                   )