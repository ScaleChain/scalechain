package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{TransactionOutput, OutPoint}

import scala.collection.mutable

/** Keeps a set of transaction outputs.
  *
  * TODO : Add test cases.
  */
class TransactionOutputSet extends CoinsView {
  /** The map from an out point to a transaction output.
    */
  val outputsByOutPoint = new mutable.HashMap[OutPoint, TransactionOutput]()

  /** The map from a transaction output to an out point
    */
  val outPointsByOutput = new mutable.HashMap[TransactionOutput, OutPoint]()

  /** Add a coin to the set.
    *
    * @param outPoint The out point that points to the output.
    * @param transactionOutput The transaction output to put.
    */
  def addTransactionOutput(outPoint : OutPoint, transactionOutput : TransactionOutput) : Unit = {
    outputsByOutPoint.put(outPoint, transactionOutput)
    outPointsByOutput.put(transactionOutput, outPoint)
  }

  /**
    * Get the out point by searching with the transaction output.
    * @param transactionOutput The transaction output to search.
    * @return The found output.
    */
  def getOutPoint(transactionOutput : TransactionOutput) : Option[OutPoint] = {
    outPointsByOutput.get(transactionOutput)
  }


  /** Get a transaction output by searching with a given out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  def getTransactionOutput(outPoint : OutPoint) : Option[TransactionOutput] = {
    outputsByOutPoint.get(outPoint)
  }
}

