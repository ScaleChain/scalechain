package io.scalechain.blockchain.cli.command.stresstests

import java.io.PrintWriter
import java.io.File

import io.scalechain.blockchain.cli.command.RpcParameters
import io.scalechain.blockchain.cli.command.Command
import io.scalechain.blockchain.cli.command.CommandDescriptor
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.HexUtil



/**
  * Created by kangmo on 7/28/16.
  */
object GenerateRawTransactions : Command {
  override val descriptor = CommandDescriptor( "generaterawtransactions", 4, "generaterawtransactions <private key> <output split count> <transaction group count> <transaction count for each group>. generaterawtransactions generates transactions.")

  fun initialSplitTransactionFileName() = "initial-split-transaction.txt"
  fun transactionGroupFileName(groupNumber : Int) = "transaction-group-${groupNumber}.txt"

  fun createSplitTransaction( privateKey : PrivateKey, transactionGroupCount : Int) : Transaction {
    // Assumption : The output of the generation transaction of the block height 1 (right above the genesis block)
    //              can be spent by using the given private key.
    //              (1) For a node, scalechain.mining.address in scalechain.conf should have the address generated from the given private key.
    //              (2) The node mines a block with height 1.
    val generationTx = TransactionGenerator.generationTransaction(1, privateKey)

    val txGenerator = TransactionGenerator.create()
    txGenerator.addTransaction(generationTx)

    val txGroupAddresses = txGenerator.newAddresses(transactionGroupCount)

    // The transaction that splits the output of the generation tx into N outputs owned by tx group addresses.
    val unsignedInitialSplitTransaction = txGenerator.newTransaction(generationTx.hash(), 0, 0, txGroupAddresses)
    val initialSplitTransaction = txGenerator.signTransaction(unsignedInitialSplitTransaction, listOf(privateKey))
    txGenerator.addTransaction(initialSplitTransaction)

    // Write the initial split transaction.
    val writer = PrintWriter(File( initialSplitTransactionFileName() ))
    val rawInitialSplitTransaction = HexUtil.hex(TransactionCodec.encode(initialSplitTransaction))
    writer.append(rawInitialSplitTransaction)
    writer.close()

    return initialSplitTransaction
  }

  override fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters) {
    val privateKeyString = args[0]
    val outputSplitCount = Integer.parseInt(args[1])
    val transactionGroupCount = Integer.parseInt(args[2])
    val transactionCountPerGroup = Integer.parseInt(args[3])

    val privateKey = PrivateKey.from(privateKeyString)

    val initialSplitTransaction = createSplitTransaction(privateKey, transactionGroupCount)
    val initialSplitTxHash = initialSplitTransaction.hash()

    val threads =
      (0 until transactionGroupCount).map { i ->
        object : Thread() {
          var outputIndex = i

          var txGenerator : TransactionGenerator
          var splitAddresses : List<CoinAddress>
          var mergeAddresses : List<CoinAddress>

          init {
            txGenerator = TransactionGenerator.create()
            txGenerator.addTransaction(initialSplitTransaction)

            splitAddresses = txGenerator.newAddresses(outputSplitCount)
            mergeAddresses = txGenerator.newAddresses(1)
          }

          override fun run(): Unit {
            val writer = PrintWriter(File(transactionGroupFileName(i)))
            var txHash : Hash = initialSplitTxHash
            for (t in 1 .. transactionCountPerGroup/2) {
              if ( ((t shr 7) shl 7) == t) { // t % 128 = 0
                println("Processing ${t*2} transactions")
              }
              val splitTx = txGenerator.newTransaction(txHash, outputIndex, outputIndex, splitAddresses )
              val signedSplitTx = txGenerator.signTransaction( splitTx )
              txGenerator.addTransaction(signedSplitTx)

              val mergeTx = txGenerator.newTransaction(signedSplitTx.hash(), 0, splitTx.outputs.size-1, mergeAddresses)
              val signedMergeTx = txGenerator.signTransaction( mergeTx )
              txGenerator.addTransaction(signedMergeTx)

              val rawSplitTransaction = HexUtil.hex(TransactionCodec.encode(signedSplitTx))
              val rawMergeTransaction = HexUtil.hex(TransactionCodec.encode(signedMergeTx))

              writer.println(rawSplitTransaction)
              writer.println(rawMergeTransaction)

              txHash = signedMergeTx.hash()
              outputIndex = 0 // From now on, we will use only the first output in the mergeTx
            }
            writer.close()
          }
        }
      }

    threads.forEach { it.start() } // Start all threads
    threads.forEach { it.join() }  // Join all threads
  }
}