package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.RecordLocatorCodec
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.test.ShouldSpec

/**
  * Test putPrefixedObject, getPrefixedObject, delPrefixedObject, seekPrefixedObject method of KeyValueDatabase.
  */
interface KeyValuePrefixedSeekTestTrait : ShouldSpec, KeyValueCommonTrait {
  var db : KeyValueDatabase

  fun addTests() {
    "getPrefixedObject(objectKey)" should "return a value which was put" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe RecordLocator(1, 2)
    }

    "getPrefixedObject(objectKey)" should "overwrite an existing value" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe RecordLocator(1, 2)

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1000, 2000))
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe RecordLocator(1000, 2000)
    }

    "getPrefixedPrefixedObject(objectKey)" should "store multiple keys" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(2)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3)) shouldBe null

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(2), RecordLocator(3, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3), RecordLocator(5, 2))

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe RecordLocator(1, 2)
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(2)) shouldBe RecordLocator(3, 2)
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3)) shouldBe RecordLocator(5, 2)
    }

    "getPrefixedPrefixedObject(objectKey)" should "store multiple keys with multiple prefixes" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(2)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3)) shouldBe null

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(2), RecordLocator(3, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3), RecordLocator(5, 2))

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe RecordLocator(1, 2)
      db.getPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(1)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(2)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(2)) shouldBe RecordLocator(3, 2)
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3)) shouldBe RecordLocator(5, 2)
      db.getPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(3)) shouldBe null

      db.delPrefixedObject(F, PREFIX1(), "prefix", FileNumber(1))
      db.delPrefixedObject(F, PREFIX2(), "prefix", FileNumber(2))
      db.delPrefixedObject(F, PREFIX1(), "prefix", FileNumber(3))

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX2(), "prefix", FileNumber(2)) shouldBe null
      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(3)) shouldBe null
    }


    "getPrefixedPrefixedObject(objectKey)" should "delete a key" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.delPrefixedObject(F, PREFIX1(), "prefix", FileNumber(1))

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
    }

    "getPrefixedPrefixedObject(objectKey)" should "delete a key with CStringPrefixed" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1), RecordLocator(1, 2))
      db.delPrefixedObject(F, PREFIX1(), CStringPrefixed("prefix", FileNumber(1)))

      db.getPrefixedObject(F, R, PREFIX1(), "prefix", FileNumber(1)) shouldBe null
    }

    "getPrefixedPrefixedObject(objectKey)" should "do nothing if we try to remove a non-existent key" {
      val F = FileNumberCodec

      db.delPrefixedObject(F, PREFIX1(), "prefix", FileNumber(1))
    }

    "seekObject(prefix)" should "iterate nothing if no key exists" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.seekPrefixedObject(F, R, PREFIX1()).use { iterator ->
        iterator.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seekObject(prefix)" should "iterate all keys matching the prefix char" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefixA", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(3), RecordLocator(3, 4))
      db.putPrefixedObject(F, R, PREFIX2(), "prefixC", FileNumber(2), RecordLocator(2, 1))
      db.putPrefixedObject(F, R, PREFIX2(), "prefixD", FileNumber(4), RecordLocator(4, 3))

      db.seekPrefixedObject(F, R, PREFIX1()).use { iterator ->
        iterator.asSequence().toList() shouldBe listOf(
          Pair(CStringPrefixed("prefixA", FileNumber(1)), RecordLocator(1, 2)),
          Pair(CStringPrefixed("prefixB", FileNumber(3)), RecordLocator(3, 4))
        )
      }
    }

    "seekPrefixedObject(prefix, key)" should "iterate nothing if no key exists" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.seekPrefixedObject(F, R, PREFIX1(), "prefix").use { iterator ->
        iterator.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seekPrefixedObject(prefix, key)" should "iterate nothing if the prefix does not exist" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefixA", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(3), RecordLocator(3, 4))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(5), RecordLocator(5, 6))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixC", FileNumber(7), RecordLocator(7, 8))

      db.seekPrefixedObject(F, R, PREFIX1(), "prefixA0").use { iterator ->
        iterator.asSequence().toList().isEmpty() shouldBe true
      }
    }

    "seekPrefixedObject(prefix, key)" should "iterate all keys matching the prefix" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefixA", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(3), RecordLocator(3, 4))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(5), RecordLocator(5, 6))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixC", FileNumber(7), RecordLocator(7, 8))
      db.putPrefixedObject(F, R, PREFIX2(), "prefixB", FileNumber(9), RecordLocator(9, 10))

      db.seekPrefixedObject(F, R, PREFIX1(), "prefixB").use { iterator ->
        iterator.asSequence().toList() shouldBe listOf(
          Pair(CStringPrefixed("prefixB", FileNumber(3)), RecordLocator(3, 4)),
          Pair(CStringPrefixed("prefixB", FileNumber(5)), RecordLocator(5, 6))
        )
      }
    }

    "seekPrefixedObject(prefix, key)" should "iterate nothing if there is no key greater than the given key" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putPrefixedObject(F, R, PREFIX1(), "prefixA", FileNumber(1), RecordLocator(1, 2))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixB", FileNumber(3), RecordLocator(3, 4))
      db.putPrefixedObject(F, R, PREFIX1(), "prefixC", FileNumber(2), RecordLocator(2, 1))

      db.seekPrefixedObject(F, R, PREFIX1(), "prefixC0").use { iterator ->
        iterator.asSequence().toList().isEmpty() shouldBe true
      }
    }
  }
}