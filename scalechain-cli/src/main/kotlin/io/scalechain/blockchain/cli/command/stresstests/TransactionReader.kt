package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import java.io.BufferedReader
import java.io.FileReader


interface RawTransactionListener {
  fun onRawTransaction(rawTransaction : String) : Unit
}

/**
  * Read a transaction file, which has a raw transaction in the hex format. The file has a transaction per line.
  *
  */
class TransactionReader(private val transactionFile : File) {
  /**
    * Read all transactions from the file, call the transaction listener for each transaction read.
    *
    * @param listener The listener, which will be notified for each transaction in the file.
    */
  fun read(listener : RawTransactionListener) : Unit {
    BufferedReader(FileReader(transactionFile)).use { br ->
      var rawTransactionString: String?
      do {
        rawTransactionString = br.readLine()
        listener.onRawTransaction(rawTransactionString)


      } while (rawTransactionString != null)
    }
  }
}
