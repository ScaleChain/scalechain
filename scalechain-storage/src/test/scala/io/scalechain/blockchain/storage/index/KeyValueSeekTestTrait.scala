package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import io.scalechain.util.Using
import org.scalatest._
import Using._

/**
  * Test seek, seekObject(rawKey), seekObject(prefix, key) method of KeyValueDatabase.
  */
trait KeyValueSeekTestTrait extends FlatSpec with KeyValueCommonTrait with ShouldMatchers {
  var db: KeyValueDatabase

  "seek(None)" should "iterate nothing if not key exists" in {
    using( db.seek(None) ) in {
      _.toList shouldBe List()
    }
  }

  "seek(None)" should "iterate all keys if any key exists" in {
    db.put(B("k1"), B("v1"))
    db.put(B("k2"), B("v2"))
    db.put(B("k3"), B("v3"))

    using( db.seek(None) ) in {
      _.toList.map { case(k,v)=> (k.toList, v.toList) } shouldBe
       List((B("k1"), B("v1")), (B("k2"), B("v2")), (B("k3"), B("v3"))).map { case(k,v)=> (k.toList, v.toList) }
    }
  }

  "seekObject(None)" should "iterate nothing if not key exists" in {
    val C = FileNumberCodec

    using( db.seekObject(None)(C) ) in {
      _.toList shouldBe List()
    }
  }

  "seekObject(None)" should "iterate all keys if any key exists" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)

    using( db.seekObject(None)(C) ) in {
      _.toList.map{ case(k,v)=> (k.toList, v)} shouldBe
        List((B("k1"), FileNumber(1)), (B("k2"), FileNumber(2)), (B("k3"), FileNumber(3))).map { case(k,v)=> (k.toList, v)}
    }
  }

  "seekObject(Some(rawKey))" should "iterate starting from the key if it exists" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    using( db.seekObject(Some(B("k2")))(C) ) in {
      _.toList.map{ case(k,v)=> (k.toList, v)} shouldBe
        List((B("k2"), FileNumber(2)), (B("k3"), FileNumber(3))).map { case(k,v)=> (k.toList, v)}
    }
  }

  "seekObject(Some(rawKey))" should "iterate starting from a key greater than the key if it does not exists" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)
    using( db.seekObject(Some(B("k20")))(C) ) in {
      _.toList.map{ case(k,v)=> (k.toList, v)} shouldBe
        List((B("k3"), FileNumber(3))).map { case(k,v)=> (k.toList, v)}
    }
  }

  "seekObject(Some(rawKey))" should "iterate nothing if there is no key greater than the given key" in {
    val C = FileNumberCodec
    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)

    using( db.seekObject(Some(B("k30")))(C) ) in {
      _.toList shouldBe List()
    }
  }


  "seekObject(prefix, key)" should "iterate starting from the key if it exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in {
      _.toList shouldBe List(
        (FileNumber(3), RecordLocator(3, 4)),
        (FileNumber(5), RecordLocator(5, 6))
      )
    }
  }

  "seekObject(prefix, key)" should "iterate starting from a key greater than the key if it does not exists" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    using( db.seekObject(PREFIX1, FileNumber(4))(F, R) ) in {
      _.toList shouldBe List(
        (FileNumber(5), RecordLocator(5, 6))
      )
    }
  }

  "seekObject(prefix, key)" should "iterate nothing if there is no key greater than the given key" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    using(  db.seekObject(PREFIX1, FileNumber(6))(F, R) ) in {
      _.toList shouldBe List()
    }
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

    using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in {
      _.toList shouldBe List(
        (FileNumber(3), RecordLocator(3, 4)),
        (FileNumber(5), RecordLocator(5, 6))
      )
    }

    using( db.seekObject(PREFIX2, FileNumber(3))(F, R) ) in {
      _.toList shouldBe List(
        (FileNumber(4), RecordLocator(4, 3)),
        (FileNumber(6), RecordLocator(6, 5))
      )
    }
  }

  "seekObject(prefix, key)" should "seek objects multiple times." in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    for (i<-1 to 10) {
      using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in { iter1 =>
        iter1.toList
      }
    }
  }

  "seekObject(prefix, key)" should "seek objects multiple times converting the type of elements." in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    (1 to 10).map { i =>
      using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in { iter1 =>
        iter1.map { case (fileNumber2, recordLocator2) =>
          ("A", i, fileNumber2, recordLocator2)
        }
      }
    }
  }

  // This test results in JVM crash. The crash was caused by the leveldbjni native library.
  "seekObject(prefix, key)" should "support nested invocation with toList materialization " ignore {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    val outerLoopResult = using( db.seekObject(PREFIX1, FileNumber(1))(F, R) ) in { iter1 =>
      iter1.toList
    }

    println("outerLoopResult : " + outerLoopResult)

    // Use toList to iterate all matching (key,value) pairs before starting the nested iteration.
    val nestedIterationResult = outerLoopResult.flatMap{ case (fileNumber1,recordLocator1) =>
      using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in { iter2 =>
        iter2.map { case (fileNumber2, recordLocator2) =>
          (fileNumber1, recordLocator1, fileNumber2, recordLocator2)
        }
      }
    }
    /*
    // Use toList to iterate all matching (key,value) pairs before starting the nested iteration.
    val nestedIterationResult = outerLoopResult.flatMap{ case (fileNumber1,recordLocator1) =>
      using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in { iter2 =>
        iter2.map { case (fileNumber2, recordLocator2) =>
          (fileNumber1, recordLocator1, fileNumber2, recordLocator2)
        }
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
    */
  }


  // This test results in JVM crash. The crash was caused by the leveldbjni native library.
  "seekObject(prefix, key)" should "support nested invocation for different prefixes" ignore {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    db.putObject(PREFIX2, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX2, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX2, FileNumber(5), RecordLocator(5, 6))(F, R)

    using( db.seekObject(PREFIX1, FileNumber(1))(F, R) ) in { iter1 =>

      val nestedIterationResult = iter1.flatMap{ case (fileNumber1,recordLocator1) =>

        using( db.seekObject(PREFIX2, FileNumber(3))(F, R) ) in { iter2 =>
          iter2.map { case (fileNumber2, recordLocator2) =>
            (fileNumber1, recordLocator1, fileNumber2, recordLocator2)
          }
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


  // This test results in JVM crash. The crash was caused by the leveldbjni native library.
  "seekObject(prefix, key)" should "support nested invocation for the same prefix" ignore {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1, 2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(3, 4))(F, R)
    db.putObject(PREFIX1, FileNumber(5), RecordLocator(5, 6))(F, R)

    using( db.seekObject(PREFIX1, FileNumber(1))(F, R) ) in { iter1 =>

      val nestedIterationResult = iter1.flatMap{ case (fileNumber1,recordLocator1) =>

        using( db.seekObject(PREFIX1, FileNumber(3))(F, R) ) in { iter2 =>
          iter2.map { case (fileNumber2, recordLocator2) =>
            (fileNumber1, recordLocator1, fileNumber2, recordLocator2)
          }
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
}