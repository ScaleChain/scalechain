package io.scalechain.blockchain.chain

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAmount
import HashSupported._


/**
  * Created by kangmo on 6/30/16.
  */
class TransactionSampleData()(protected implicit val db : KeyValueDatabase) extends BlockBuildingTestTrait {
  val Addr1 = generateAccountAddress("Address1")
  // address 1
  val Addr2 = generateAccountAddress("Address2")
  // address 2
  val Addr3 = generateAccountAddress("Address3")

  // address 3

  object Tx {
    val GEN01 = generationTransaction("GenTx.BLK01", CoinAmount(50), Addr1.address)
    val GEN02 = generationTransaction("GenTx.BLK02", CoinAmount(50), Addr1.address)
    val GEN03 = generationTransaction("GenTx.BLK03", CoinAmount(50), Addr1.address)
    val GEN04 = generationTransaction("GenTx.BLK04", CoinAmount(50), Addr1.address)
    val GEN05 = generationTransaction("GenTx.BLK05", CoinAmount(50), Addr1.address)

    val TX03 = normalTransaction(
      "TX03",
      spendingOutputs = List( getOutput(GEN01,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(49), Addr2.address)
        // We have very expensive fee, 1 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0


    val TX04_01 = normalTransaction(
      "TX04_01",
      spendingOutputs = List( getOutput(GEN02,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(49), Addr2.address)
        // We have very expensive fee, 1 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_01 : 0


    val TX04_02 = normalTransaction(
      "TX04_02",
      spendingOutputs = List( getOutput(TX04_01,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(20), Addr1.address),
        NewOutput(CoinAmount(27), Addr3.address)
        // We have very expensive fee, 2 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_02 : 0,1


    val TX04_03 = normalTransaction(
      "TX04_03",
      spendingOutputs = List( getOutput(TX04_02,1) ),
      newOutputs = List(
        NewOutput(CoinAmount(10), Addr2.address),
        NewOutput(CoinAmount(13), Addr3.address)
        // We have very expensive fee, 4 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_03 : 0,1

    val TX04_04 = normalTransaction(
      "TX04_04",
      spendingOutputs = List( getOutput(TX03,0), getOutput(TX04_03, 1)),
      newOutputs = List(
        NewOutput(CoinAmount(10), Addr1.address),
        NewOutput(CoinAmount(10), Addr2.address),
        NewOutput(CoinAmount(10), Addr3.address),
        NewOutput(CoinAmount(10), Addr1.address),
        NewOutput(CoinAmount(10), Addr2.address)
        // We have very expensive fee, 49 + 13 - 50 = 12 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_03 : 0
    // UTXO : TX04_04 : 0,1,2,3,4


    val TX04_05_01 = normalTransaction(
      "TX04_05_01",
      spendingOutputs = List( getOutput(TX04_04,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(2), Addr1.address)
        // We have very expensive fee, 8 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 1,2,3,4

    val TX04_05_02 = normalTransaction(
      "TX04_05_02",
      spendingOutputs = List( getOutput(TX04_04,1) ),
      newOutputs = List(
        NewOutput(CoinAmount(4), Addr1.address)
        // We have very expensive fee, 6 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2,3,4

    val TX04_05_03 = normalTransaction(
      "TX04_05_03",
      spendingOutputs = List( getOutput(TX04_04,4) ),
      newOutputs = List(
        NewOutput(CoinAmount(6), Addr1.address)
        // We have very expensive fee, 4 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2,3

    val TX04_05_04 = normalTransaction(
      "TX04_05_04",
      spendingOutputs = List( getOutput(TX04_04,3) ),
      newOutputs = List(
        NewOutput(CoinAmount(8), Addr1.address)
        // We have very expensive fee, 2 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2

    val TX04_05_05 = normalTransaction(
      "TX04_05_05",
      spendingOutputs = List( getOutput(TX04_04,2) ),
      newOutputs = List(
        NewOutput(CoinAmount(10), Addr1.address)
        // We have very expensive fee, 0 SC
      )
    )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0

  }

  object Block {
    val BLK01 = doMining(newBlock(env.GenesisBlockHash, List(Tx.GEN01)), 4)
    val BLK02 = doMining(newBlock(BLK01.header.hash, List(Tx.GEN02)), 4)
    val BLK03 = doMining(newBlock(BLK02.header.hash, List(Tx.GEN03, Tx.TX03)), 4)
  }


}
