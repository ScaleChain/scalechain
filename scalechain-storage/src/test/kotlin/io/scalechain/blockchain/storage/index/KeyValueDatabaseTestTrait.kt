package io.scalechain.blockchain.storage.index

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.RecordLocatorCodec
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.crypto.HashFunctions

/**
  * Created by kangmo on 3/23/16.
  */
abstract class KeyValueDatabaseTestTrait : FlatSpec(), KeyValueCommonTrait, Matchers {
  abstract var db : KeyValueDatabase

  fun runTests() {
    "getObject(rawKey)" should "return a value which was put" {
      val C = FileNumberCodec

      db.getObject(C, B("k1")) shouldBe null
      db.putObject(C, B("k1"), FileNumber(1))
      db.getObject(C, B("k1")) shouldBe FileNumber(1)
    }

    "putObject(rawKey)" should "overwrite an existing value" {
      val C = FileNumberCodec

      db.getObject(C, B("k1")) shouldBe null
      db.putObject(C, B("k1"), FileNumber(1))
      db.getObject(C, B("k1")) shouldBe FileNumber(1)

      db.putObject(C, B("k1"), FileNumber(2))
      db.getObject(C, B("k1")) shouldBe FileNumber(2)
    }

    "putObject(rawKey)" should "store multiple keys" {
      val C = FileNumberCodec

      db.getObject(C, B("k1")) shouldBe null
      db.getObject(C, B("k2")) shouldBe null
      db.getObject(C, B("k3")) shouldBe null

      db.putObject(C, B("k1"), FileNumber(1))
      db.putObject(C, B("k2"), FileNumber(2))
      db.putObject(C, B("k3"), FileNumber(3))

      db.getObject(C, B("k1")) shouldBe FileNumber(1)
      db.getObject(C, B("k2")) shouldBe FileNumber(2)
      db.getObject(C, B("k3")) shouldBe FileNumber(3)
    }

    "getObject(objectKey)" should "return a value which was put" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null
      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1,2))
      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe RecordLocator(1,2)
    }

    "putObject(objectKey)" should "overwrite an existing value" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1,2))
      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe RecordLocator(1,2)

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1000,2000))
      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe RecordLocator(1000,2000)
    }

    "putObject(objectKey)" should "store multiple keys" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null
      db.getObject(F, R, PREFIX1(), FileNumber(2)) shouldBe null
      db.getObject(F, R, PREFIX1(), FileNumber(3)) shouldBe null

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1,2))
      db.putObject(F, R, PREFIX1(), FileNumber(2), RecordLocator(3,2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(5,2))

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe RecordLocator(1,2)
      db.getObject(F, R, PREFIX1(), FileNumber(2)) shouldBe RecordLocator(3,2)
      db.getObject(F, R, PREFIX1(), FileNumber(3)) shouldBe RecordLocator(5,2)
    }

    "putObject(objectKey)" should "store multiple keys with multiple prefixes" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec
      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null
      db.getObject(F, R, PREFIX2(), FileNumber(2)) shouldBe null
      db.getObject(F, R, PREFIX1(), FileNumber(3)) shouldBe null

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1,2))
      db.putObject(F, R, PREFIX2(), FileNumber(2), RecordLocator(3,2))
      db.putObject(F, R, PREFIX1(), FileNumber(3), RecordLocator(5,2))

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe RecordLocator(1,2)
      db.getObject(F, R, PREFIX2(), FileNumber(1)) shouldBe null
      db.getObject(F, R, PREFIX1(), FileNumber(2)) shouldBe null
      db.getObject(F, R, PREFIX2(), FileNumber(2)) shouldBe RecordLocator(3,2)
      db.getObject(F, R, PREFIX1(), FileNumber(3)) shouldBe RecordLocator(5,2)
      db.getObject(F, R, PREFIX2(), FileNumber(3)) shouldBe null

      db.delObject(F, PREFIX1(), FileNumber(1))
      db.delObject(F, PREFIX2(), FileNumber(2))
      db.delObject(F, PREFIX1(), FileNumber(3))

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null
      db.getObject(F, R, PREFIX2(), FileNumber(2)) shouldBe null
      db.getObject(F, R, PREFIX1(), FileNumber(3)) shouldBe null
    }


    "delObject(objectKey)" should "delete a key" {
      val F = FileNumberCodec
      val R = RecordLocatorCodec

      db.putObject(F, R, PREFIX1(), FileNumber(1), RecordLocator(1,2))
      db.delObject(F, PREFIX1(), FileNumber(1))

      db.getObject(F, R, PREFIX1(), FileNumber(1)) shouldBe null
    }

    "delObject(objectKey)" should "do nothing if we try to remove a non-existent key" {
      val F = FileNumberCodec

      db.delObject(F, PREFIX1(), FileNumber(1))
    }

    "get" should "return a value which was put" {
      db.get(B("k1")) shouldBe null
      db.put(B("k1"), B("v1"))
      L(db.get(B("k1"))) shouldBe L(B("v1"))
    }

    "put" should "overwrite an existing value" {
      db.get(B("k1")) shouldBe null
      db.put(B("k1"), B("v1"))
      L(db.get(B("k1"))) shouldBe L(B("v1"))

      db.put(B("k1"), B("v1004"))
      L(db.get(B("k1"))) shouldBe L(B("v1004"))
    }

    "put" should "store multiple keys" {
      db.get(B("k1")) shouldBe null
      db.get(B("k2")) shouldBe null
      db.get(B("k3")) shouldBe null

      db.put(B("k1"), B("v1"))
      db.put(B("k2"), B("v2"))
      db.put(B("k3"), B("v3"))

      L(db.get(B("k1"))) shouldBe L(B("v1"))
      L(db.get(B("k2"))) shouldBe L(B("v2"))
      L(db.get(B("k3"))) shouldBe L(B("v3"))
    }

    val keyCount = 1000

    "put/get" should "put/get 1,000 keys" {
      for (i in 1.. keyCount ) {
        val key = HashFunctions.sha256(i.toString().toByteArray())
        val value = HashFunctions.sha256((i*10).toString().toByteArray())
        db.put(key.value, value.value)
      }

      for (i in 1.. keyCount ) {
        val key = HashFunctions.sha256(i.toString().toByteArray())
        val value = HashFunctions.sha256((i*10).toString().toByteArray())
        L(db.get(key.value)) shouldBe L(value.value)
      }
    }

    "put/del/get" should "put/del/get 1,000 keys" {
      for (i in 1 .. keyCount ) {
        val key = HashFunctions.sha256(i.toString().toByteArray())
        val value = HashFunctions.sha256((i*10).toString().toByteArray())
        db.put(key.value, value.value)
      }

      // Delete all keys genereted from the even numbers.
      for (i in 1 .. keyCount ) {
        if ( i % 2 == 0) {
          val key = HashFunctions.sha256(i.toString().toByteArray())
          db.del(key.value)
        }
      }

      for (i in 1.. keyCount ) {
        val key = HashFunctions.sha256(i.toString().toByteArray())
        val value = HashFunctions.sha256((i*10).toString().toByteArray())

        // Should get None for all keys genereted from the even numbers.
        if ( i % 2 == 0 ) {
          db.get(key.value) shouldBe null
        } else {
          L(db.get(key.value)) shouldBe L(value.value)
        }
      }
    }


    "del" should "delete a key" {
      db.get(B("k1")) shouldBe null
      db.put(B("k1"), B("v1"))
      L(db.get(B("k1"))) shouldBe L(B("v1"))
      db.del(B("k1"))
      db.get(B("k1")) shouldBe null
    }

    "del" should "do nothing if we try to remove a non-existent key" {
      db.del(B("k1"))
    }
  }
}
