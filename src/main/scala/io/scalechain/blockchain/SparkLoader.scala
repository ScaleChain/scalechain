package io.scalechain.blockchain

/* SimpleApp.scala */

import io.scalechain.blockchain.block.{NormalTransactionInput, Util}
import io.scalechain.util.HexUtil
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SparkLoader {
  def main(args: Array[String]) {
    // Step 1 : Setup spark context
    val conf = new SparkConf().setAppName("Blockchain Loader")
    val sc = new SparkContext(conf)

    // Step 2 : Load the block index data created by running 'DumpChain build-block-index-data'.
    val logFile = "/test/block-index-data.txt" // Should be some file on your system
    val logData = sc.textFile(logFile, minPartitions = 2).cache()

    // Step 3 : Convert the RDD to pair RDD using block hash as the key.
    // Its format is (1) block hash (2) space (3) serialized block data in hex format.
    val blockByHash = logData.map{x =>
      val hashAndBlock = x.split(" ")
      val hash = hashAndBlock(0)
      val block = hashAndBlock(1)
      (hash, block)
    }

    // Step 4 : Calculate total amount of Bitcoins sent to outputs by for each block
    val blockStat = blockByHash.mapValues( rawBlockInHex => {
        val block = Util.parse(HexUtil.bytes(rawBlockInHex))
        block.transactions.flatMap{ tx =>
          tx.outputs.map { output =>
            output.value
          }
        }
      }
    )

    println(s"== total amount of Bitcoins sent to outputs ==")
    blockStat.foreach
    { case (blockHash, outputValues : Array[Long] ) =>
      val totalOutputValue = outputValues.sum
      println(s"[$blockHash] total output=${totalOutputValue}")
    }
  }
}