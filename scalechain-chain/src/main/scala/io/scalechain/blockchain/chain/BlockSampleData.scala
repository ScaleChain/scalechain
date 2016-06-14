package io.scalechain.blockchain.chain

import io.scalechain.blockchain.transaction.CoinAmount
import io.scalechain.blockchain.script.HashSupported._
/**
  * Create following blocks for testing block reorganization.
  *
  * T0 :
  *   Genesis → B01(4) → B02a(4) : The best chain.
  *
  * T1 :
  *   Genesis → B01(4) → B02a(4) : (Still) the best chain.
  *                    ↘ B02b(4)
  *
  * T2 :
  *   Genesis → B01(4) → B02a(4) → B03a(4)
  *                    ↘ B02b(4) → B03b(5) : The best chain. (Block reorganization required)
  *
  * T3 :
  *   Genesis → B01(4) → B02a(4) → B03a(4) → B04a(4) : The best chain. ( Block reorganization required)
  *                    ↘ B02b(4) → B03b(5)
  */
class BlockSampleData extends BlockBuildingTestTrait {

  val Addr1 = generateAddress("Address1") // address 1
  val Addr2 = generateAddress("Address2") // address 2
  val Addr3 = generateAddress("Address3") // address 3

  object Tx {
    val GEN01 = generationTransaction( "GenTx.BLK01", CoinAmount(50), Addr1.address )
    val GEN02a = generationTransaction( "GenTx.BLK02", CoinAmount(50), Addr1.address )
    val GEN03a = generationTransaction( "GenTx.BLK03", CoinAmount(50), Addr1.address )
    val GEN04a = generationTransaction( "GenTx.BLK04", CoinAmount(50), Addr1.address )
    val GEN02b = generationTransaction( "GenTx.BLK02a", CoinAmount(50), Addr1.address )
    val GEN03b = generationTransaction( "GenTx.BLK03a", CoinAmount(50), Addr1.address )

    val TX01 = normalTransaction(
      "TX01",
      spendingOutputs = List( getOutput(GEN01,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(10), Addr2.address),
        NewOutput(CoinAmount(39), Addr1.address)
        // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX01 : 0,1

    val TX02 = normalTransaction(
      "TX02",
      spendingOutputs = List( getOutput(TX01,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(9), Addr2.address)
        // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX01 : 1
    // UTXO : TX02 : 0

    // Case 1 : Two transactions TX02a, TX02b in different block spends the same output. (conflict)
    val TX02a = normalTransaction(
      "TX02a",
      spendingOutputs = List( getOutput(TX01,1) ),
      newOutputs = List(
        NewOutput(CoinAmount(37), Addr2.address)
        // We have very expensive fee, 2 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX02 : 0
    val TX02b = normalTransaction(
      "TX02b",
      spendingOutputs = List( getOutput(TX01,1) ),
      newOutputs = List(
        NewOutput(CoinAmount(35), Addr2.address)
        // We have very expensive fee, 4 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX02 : 0

    val TX03 = normalTransaction(
      "TX03",
      spendingOutputs = List( getOutput(TX02,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(8), Addr2.address)
        // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX03 : 0
    // UTXO : Gen02 : 0

    // Case 2 : Two transactions TX03a, TX03b in different block spends different outputs. (no conflict)

    val TX03a = normalTransaction(
      "TX03a",
      spendingOutputs = List( getOutput(TX03,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(6), Addr2.address)
        // We have very expensive fee, 2 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : Gen02a : 0
    // UTXO : TX03a : 0

    // TX03b can't go into the transaction pool when the BLK04 becomes the best block,
    // as it depends on the output GEN02b created on the branch b.
    val TX03b = normalTransaction(
      "TX03b",
      spendingOutputs = List( getOutput(GEN02b,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(4), Addr2.address)
        // We have very expensive fee, 4 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : TX03 : 0
    // UTXO : TX03b : 0

    val TX04a = normalTransaction(
      "TX04a",
      spendingOutputs = List( getOutput(TX03a,0) ),
      newOutputs = List(
        NewOutput(CoinAmount(5), Addr2.address)
        // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
      )
    )
    // UTXO : Gen02a : 0
    // UTXO : Gen03a : 0
    // UTXO : TX04a : 0


  }

  object Block {
    val BLK01  = doMining( newBlock(env.GenesisBlockHash,  List(Tx.GEN01, Tx.TX01)), 4)
    val BLK02a = doMining( newBlock(BLK01.header.hash,     List(Tx.GEN02a, Tx.TX02, Tx.TX02a)), 4)
    val BLK03a = doMining( newBlock(BLK02a.header.hash,    List(Tx.GEN03a, Tx.TX03, Tx.TX03a)), 4)
    val BLK04a = doMining( newBlock(BLK03a.header.hash,    List(Tx.GEN04a, Tx.TX04a)), 4)

    val BLK02b = doMining( newBlock(BLK01.header.hash,   List(Tx.GEN02b, Tx.TX02, Tx.TX02b)), 4)
    val BLK03b = doMining( newBlock(BLK02b.header.hash,  List(Tx.GEN03b, Tx.TX03, Tx.TX03b)), 5)

  }

}
