package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.script.ops.*
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.hash

/**
  * Created by kangmo on 5/18/16.
  */
interface TransactionTestDataTrait : ChainTestTrait, ProtoTestData {
  data class AddressData(val privateKey : PrivateKey, val publicKey : PublicKey, val pubKeyScript : ParsedPubKeyScript, val address : CoinAddress)

  /** Get rid of bytes data in OpCheckSig in the operations of ParsedPubKeyScript.
    *
    * This is necessary for our test automation.
    * If we parse a serialized script of script operations created from scala program, we will get script bytes attached to the OpCheckSig, OpCheckMultisig, etc..
    *
    * @param pubKeyScript The ParsedPubKeyScript to scrub script operations it it.
    * @return The ParsedPubKeyScript which has scrubbed script operations.
    */
  fun scrubScript(pubKeyScript : ParsedPubKeyScript) : ParsedPubKeyScript {
    return pubKeyScript.copy(
      scriptOps = ScriptOpList(pubKeyScript.scriptOps.operations.map { op ->
        when(op) {
          is OpCheckSig -> OpCheckSig()
          is OpCheckMultiSig -> OpCheckMultiSig()
          else -> op
        }
      })
    )
  }

  /** Same as scrubScript, but checks from a list of output ownerships
    *
    * @param ownerships the list of ownerships to scrub scripts. Only ParsedPubKeyScript is scrubbed. CoinAddress remains untouched.
    * @return The scrubbed scripts.
    */
  fun scrubScript(ownerships : List<OutputOwnership>) : List<OutputOwnership> {
    return ownerships.map { ownership -> scrubScript(ownership) }
  }

  fun scrubScript(ownership : OutputOwnership) : OutputOwnership {
    return when(ownership) {
      is ParsedPubKeyScript -> scrubScript(ownership)
      else -> ownership
    }
  }

  fun generateAddress() : AddressData {
    val privateKey   = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey)
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    val address       = CoinAddress.from(privateKey)

    return AddressData(
      privateKey,
      publicKey,
      publicKeyScript,
      address
    )
  }

  fun generateTransactionOutput(value : Long, pubKeyScript : ParsedPubKeyScript) {
    TransactionOutput( value, pubKeyScript.lockingScript() )
  }

  companion object {
      private val txTestData = object : TransactionTestDataTrait {}
      val TXHASH1 = ProtoTestData.transaction1.hash()
      val TXHASH2 = ProtoTestData.transaction2.hash()
      val TXHASH3 = ProtoTestData.transaction3.hash()
      /*
        val DUMMY_TXHASH1 = Hash
        val DUMMY_TXHASH2 =
        val DUMMY_TXHASH3 =
        val DUMMY_TXHASH4 =
        val DUMMY_TXHASH5 =
      */
      val OUTPOINT1 = OutPoint(TXHASH1, 0)
      val OUTPOINT2 = OutPoint(TXHASH2, 0)
      val OUTPOINT3 = OutPoint(TXHASH3, 0)

      val ADDR1 = txTestData.generateAddress()
      val ADDR2 = txTestData.generateAddress()
      val ADDR3 = txTestData.generateAddress()

      val AMOUNT1 = 1000L
      val AMOUNT2 = 2000L
      val AMOUNT3 = 3000L

      val OUTPUT1 = txTestData.generateTransactionOutput(AMOUNT1, ADDR1.pubKeyScript)
      val OUTPUT2 = txTestData.generateTransactionOutput(AMOUNT2, ADDR2.pubKeyScript)
      val OUTPUT3 = txTestData.generateTransactionOutput(AMOUNT3, ADDR3.pubKeyScript)

      val SIMPLE_SCRIPT_OPS_A = ScriptOpList(listOf(OpNum(2), OpEqual()))
      val SIMPLE_SCRIPT_OPS_B = ScriptOpList(listOf(OpNum(3), OpEqual()))
  }
}

