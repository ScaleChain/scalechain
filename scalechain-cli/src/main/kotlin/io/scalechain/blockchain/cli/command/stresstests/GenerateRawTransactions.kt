package io.scalechain.blockchain.cli.command.stresstests

import java.io.{PrintWriter, File}
import java.util

import io.scalechain.blockchain.chain.{TransactionBuilder}
import io.scalechain.blockchain.cli.CoinMiner
import io.scalechain.blockchain.cli.command.{RpcParameters, Command}
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction._
import io.scalechain.util.HexUtil
import HashSupported._



/**
  * Created by kangmo on 7/28/16.
  */
object GenerateRawTransactions extends Command {
  def initialSplitTransactionFileName() = "initial-split-transaction.txt"
  def transactionGroupFileName(groupNumber : Int) = s"transaction-group-${groupNumber}.txt"

  def createSplitTransaction( privateKey : PrivateKey, transactionGroupCount : Int) : Transaction = {
    // Assumption : The output of the generation transaction of the block height 1 (right above the genesis block)
    //              can be spent by using the given private key.
    //              (1) For a node, scalechain.mining.address in scalechain.conf should have the address generated from the given private key.
    //              (2) The node mines a block with height 1.
    val generationTx = TransactionGenerator.generationTransaction(1, privateKey)

    val txGenerator = TransactionGenerator.create
    txGenerator.addTransaction(generationTx)

    val txGroupAddresses = txGenerator.newAddresses(transactionGroupCount)

    // The transaction that splits the output of the generation tx into N outputs owned by tx group addresses.
    val unsignedInitialSplitTransaction = txGenerator.newTransaction(generationTx.hash, 0, 0, txGroupAddresses)
    val initialSplitTransaction = txGenerator.signTransaction(unsignedInitialSplitTransaction, Some(List(privateKey)))
    txGenerator.addTransaction(initialSplitTransaction)

    // Write the initial split transaction.
    val writer = new PrintWriter(new File( initialSplitTransactionFileName() ))
    val rawInitialSplitTransaction = HexUtil.hex(TransactionCodec.serialize(initialSplitTransaction))
    writer.append(rawInitialSplitTransaction)
    writer.close

    initialSplitTransaction
  }
  def invoke(command : String, args : Array[String], rpcParams : RpcParameters) = {
    val privateKeyString = args(1)
    val outputSplitCount = Integer.parseInt(args(2))
    val transactionGroupCount = Integer.parseInt(args(3))
    val transactionCountPerGroup = Integer.parseInt(args(4))

    val privateKey = PrivateKey.from(privateKeyString)

    val initialSplitTransaction = createSplitTransaction(privateKey, transactionGroupCount)
    val initialSplitTxHash = initialSplitTransaction.hash

    val threads =
      (0 until transactionGroupCount).map { i =>
        new Thread() {
          var outputIndex = i
          val txGenerator = TransactionGenerator.create
          txGenerator.addTransaction(initialSplitTransaction)
          val splitAddresses = txGenerator.newAddresses(outputSplitCount)
          val mergeAddresses = txGenerator.newAddresses(1)
          override def run(): Unit = {
            val writer = new PrintWriter(new File(transactionGroupFileName(i)))
            var txHash : Hash = initialSplitTxHash
            for (t <- 1 to transactionCountPerGroup/2) {
              if ( ((t >> 7) << 7) == t) { // t % 128 = 0
                println(s"Processing ${t*2} transactions")
              }
              val splitTx = txGenerator.newTransaction(txHash, outputIndex, outputIndex, splitAddresses )
              val signedSplitTx = txGenerator.signTransaction( splitTx )
              txGenerator.addTransaction(signedSplitTx)

              val mergeTx = txGenerator.newTransaction(signedSplitTx.hash, 0, splitTx.outputs.length-1, mergeAddresses)
              val signedMergeTx = txGenerator.signTransaction( mergeTx )
              txGenerator.addTransaction(signedMergeTx)

              val rawSplitTransaction = HexUtil.hex(TransactionCodec.serialize(signedSplitTx))
              val rawMergeTransaction = HexUtil.hex(TransactionCodec.serialize(signedMergeTx))

              writer.println(rawSplitTransaction)
              writer.println(rawMergeTransaction)

              txHash = signedMergeTx.hash
              outputIndex = 0 // From now on, we will use only the first output in the mergeTx
            }
            writer.close
          }
        }
      }

    threads foreach { _.start } // Start all threads
    threads foreach { _.join }  // Join all threads
  }

}