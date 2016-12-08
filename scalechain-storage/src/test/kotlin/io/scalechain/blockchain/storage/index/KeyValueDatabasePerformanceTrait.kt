package io.scalechain.blockchain.storage.index

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.test.PerformanceTestTrait

abstract class KeyValueDatabasePerformanceTrait : FlatSpec(), PerformanceTestTrait, Matchers {
  abstract var db : KeyValueDatabase

  val TEST_COUNT : Int = 100000

  fun runTests() {
    "put/get/del perf(single)" should "measure performance" {
      val kv = prepareKeyValue(TEST_COUNT)
      measure(TEST_COUNT, "put perf(single)") {
        for ((key,value) in kv) {
          db.put(key,value)
        }
      }

      measure(TEST_COUNT, "get perf(single)") {
        for ((key,value) in kv) {
          db.get(key)
        }
      }

      measure(TEST_COUNT, "del perf(single)") {
        for ((key,value) in kv) {
          db.del(key)
        }
      }
    }
  }
}
