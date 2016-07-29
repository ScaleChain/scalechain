package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.cli.command.stresstests.TransactionReader.TransactionListener
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.util.HexUtil

import scala.io.Source

object TransactionReader {
  type TransactionListener = Transaction => Unit
}

/**
  * Read a transaction file, which has a raw transaction in the hex format. The file has a transaction per line.
  *
  */
class TransactionReader(transactionFile : File) {
  /**
    * Read all transactions from the file, call the transaction listener for each transaction read.
 *
    * @param listener The listener, which will be notified for each transaction in the file.
    */
  def read(listener : TransactionListener) : Unit = {
    for( rawTransactionString <- Source.fromFile(transactionFile).getLines()) {
      val rawTransaction = HexUtil.bytes(rawTransactionString)
      val transaction = TransactionCodec.parse(rawTransaction)
      listener(transaction)
    }
  }
}
