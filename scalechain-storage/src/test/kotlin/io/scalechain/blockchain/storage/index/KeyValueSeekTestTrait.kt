package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.RecordLocatorCodec
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.test.ShouldSpec

/**
  * Test seek, seekObject(rawKey), seekObject(prefix, key) method of KeyValueDatabase.
  */
interface KeyValueSeekTestTrait : ShouldSpec, KeyValueCommonTrait {
  var db : KeyValueDatabase

  fun addTests() {
    "seek(None)" should "iterate nothing if not key exists" {
      db.seek(null).use {
        it.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seek(None)" should "iterate all keys if any key exists" {
      db.put(B("k1"), B("v1"))
      db.put(B("k2"), B("v2"))
      db.put(B("k3"), B("v3"))

      db.seek(null).use {
        it.asSequence().map { pair ->
          Pair(pair.first.toList(), pair.second.toList())
        }.toList() shouldBe
          listOf(
            Pair(B("k1"), B("v1")),
            Pair(B("k2"), B("v2")),
            Pair(B("k3"), B("v3"))).map { pair ->
            Pair(pair.first.toList(), pair.second.toList())
          }
      }
    }

    "seekObject(None)" should "iterate nothing if not key exists" {
      val C = FileNumberCodec

      db.seekObject(C, null).use {
        it.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seekObject(None)" should "iterate all keys if any key exists" {
      val C = FileNumberCodec
      db.putObject(C, B("k1"), FileNumber(1))
      db.putObject(C, B("k2"), FileNumber(2))
      db.putObject(C, B("k3"), FileNumber(3))

      db.seekObject(C, null).use {
        it.asSequence().map { pair ->
          Pair(pair.first.toList(), pair.second)
        }.toList() shouldBe
          listOf(
            Pair(B("k1"), FileNumber(1)),
            Pair(B("k2"), FileNumber(2)),
            Pair(B("k3"), FileNumber(3))).map { pair ->
            Pair(pair.first.toList(), pair.second)
          }
      }
    }

    "seekObject(Some(rawKey))" should "iterate starting from the key if it exists" {
      val C = FileNumberCodec
      db.putObject(C, B("k1"), FileNumber(1))
      db.putObject(C, B("k2"), FileNumber(2))
      db.putObject(C, B("k3"), FileNumber(3))
      db.seekObject(C, B("k2")).use {
        it.asSequence().map { pair ->
          Pair(pair.first.toList(), pair.second)
        }.toList() shouldBe
          listOf(
            Pair(B("k2"), FileNumber(2)),
            Pair(B("k3"), FileNumber(3))).map { pair ->
            Pair(pair.first.toList(), pair.second)
          }
      }
    }

    "seekObject(Some(rawKey))" should "iterate starting from a key greater than the key if it does not exists" {
      val C = FileNumberCodec
      db.putObject(C, B("k1"), FileNumber(1))
      db.putObject(C, B("k2"), FileNumber(2))
      db.putObject(C, B("k3"), FileNumber(3))
      db.seekObject(C, B("k20")).use {
        it.asSequence().map { pair ->
          Pair(pair.first.toList(), pair.second)
        }.toList() shouldBe
          listOf(Pair(B("k3"), FileNumber(3))).map { pair ->
            Pair(pair.first.toList(), pair.second)
          }
      }
    }

    "seekObject(Some(rawKey))" should "iterate nothing if there is no key greater than the given key" {
      val C = FileNumberCodec
      db.putObject(C, B("k1"), FileNumber(1))
      db.putObject(C, B("k2"), FileNumber(2))
      db.putObject(C, B("k3"), FileNumber(3))

      db.seekObject(C, B("k30")).use {
        it.asSequence().toList().isEmpty() shouldBe true
      }
    }


    "seekObject(prefix, key)" should "iterate starting from the key if it exists" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
        it.asSequence().toList() shouldBe listOf(
          Pair(FileNumber(3), RecordLocator(3, 4)),
          Pair(FileNumber(5), RecordLocator(5, 6))
        )
      }
    }

    "seekObject(prefix, key)" should "iterate starting from a key greater than the key if it does not exists" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.seekObject(F, R, PREFIX1(), FileNumber(4)).use {
        it.asSequence().toList() shouldBe listOf(
          Pair(FileNumber(5), RecordLocator(5, 6))
        )
      }
    }

    "seekObject(prefix, key)" should "iterate nothing if there is no key greater than the given key" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.seekObject(F, R, PREFIX1(), FileNumber(6)).use {
        it.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seekObject(prefix, key)" should "only iterate keys with specific prefix" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      // Put the same set of data for PREFIX1() and PREFIX2()
      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.putObject(F, R, PREFIX2(), FileNumber(2), RecordLocator(2, 1))
      db.putObject(F, R, PREFIX2(), FileNumber(4), RecordLocator(4, 3))
      db.putObject(F, R, PREFIX2(), FileNumber(6), RecordLocator(6, 5))

      db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
        it.asSequence().toList() shouldBe listOf(
          Pair(FileNumber(3), RecordLocator(3, 4)),
          Pair(FileNumber(5), RecordLocator(5, 6))
        )
      }

      db.seekObject(F, R, PREFIX2(), FileNumber(3)).use {
        it.asSequence().toList() shouldBe listOf(
          Pair(FileNumber(4), RecordLocator(4, 3)),
          Pair(FileNumber(6), RecordLocator(6, 5))
        )
      }
    }

    "seekObject(prefix, key)" should "seek objects multiple times." {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      for (i in 1..10) {
        db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
          it.asSequence().toList() shouldBe listOf(
            Pair(FileNumber(3), RecordLocator(3, 4)),
            Pair(FileNumber(5), RecordLocator(5, 6))
          )
        }
      }
    }

    "seekObject(prefix, key)" should "seek objects multiple times converting the type of elements." {
      data class ConvertedData(val string: String, val num: Int, val fileNum: FileNumber, val recordLocator: RecordLocator)

      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      (1..10).map { i ->
        db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
          it.asSequence().map { pair ->
            val fileNumber2 = pair.first
            val recordLocator2 = pair.second
            ConvertedData("A", i, fileNumber2, recordLocator2)
          }.toList()
        } shouldBe listOf(
          ConvertedData("A", i, FileNumber(3), RecordLocator(3, 4)),
          ConvertedData("A", i, FileNumber(5), RecordLocator(5, 6))
        )
      }
    }

    "seekObject(prefix, key)" should "support nested invocation(simplified version)" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.putObject(F, R, PREFIX2(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX2(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX2(), FileNumber(5), RecordLocator(5, 6))

      db.seekObject(F, R, PREFIX1(), FileNumber(1)).use {
        it.forEach { pair ->
          val fileNumber1 = pair.first
          val recordLocator1 = pair.second
          //        println(s"$fileNumber1, $recordLocator1")

          db.seekObject(F, R, PREFIX2(), FileNumber(3)).use {
            it.forEach { pair ->
              val fileNumber2 = pair.first
              val recordLocator2 = pair.second
              //            println(s"$fileNumber1, $recordLocator1, $fileNumber2, $recordLocator2")
            }
          }
        }
      }
    }

    data class ConvertedData(val fnum1: FileNumber, val rloc1: RecordLocator, val fnum2: FileNumber, val rloc2: RecordLocator)

    // This case calls RocksIterator.close and then calls RocksIterator.hasNext.
    "seekObject(prefix, key)" should "support nested invocation with toList materialization " {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      val outerLoopResult = db.seekObject(F, R, PREFIX1(), FileNumber(1)).use {
        it.asSequence().toList()
      }

      //println("outerLoopResult : " + outerLoopResult)

      // Use toList to iterate all matching (key,value) pairs before starting the nested iteration.
      val nestedIterationResult = outerLoopResult.flatMap { pair ->
        val fileNumber1 = pair.first
        val recordLocator1 = pair.second

        db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
          it.asSequence().map { pair ->
            val fileNumber2 = pair.first
            val recordLocator2 = pair.second

            ConvertedData(fileNumber1, recordLocator1, fileNumber2, recordLocator2)
          }.toList()
        }
      }
    }


    // This test results in JVM crash. The crash was caused by the leveldbjni, rocksdbjni native library.
    "seekObject(prefix, key)" should "support nested invocation for different prefixes" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      db.putObject(F, R, PREFIX2(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX2(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX2(), FileNumber(5), RecordLocator(5, 6))

      val nestedIterationResult = db.seekObject(F, R, PREFIX1(), FileNumber(1)).use {
        it.asSequence().flatMap { pair ->
          val fileNumber1 = pair.first
          val recordLocator1 = pair.second

          db.seekObject(F, R, PREFIX2(), FileNumber(3)).use { it ->
            it.asSequence().map { pair ->
              val fileNumber2 = pair.first
              val recordLocator2 = pair.second

              ConvertedData(fileNumber1, recordLocator1, fileNumber2, recordLocator2)
            }.toList().asSequence() // If we don't call toList(), the iterator is closed by .use, and we have no items in the sequence.
          }
        }.toList()
      }

      nestedIterationResult shouldBe listOf(
        ConvertedData(FileNumber(1), RecordLocator(1, 2), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(1), RecordLocator(1, 2), FileNumber(5), RecordLocator(5, 6)),
        ConvertedData(FileNumber(3), RecordLocator(3, 4), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(3), RecordLocator(3, 4), FileNumber(5), RecordLocator(5, 6)),
        ConvertedData(FileNumber(5), RecordLocator(5, 6), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(5), RecordLocator(5, 6), FileNumber(5), RecordLocator(5, 6))
      )
    }

    // This test results in JVM crash. The crash was caused by the leveldbjni, rocksdbjni native library.
    "seekObject(prefix, key)" should "support nested invocation for the same prefix" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1, 2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(3, 4))
      db.putObject(F, R, PREFIX1(), FileNumber(5), RecordLocator(5, 6))

      val nestedIterationResult = db.seekObject(F, R, PREFIX1(), FileNumber(1)).use {
        it.asSequence().flatMap { pair ->
          val fileNumber1 = pair.first
          val recordLocator1 = pair.second

          db.seekObject(F, R, PREFIX1(), FileNumber(3)).use {
            it.asSequence().map { pair ->
              val fileNumber2 = pair.first
              val recordLocator2 = pair.second

              ConvertedData(fileNumber1, recordLocator1, fileNumber2, recordLocator2)
            }.toList().asSequence() // If we don't call toList(), the iterator is closed by .use, and we have no items in the sequence.
          }
        }.toList()
      }

      nestedIterationResult shouldBe listOf(
        ConvertedData(FileNumber(1), RecordLocator(1, 2), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(1), RecordLocator(1, 2), FileNumber(5), RecordLocator(5, 6)),
        ConvertedData(FileNumber(3), RecordLocator(3, 4), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(3), RecordLocator(3, 4), FileNumber(5), RecordLocator(5, 6)),
        ConvertedData(FileNumber(5), RecordLocator(5, 6), FileNumber(3), RecordLocator(3, 4)),
        ConvertedData(FileNumber(5), RecordLocator(5, 6), FileNumber(5), RecordLocator(5, 6))
      )
    }
  }
}