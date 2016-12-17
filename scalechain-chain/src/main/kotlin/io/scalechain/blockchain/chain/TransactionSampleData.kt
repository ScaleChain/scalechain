package io.scalechain.blockchain.chain

import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAmount
import io.scalechain.blockchain.transaction.TransactionGeneratorBlockchainView

/**
 * Test data for testing blocks and signed transactions.
 * @params chainView ; if this is not null, transactions are signed in the generated test data.
 */
class TransactionSampleData(override val db : KeyValueDatabase, val chainView : TransactionGeneratorBlockchainView? = null) : AbstractBlockBuildingTest(chainView) {

  val Addr1 = generateAccountAddress("Address1")
  // address 1
  val Addr2 = generateAccountAddress("Address2")
  // address 2
  val Addr3 = generateAccountAddress("Address3")

  // Private keys for signing transactions.
  val AllPrivateKeys = listOf(Addr1, Addr2, Addr3).map { it.privateKey }

  // Add a transaction to the view. Because the transaction is visible to other transactions, the transaction's outputs can be spent by other transactions.
  fun add(txWithName : TransactionWithName) : TransactionWithName {
    chainView?.addTransaction(txWithName.transaction)
    return txWithName
  }

  inner class TxClass {
    val GEN01 = add(generationTransaction("GenTx.BLK01", CoinAmount(50), Addr1.address))
    val GEN02 = add(generationTransaction("GenTx.BLK02", CoinAmount(50), Addr1.address))
    val GEN03 = add(generationTransaction("GenTx.BLK03", CoinAmount(50), Addr1.address))
    val GEN04 = add(generationTransaction("GenTx.BLK04", CoinAmount(50), Addr1.address))
    val GEN05 = add(generationTransaction("GenTx.BLK05", CoinAmount(50), Addr1.address))



    val TX03 = add( normalTransaction(
      "TX03",
      spendingOutputs = listOf( getOutput(GEN01,0) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(49), Addr2.address)
        // We have very expensive fee, 1 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0


    val TX04_01 = add( normalTransaction(
      "TX04_01",
      spendingOutputs = listOf( getOutput(GEN02,0) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(49), Addr2.address)
        // We have very expensive fee, 1 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_01 : 0


    val TX04_02 = add( normalTransaction(
      "TX04_02",
      spendingOutputs = listOf( getOutput(TX04_01,0) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(20), Addr1.address),
        NewOutput(CoinAmount(27), Addr3.address)
        // We have very expensive fee, 2 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_02 : 0,1


    val TX04_03 = add( normalTransaction(
      "TX04_03",
      spendingOutputs = listOf( getOutput(TX04_02,1) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(10), Addr2.address),
        NewOutput(CoinAmount(13), Addr3.address)
        // We have very expensive fee, 4 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_03 : 0,1

    val TX04_04 = add( normalTransaction(
      "TX04_04",
      spendingOutputs = listOf( getOutput(TX03,0), getOutput(TX04_03, 1)),
      newOutputs = listOf(
        NewOutput(CoinAmount(10), Addr1.address),
        NewOutput(CoinAmount(10), Addr2.address),
        NewOutput(CoinAmount(10), Addr3.address),
        NewOutput(CoinAmount(10), Addr1.address),
        NewOutput(CoinAmount(10), Addr2.address)
        // We have very expensive fee, 49 + 13 - 50 = 12 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_03 : 0
    // UTXO : TX04_04 : 0,1,2,3,4


    val TX04_05_01 = add( normalTransaction(
      "TX04_05_01",
      spendingOutputs = listOf( getOutput(TX04_04,0) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(2), Addr1.address)
        // We have very expensive fee, 8 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 1,2,3,4

    val TX04_05_02 = add( normalTransaction(
      "TX04_05_02",
      spendingOutputs = listOf( getOutput(TX04_04,1) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(4), Addr1.address)
        // We have very expensive fee, 6 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2,3,4

    val TX04_05_03 = add( normalTransaction(
      "TX04_05_03",
      spendingOutputs = listOf( getOutput(TX04_04,4) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(6), Addr1.address)
        // We have very expensive fee, 4 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2,3

    val TX04_05_04 = add( normalTransaction(
      "TX04_05_04",
      spendingOutputs = listOf( getOutput(TX04_04,3) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(8), Addr1.address)
        // We have very expensive fee, 2 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0
    // UTXO : TX04_04 : 2

    val TX04_05_05 = add( normalTransaction(
      "TX04_05_05",
      spendingOutputs = listOf( getOutput(TX04_04,2) ),
      newOutputs = listOf(
        NewOutput(CoinAmount(10), Addr1.address)
        // We have very expensive fee, 0 SC
      ),
      privateKeys = AllPrivateKeys
    ) )
    // UTXO : GEN02 : 0
    // UTXO : GEN03 : 0
    // UTXO : TX04_02 : 0

  }
  val Tx = TxClass()

  inner class BlockClass {
    val BLK01 = doMining(newBlock(env().GenesisBlockHash, listOf(Tx.GEN01)), 4)
    val BLK02 = doMining(newBlock(BLK01.header.hash(), listOf(Tx.GEN02)), 4)
    val BLK03 = doMining(newBlock(BLK02.header.hash(), listOf(Tx.GEN03, Tx.TX03)), 4)
  }
  val Block = BlockClass()

}
