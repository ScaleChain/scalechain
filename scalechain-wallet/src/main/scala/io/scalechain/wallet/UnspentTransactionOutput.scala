package io.scalechain.wallet

import io.scalechain.blockchain.proto.Hash

case class UnspentTranasctionOutput(
  // The TXID of the transaction the output appeared in. The TXID must be encoded in hex in RPC byte order
  txid      : Hash,
  // The index number of the output (vout) as it appeared in its transaction, with the first output being 0
  vout      : Int,
  // The output’s pubkey script encoded as hex
  scriptPubKey : String,
  // If the pubkey script was a script hash, this must be the corresponding redeem script
  redeemScript : Option[String]
)
