package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.DatabaseFactory
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.test.TestData.dummyHash
import io.scalechain.util.Bytes
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/4/16.
  */
@RunWith(KTestJUnitRunner::class)
class OrphanTransactionIndexSpec  : FlatSpec(), Matchers {

  /** Create a dummy orphan block.
    *
    * @param num should be an integer value to create different dummy orphan blocks.
    * @return The created orphan block.
    */
  fun orphanTransaction(num : Long) = OrphanTransactionDescriptor(
    Transaction(version=1, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("42828762f3233f5b4ca6d56dc8b90c7e7fd27c971860c6df7b628c6090212553")), outputIndex=1, unlockingScript=UnlockingScript(Bytes.from("483045022100e55e797b51711148692b9cc4a2c80652c69bd78d4103438dec41e01670c046f802207ef80574570cfde8f6a788a6b2900343d01115645b6a037773c43df507493294012102772555483a3445bab2cdd3886a1cc9b362f4e9991db50d0944689aed90b67b19")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("b4055f82e6607c370e20f58245fb2ff304989d817b565dfa131b774176cacdb3")), outputIndex=1, unlockingScript=UnlockingScript(Bytes.from("483045022100844f55bdc9bfa12f468560ee293219433ad96060538a6ee6df7d0922d7e4b80602204da8d73e024e353fb5598dda67a6afe8425677b7ee483cbb2147ced5bfc5a629012103df96db891b824f53f5ac423368b627319071b95409491989e641eee2d7326864")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("db373a4ccea956e127dfaf2b34519c27448c17c51c37c2c6e303adc256e472fe")), outputIndex=1, unlockingScript=UnlockingScript(Bytes.from("48304502210082ca0851f3a94d4e0d0067a47f6bd132b47b798a83e40e7a408011d4021d7ec5022034b75358575754bfe58229610d12749c60e7295d9472790014067d36eab88dd1012103df96db891b824f53f5ac423368b627319071b95409491989e641eee2d7326864")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("6c2bb41d2998952a63e17ba5890b38889ebec7537d77e6d6d086d3feedce6f35")), outputIndex=0, unlockingScript=UnlockingScript(Bytes.from("463043022070d2b610ac64d9643a87d33d785996596612e343fe62f7a6a94f7ab732ffbb8a021f1b1bee854faa6595be856c64a8cf5086821d3867a1da5e12bc27ab5905ab9501210251571c711c89343b477ea209601542b9827dad55e1249ca35bb8cc0f65efad1c")), sequenceNumber=num),NormalTransactionInput(outputTransactionHash=Hash(Bytes.from("ae5cfce86e33b718916fa76779c6e62ccb0596ced839940107ae832ccf29d0f4")), outputIndex=2, unlockingScript=UnlockingScript(Bytes.from("473044022100fec80cc70b82274c0b292c79a1c0cb672d83b0bcc79ea61a55e7b50ae6c32f6b021f4dc2534e647bbcec0d2741e44421a080ad192edccccabc742da715980dbe940121023547f5787c54caf67be8359a482553ee83a8e77eb13526475ad17475077d888c")), sequenceNumber=4294967295L)), outputs=listOf(TransactionOutput(value=num, lockingScript=LockingScript(Bytes.from("76a914f92a54b60ae8b9eaf4234aea1b51d0c0659d6aed88ac"))),TransactionOutput(value=num+10, lockingScript=LockingScript(Bytes.from("76a91408240470d3e7e1a3dd26e7655e3fb90373bfa5bb88ac")))), lockTime=0L)
  )

  lateinit var index : OrphanTransactionIndex
  lateinit var db : KeyValueDatabase

  val testPath = File("./target/unittests-OrphanTransactionIndexSpec")
  override fun beforeEach() {

    testPath.deleteRecursively()
    testPath.mkdir()

    index = object : OrphanTransactionIndex {}
    db = DatabaseFactory.create(testPath)


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()

    testPath.deleteRecursively()
  }

  init {
    Storage.initialize()

    "putOrphanTransaction/getOrphanTransaction" should "successfully put/get orphan transactions" {
      index.getOrphanTransaction(db, dummyHash(1)) shouldBe null
      index.putOrphanTransaction(db, dummyHash(1), orphanTransaction(1))
      index.getOrphanTransaction(db, dummyHash(1)) shouldBe orphanTransaction(1)

      index.getOrphanTransaction(db, dummyHash(2)) shouldBe null
      index.putOrphanTransaction(db, dummyHash(2), orphanTransaction(2))
      index.getOrphanTransaction(db, dummyHash(2)) shouldBe orphanTransaction(2)
    }

    "delOrphanTransaction" should "delete the specified orphan transaction" {
      index.putOrphanTransaction(db, dummyHash(1), orphanTransaction(1) )
      index.putOrphanTransaction(db, dummyHash(2), orphanTransaction(2) )
      index.putOrphanTransaction(db, dummyHash(3), orphanTransaction(3) )
      index.putOrphanTransaction(db, dummyHash(4), orphanTransaction(4) )
      index.putOrphanTransaction(db, dummyHash(5), orphanTransaction(5) )

      index.delOrphanTransaction(db, dummyHash(2) )
      index.delOrphanTransaction(db, dummyHash(4) )

      index.getOrphanTransaction(db, dummyHash(1) ) shouldBe orphanTransaction(1)
      index.getOrphanTransaction(db, dummyHash(2)) shouldBe null
      index.getOrphanTransaction(db, dummyHash(3) ) shouldBe orphanTransaction(3)
      index.getOrphanTransaction(db, dummyHash(4)) shouldBe null
      index.getOrphanTransaction(db, dummyHash(5) ) shouldBe orphanTransaction(5)
    }

    "addOrphanTransactionByParent/getOrphanTransactionsByParent" should "successfully put/get orphan transactions" {
      index.getOrphanTransactionsByParent(db, dummyHash(0) ).isEmpty() shouldBe true
      index.addOrphanTransactionByParent(db, dummyHash(0), dummyHash(1) )
      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1))
      index.addOrphanTransactionByParent(db, dummyHash(0), dummyHash(2) )
      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2))
      index.addOrphanTransactionByParent(db, dummyHash(0), dummyHash(3) )
      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2), dummyHash(3))
    }

    "delOrphanTransactionsByParent" should "delete all orphan transactions that depend on the given missing transaction" {
      index.addOrphanTransactionByParent(db, dummyHash(0), dummyHash(1) )
      index.addOrphanTransactionByParent(db, dummyHash(0), dummyHash(2) )
      index.addOrphanTransactionByParent(db, dummyHash(3), dummyHash(4) )
      index.addOrphanTransactionByParent(db, dummyHash(3), dummyHash(5) )
      index.addOrphanTransactionByParent(db, dummyHash(6), dummyHash(7) )
      index.addOrphanTransactionByParent(db, dummyHash(6), dummyHash(8) )

      index.delOrphanTransactionsByParent(db, dummyHash(3) )

      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2))
      index.getOrphanTransactionsByParent(db, dummyHash(3) ).toSet().isEmpty() shouldBe true
      index.getOrphanTransactionsByParent(db, dummyHash(6) ).toSet() shouldBe setOf(dummyHash(7), dummyHash(8))

      index.delOrphanTransactionsByParent(db, dummyHash(0) )

      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet().isEmpty() shouldBe true
      index.getOrphanTransactionsByParent(db, dummyHash(3) ).toSet().isEmpty() shouldBe true
      index.getOrphanTransactionsByParent(db, dummyHash(6) ).toSet() shouldBe setOf(dummyHash(7), dummyHash(8))

      index.delOrphanTransactionsByParent(db, dummyHash(6) )

      index.getOrphanTransactionsByParent(db, dummyHash(0) ).toSet().isEmpty() shouldBe true
      index.getOrphanTransactionsByParent(db, dummyHash(3) ).toSet().isEmpty() shouldBe true
      index.getOrphanTransactionsByParent(db, dummyHash(6) ).toSet().isEmpty() shouldBe true

    }
  }
}


