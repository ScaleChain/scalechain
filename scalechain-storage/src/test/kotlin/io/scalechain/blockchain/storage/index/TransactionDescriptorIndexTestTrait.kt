package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.TransactionLocator
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.test.ShouldSpec
import io.scalechain.util.ListExt
import org.junit.runner.RunWith

/**
 * Created by kangmo on 15/12/2016.
 */
@RunWith(KTestJUnitRunner::class)
interface TransactionDescriptorIndexTestTrait : ShouldSpec, KeyValueCommonTrait, ProtoTestData {
  var db : KeyValueDatabase

  fun txDesc(height : Long, outputCount:Int) : TransactionDescriptor {
    return TransactionDescriptor(
      transactionLocator = FileRecordLocator(1, RecordLocator(2,3)),
      blockHeight = height,
      outputsSpentBy = ListExt.fill<InPoint?>( outputCount, null)
    )
  }

  fun addTests() {
    "getTransactionDescriptor" should "return null if the descriptor was not put" {
      val index = object : TransactionDescriptorIndex{}

      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe null
    }

    "getTransactionDescriptor" should "return descriptor if the descriptor was put" {
      val index = object : TransactionDescriptorIndex{}

      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(1,1))
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(1,1)

      index.putTransactionDescriptor( db, transaction2().hash(), txDesc(2,2))
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(1,1)
      index.getTransactionDescriptor( db, transaction2().hash()) shouldBe txDesc(2,2)

      index.putTransactionDescriptor( db, transaction3().hash(), txDesc(3,3))
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(1,1)
      index.getTransactionDescriptor( db, transaction2().hash()) shouldBe txDesc(2,2)
      index.getTransactionDescriptor( db, transaction3().hash()) shouldBe txDesc(3,3)
    }

    "getTransactionDescriptor" should "overwrite an existing desciptor" {
      val index = object : TransactionDescriptorIndex{}

      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(1,1))
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(1,1)

      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(2,2))
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(2,2)
    }

    "getTransactionDescriptor" should "return null if the descriptor was deleted" {
      val index = object : TransactionDescriptorIndex{}

      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(1,1))
      index.delTransactionDescriptor( db, transaction1().hash())
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe null


      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(1,1))
      index.putTransactionDescriptor( db, transaction2().hash(), txDesc(2,2))
      index.delTransactionDescriptor( db, transaction1().hash())

      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe null
      index.getTransactionDescriptor( db, transaction2().hash()) shouldBe txDesc(2,2)

      index.putTransactionDescriptor( db, transaction1().hash(), txDesc(1,1))
      index.putTransactionDescriptor( db, transaction2().hash(), txDesc(2,2))
      index.putTransactionDescriptor( db, transaction3().hash(), txDesc(3,3))
      index.delTransactionDescriptor( db, transaction2().hash())

      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe txDesc(1,1)
      index.getTransactionDescriptor( db, transaction2().hash()) shouldBe null
      index.getTransactionDescriptor( db, transaction3().hash()) shouldBe txDesc(3,3)

      index.delTransactionDescriptor( db, transaction1().hash())
      index.getTransactionDescriptor( db, transaction1().hash()) shouldBe null

      index.delTransactionDescriptor( db, transaction3().hash())
      index.getTransactionDescriptor( db, transaction3().hash()) shouldBe null
    }
  }
}
