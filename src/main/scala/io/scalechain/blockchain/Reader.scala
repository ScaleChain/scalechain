package io.scalechain.blockchain

/**
 * Created by kangmo on 2015. 11. 1..
 */


class BlockHeader {

}

class Transaction {

}

class TransactionInput {

}

class TransactionOutput {

}


case class Block(val size:Long, val header:BlockHeader, val transactionCount:Int, val transactions : Array[Transaction], val previousBlock : Block ) {

}


/** Read the blockchain data downloaded by the reference Bitcoin core implementation.
 *
 */
object Reader {
  def readFrom(path : String) : Block = {
    null
  }
}
class Reader {

}
