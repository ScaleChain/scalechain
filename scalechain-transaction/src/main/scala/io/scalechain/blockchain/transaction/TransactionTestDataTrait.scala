package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.proto.{TransactionOutput, OutPoint, Hash}
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.script.{ScriptOpList, HashCalculator}
import io.scalechain.util.HexUtil

/**
  * Created by kangmo on 5/18/16.
  */
trait TransactionTestDataTrait extends ProtoTestData with ChainTestTrait {
  case class AddressData(privateKey : PrivateKey, publicKey : PublicKey, pubKeyScript : ParsedPubKeyScript, address : CoinAddress)

  /** Get rid of bytes data in OpCheckSig in the operations of ParsedPubKeyScript.
    *
    * This is necessary for our test automation.
    * If we parse a serialized script of script operations created from scala program, we will get script bytes attached to the OpCheckSig, OpCheckMultisig, etc..
    *
    * @param pubKeyScript The ParsedPubKeyScript to scrub script operations it it.
    * @return The ParsedPubKeyScript which has scrubbed script operations.
    */
  def scrubScript(pubKeyScript : ParsedPubKeyScript) : ParsedPubKeyScript = {
    pubKeyScript.copy(
      scriptOps = ScriptOpList(pubKeyScript.scriptOps.operations.map { op =>
        op match {
          case OpCheckSig(_) => OpCheckSig()
          case OpCheckMultiSig(_) => OpCheckMultiSig()
          case op => op
        }
      })
    )
  }

  /** Same as scrubScript, but checks from a list of output ownerships
    *
    * @param ownerships the list of ownerships to scrub scripts. Only ParsedPubKeyScript is scrubbed. CoinAddress remains untouched.
    * @return The scrubbed scripts.
    */
  def scrubScript(ownerships : List[OutputOwnership]) : List[OutputOwnership] = {
    ownerships.map { ownership =>
      ownership match {
        case p : ParsedPubKeyScript => scrubScript(p)
        case ownership => ownership
      }
    }
  }

  def scrubScript(ownership : OutputOwnership) : OutputOwnership = {
    ownership match {
      case p : ParsedPubKeyScript => scrubScript(p)
      case ownership => ownership
    }
  }

  def generateAddress() : AddressData = {

    val privateKey   = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    val address       = CoinAddress.from(privateKey)

    AddressData(
      privateKey,
      publicKey,
      publicKeyScript,
      address
    )
  }

  def generateTransactionOutput(value : Long, pubKeyScript : ParsedPubKeyScript) = {
    TransactionOutput( value, pubKeyScript.lockingScript )
  }

  val TXHASH1 = Hash( HashCalculator.transactionHash(transaction1) )
  val TXHASH2 = Hash( HashCalculator.transactionHash(transaction2) )
  val TXHASH3 = Hash( HashCalculator.transactionHash(transaction3) )
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

  val ADDR1 = generateAddress()
  val ADDR2 = generateAddress()
  val ADDR3 = generateAddress()

  val AMOUNT1 = 1000L
  val AMOUNT2 = 2000L
  val AMOUNT3 = 3000L

  val OUTPUT1 = generateTransactionOutput(AMOUNT1, ADDR1.pubKeyScript)
  val OUTPUT2 = generateTransactionOutput(AMOUNT2, ADDR2.pubKeyScript)
  val OUTPUT3 = generateTransactionOutput(AMOUNT3, ADDR3.pubKeyScript)

  val SIMPLE_SCRIPT_OPS_A = ScriptOpList(List(OpNum(2), OpEqual()))
  val SIMPLE_SCRIPT_OPS_B = ScriptOpList(List(OpNum(3), OpEqual()))

}

