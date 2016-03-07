package io.scalechain.blockchain.storage

import org.apache.spark.{SparkContext, SparkConf}

object SparkTester {
  def main(args: Array[String]) {
    val logFile = "/Users/kangmo/crypto/scalechain/README.md" // Should be some file on your system
    val conf = new SparkConf().setMaster("local[2]").setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }
}