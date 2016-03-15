package io.scalechain.blockchain.net.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.scalechain.blockchain.net.processor.TransactionProcessor.GetTransactionResult
import io.scalechain.blockchain.net.processor.{TransactionProcessor, BlockProcessor}
import io.scalechain.blockchain.net.processor.BlockProcessor.GetBlockResult
import io.scalechain.blockchain.proto.{Transaction, Block, Hash}

import scala.concurrent._
import scala.concurrent.duration._

/**
  * Created by kangmo on 3/15/16.
  */
class MempoolService(transactionProcessor : ActorRef) {
  implicit val timeout = Timeout(60 seconds)

  /** Get a transaction by the transaction hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The hash of the transaction to search.
    * @return The searched transaction.
    */
  def getTransaction(txHash : Hash): Option[Transaction] = {
    val resultFuture = ( transactionProcessor ? TransactionProcessor.GetTransaction(txHash) ).mapTo[GetTransactionResult]
    Await.result(resultFuture, Duration.Inf).transactionOption
  }
}
