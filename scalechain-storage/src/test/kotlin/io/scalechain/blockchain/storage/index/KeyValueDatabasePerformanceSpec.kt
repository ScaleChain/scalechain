package io.scalechain.blockchain.storage.index

import java.io.File
import java.math.BigInteger

import io.scalechain.blockchain.storage.Storage
import io.scalechain.crypto.HashFunctions
import io.scalechain.test.PerformanceTestTrait
import org.apache.commons.io.FileUtils
import org.scalatest._

import scala.collection.mutable.ListBuffer
import scala.util.Random

trait KeyValueDatabasePerformanceTrait : FlatSpec with PerformanceTestTrait with Matchers {
  var db : KeyValueDatabase

  implicit val TEST_COUNT : Int = 100000

  "put/get/del perf(single)" should "measure performance" in {
    val kv = prepareKeyValue(TEST_COUNT)
    measure("put perf(single)") {
      for ((key,value) <- kv) {
        db.put(key,value)
      }
    }

    measure("get perf(single)") {
      for ((key,value) <- kv) {
        db.get(key)
      }
    }

    measure("del perf(single)") {
      for ((key,value) <- kv) {
        db.del(key)
      }
    }
  }

}
