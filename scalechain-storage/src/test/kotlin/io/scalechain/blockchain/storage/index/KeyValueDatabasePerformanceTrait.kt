package io.scalechain.blockchain.storage.index

import io.scalechain.test.PerformanceTestTrait
import io.scalechain.test.ShouldSpec

interface KeyValueDatabasePerformanceTrait : ShouldSpec, PerformanceTestTrait {
  var db : KeyValueDatabase

  fun addTests() {
    "put/get/del perf(single)" should "measure performance" {
      val kv = prepareKeyValue(TEST_COUNT)
      measure(TEST_COUNT, "put perf(single)") {
        for ((key, value) in kv) {
          db.put(key, value)
        }
      }

      measure(TEST_COUNT, "get perf(single)") {
        for ((key, _) in kv) {
          db.get(key)
        }
      }

      measure(TEST_COUNT, "del perf(single)") {
        for ((key, _) in kv) {
          db.del(key)
        }
      }
    }
  }

  companion object {
    val TEST_COUNT : Int = 1000
  }
}
