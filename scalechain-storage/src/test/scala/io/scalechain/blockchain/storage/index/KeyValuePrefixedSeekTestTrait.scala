package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import io.scalechain.util.Using
import org.scalatest._
import Using._

/**
  * Test putPrefixedObject, getPrefixedObject, delPrefixedObject, seekPrefixedObject method of KeyValueDatabase.
  */
trait KeyValuePrefixedSeekTestTrait extends FlatSpec with KeyValueCommonTrait with ShouldMatchers {
  var db: KeyValueDatabase

  "getPrefixedObject(objectKey)" should "return a value which was put" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None
    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1,2))(F, R)
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
  }

  "getPrefixedObject(objectKey)" should "overwrite an existing value" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None

    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1,2))(F, R)
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))

    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1000,2000))(F, R)
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe Some(RecordLocator(1000,2000))
  }

  "getPrefixedPrefixedObject(objectKey)" should "store multiple keys" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(2))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F, R) shouldBe None

    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1,2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(2), RecordLocator(3,2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(3), RecordLocator(5,2))(F, R)

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(2))(F, R) shouldBe Some(RecordLocator(3,2))
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F, R) shouldBe Some(RecordLocator(5,2))
  }

  "getPrefixedPrefixedObject(objectKey)" should "store multiple keys with multiple prefixes" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX2, "prefix", FileNumber(2))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F, R) shouldBe None

    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1,2))(F, R)
    db.putPrefixedObject(PREFIX2, "prefix", FileNumber(2), RecordLocator(3,2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(3), RecordLocator(5,2))(F, R)

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
    db.getPrefixedObject(PREFIX2, "prefix", FileNumber(1))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(2))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX2, "prefix", FileNumber(2))(F, R) shouldBe Some(RecordLocator(3,2))
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F, R) shouldBe Some(RecordLocator(5,2))
    db.getPrefixedObject(PREFIX2, "prefix", FileNumber(3))(F, R) shouldBe None

    db.delPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F)
    db.delPrefixedObject(PREFIX2, "prefix", FileNumber(2))(F)
    db.delPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F)

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX2, "prefix", FileNumber(2))(F, R) shouldBe None
    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(3))(F, R) shouldBe None
  }


  "getPrefixedPrefixedObject(objectKey)" should "delete a key" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putPrefixedObject(PREFIX1, "prefix", FileNumber(1), RecordLocator(1,2))(F, R)
    db.delPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F)

    db.getPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F, R) shouldBe None
  }

  "getPrefixedPrefixedObject(objectKey)" should "do nothing if we try to remove a non-existent key" in {
    val F = FileNumberCodec

    db.delPrefixedObject(PREFIX1, "prefix", FileNumber(1))(F)
  }

  "seekObject(prefix)" should "iterate nothing if no key exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    using( db.seekPrefixedObject(PREFIX1)(F,R) ) in {
      _.toList shouldBe List()
    }
  }

  "seekObject(prefix)" should "iterate all keys matching the prefix char" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putPrefixedObject(PREFIX1, "prefixA", FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putPrefixedObject(PREFIX2, "prefixC", FileNumber(2), RecordLocator(2, 1))(F, R)
    db.putPrefixedObject(PREFIX2, "prefixD", FileNumber(4), RecordLocator(4, 3))(F, R)

    using( db.seekPrefixedObject(PREFIX1)(F,R) ) in {
      _.toList shouldBe List(
        (CStringPrefixed("prefixA", FileNumber(1)), RecordLocator(1, 2)),
        (CStringPrefixed("prefixB", FileNumber(3)), RecordLocator(3, 4))
      )
    }
  }

  "seekPrefixedObject(prefix, key)" should "iterate nothing if no key exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    using( db.seekPrefixedObject(PREFIX1, "prefix")(F,R) ) in {
      _.toList shouldBe List()
    }
  }

  "seekPrefixedObject(prefix, key)" should "iterate nothing if the prefix does not exist" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putPrefixedObject(PREFIX1, "prefixA", FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(5), RecordLocator(5, 6))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixC", FileNumber(7), RecordLocator(7, 8))(F, R)

    using( db.seekPrefixedObject(PREFIX1, "prefixA0")(F,R) ) in {
      _.toList shouldBe List()
    }
  }

  "seekPrefixedObject(prefix, key)" should "iterate all keys matching the prefix" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putPrefixedObject(PREFIX1, "prefixA", FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(5), RecordLocator(5, 6))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixC", FileNumber(7), RecordLocator(7, 8))(F, R)
    db.putPrefixedObject(PREFIX2, "prefixB", FileNumber(9), RecordLocator(9, 10))(F, R)

    using( db.seekPrefixedObject(PREFIX1, "prefixB")(F, R) ) in {
      _.toList shouldBe List(
        (CStringPrefixed("prefixB", FileNumber(3)), RecordLocator(3, 4)),
        (CStringPrefixed("prefixB", FileNumber(5)), RecordLocator(5, 6))
      )
    }
  }

  "seekPrefixedObject(prefix, key)" should "iterate nothing if there is no key greater than the given key" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putPrefixedObject(PREFIX1, "prefixA", FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixB", FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putPrefixedObject(PREFIX1, "prefixC", FileNumber(2), RecordLocator(2, 1))(F, R)

    using( db.seekPrefixedObject(PREFIX1, "prefixC0")(F, R) ) in {
      _.toList shouldBe List()
    }
  }

}