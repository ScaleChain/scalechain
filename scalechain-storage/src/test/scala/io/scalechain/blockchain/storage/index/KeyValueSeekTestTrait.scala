package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import org.scalatest._
/**
  * Test seek, seekObject(rawKey), seekObject(prefix, key) method of KeyValueDatabase.
  */
trait KeyValueSeekTestTrait extends FlatSpec with KeyValueCommonTrait with ShouldMatchers {
  var db: KeyValueDatabase

  "seek(None)" should "iterate nothing if not key exists" in {
    db.seek(None).toList shouldBe List()
  }

  "seek(None)" should "iterate all keys if any key exists" in {
    db.put(B("k1"), B("v1"))
    db.put(B("k2"), B("v2"))
    db.put(B("k3"), B("v3"))

    db.seek(None).toList shouldBe List((B("k1"), B("v1")), (B("k2"), B("v2")), (B("k3"), B("v3")))
  }

  "seekObject(None)" should "iterate nothing if not key exists" in {
    val C = FileNumberCodec

    db.seekObject(None)(C).toList shouldBe List()
  }

  "seekObject(None)" should "iterate all keys if any key exists" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    db.seekObject(None)(C).toList shouldBe List((B("k1"), FileNumber(1)), (B("k2"), FileNumber(2)), (B("k3"), FileNumber(3)))
  }

  "seekObject(Some(rawKey))" should "iterate starting from the key if it exists" ignore {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    db.seekObject(Some(B("k2")))(C).toList shouldBe List((B("k2"), FileNumber(2)), (B("k3"), FileNumber(3)))
  }

  "seekObject(Some(rawKey))" should "iterate starting from a key greater than the key if it does not exists" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    db.seekObject(Some(B("k20")))(C).toList shouldBe List((B("k3"), FileNumber(3)))
  }

  "seekObject(Some(rawKey))" should "iterate nothing if there is no key greater than the given key" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    db.seekObject(Some(B("k30")))(C).toList shouldBe List()
  }


  "seekObject(prefix, key)" should "iterate starting from the key if it exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    db.seekObject(PREFIX1, FileNumber(3))(F, R).toList shouldBe List(
      (FileNumber(3), RecordLocator(3, 4)),
      (FileNumber(5), RecordLocator(5, 6))
    )
  }

  "seekObject(prefix, key)" should "iterate starting from a key greater than the key if it does not exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    db.seekObject(PREFIX1, FileNumber(4))(F, R).toList shouldBe List(
      (FileNumber(5), RecordLocator(5, 6))
    )
  }

  "seekObject(prefix, key)" should "iterate nothing if there is no key greater than the given key" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    db.seekObject(PREFIX1, FileNumber(5))(F, R).toList shouldBe List()
  }

  "seekObject(prefix, key)" should "only iterate keys with specific prefix" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    // Put the same set of data for PREFIX1 and PREFIX2
    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    db.putObject(PREFIX2, FileNumber(2), RecordLocator(2, 1))(F, R)
    db.putObject(PREFIX2, FileNumber(4), RecordLocator(4, 3))(F, R)
    db.putObject(PREFIX2, FileNumber(6), RecordLocator(6, 5))(F, R)

    db.seekObject(PREFIX1, FileNumber(3))(F, R).toList shouldBe List(
      (FileNumber(3), RecordLocator(3, 4)),
      (FileNumber(5), RecordLocator(5, 6))
    )

    db.seekObject(PREFIX2, FileNumber(3))(F, R).toList shouldBe List(
      (FileNumber(4), RecordLocator(4, 3)),
      (FileNumber(6), RecordLocator(6, 5))
    )
  }

  "seekObject(prefix, key)" should "support nested invocation" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    val nestedIterationResult = db.seekObject(PREFIX1, FileNumber(1))(F, R).flatMap{ case (fileNumber1,recordLocator1) =>
      db.seekObject(PREFIX1, FileNumber(3))(F, R).map { case (fileNumber2, recordLocator2) =>
        (fileNumber1, recordLocator1, fileNumber2, recordLocator2)
      }
    }

    nestedIterationResult shouldBe List(
      (FileNumber(1), RecordLocator(1, 2), FileNumber(3), RecordLocator(3, 4)),
      (FileNumber(1), RecordLocator(1, 2), FileNumber(5), RecordLocator(5, 6)),
      (FileNumber(3), RecordLocator(3, 4), FileNumber(3), RecordLocator(3, 4)),
      (FileNumber(3), RecordLocator(3, 4), FileNumber(5), RecordLocator(5, 6)),
      (FileNumber(5), RecordLocator(5, 6), FileNumber(3), RecordLocator(3, 4)),
      (FileNumber(5), RecordLocator(5, 6), FileNumber(5), RecordLocator(5, 6))
    )
  }
}