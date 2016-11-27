package io.scalechain.wallet

import io.scalechain.blockchain.proto.Hash

data class UnspentCoinDescriptor(
  // The TXID of the transaction containing the output, encoded as hex in RPC byte order
  val txid          : Hash,                  // "d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a",
  // The output index number (vout) of the output within its containing transaction
  val vout          : Int,                   // 1,
  // The P2PKH or P2SH address the output paid. Only returned for P2PKH or P2SH output scripts
  val address       : String?,        // "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe",
  // If the address returned belongs to an account, this is the account. Otherwise not returned
  val account       : String?,        // "test label",
  // The output script paid, encoded as hex
  val scriptPubKey  : String,                // "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
  // If the output is a P2SH whose script belongs to this wallet, this is the redeem script
  val redeemScript  : String?,
  // The amount paid to the output in bitcoins
  val amount        : java.math.BigDecimal, // 0.00010000,
  // The number of confirmations received for the transaction containing this output
  val confirmations : Long,                  // 6210,
  // ( Since : 0.10.0 )
  // Set to true if the private key or keys needed to spend this output are part of the wallet.
  // Set to false if not (such as for watch-only addresses)
  val spendable     : Boolean                // true
)
