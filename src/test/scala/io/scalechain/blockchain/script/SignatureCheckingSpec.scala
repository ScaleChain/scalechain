package io.scalechain.blockchain.script

import io.scalechain.blockchain.block._
import io.scalechain.blockchain.script.ops.{ScriptOp, OpRIPEMD160}
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import org.scalatest.{Suite, BeforeAndAfterEach, FlatSpec}

case class MergedScript(transaction:Transaction, inputIndex:Int, unlockingScript:UnlockingScript, lockingScript:LockingScript)

/** Test signature validation operations in Crypto.scala
  *
  */
class SignatureCheckingSpec extends FlatSpec with BeforeAndAfterEach with SignatureTestTrait {

  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val transactionInputs =
    Table(
      // column names
      ("subject", "mergedScript"),

      // Summarize the locking/unlocking script as a subject.
      // The MergedScript creating code was copied from the output of DumpChain merge-scripts,
      // which reads all transactions from blkNNNNN.dat file written by the reference implementation.
      (
        "p2pkh",
        // Caution : Do not edit this line. This line is copied from the output of DumpChain program.
        MergedScript(transaction=Transaction(version=1, inputs=Array(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("7b5afb2a1721edee10e4f7af58b6ee509d1ea43308f6f30cf31dfacb67ebf712")), outputIndex=2, unlockingScript=UnlockingScript(bytes("4830450221008f70e4947b1dc666cb6b8296ebb7a9cca3d7eb55740213c7c19673dd9b1b66f40220634bc7e806fbca468aeade5da23160770188582b218174ed91c0bb619771cbc50121031c24239a829a89d7e12a0a5b1456ce60168c2c7dd29b63ea6a2aa8ef64665050")) /* ops:ScriptOpList(ops:OpPush(72,ScriptBytes(bytes("30450221008f70e4947b1dc666cb6b8296ebb7a9cca3d7eb55740213c7c19673dd9b1b66f40220634bc7e806fbca468aeade5da23160770188582b218174ed91c0bb619771cbc501"))),OpPush(33,ScriptBytes(bytes("031c24239a829a89d7e12a0a5b1456ce60168c2c7dd29b63ea6a2aa8ef64665050")))), hashType:Some(1) */, sequenceNumber= -1)), outputs=Array(TransactionOutput(value=100000000, lockingScript=LockingScript(bytes("76a91426b8b5d8bc8548c5176d1d8e9046320dc35d8ff588ac")) /* ops:ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("26b8b5d8bc8548c5176d1d8e9046320dc35d8ff5"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a91426b8b5d8bc8548c5176d1d8e9046320dc35d8ff588ac")))) */ ),TransactionOutput(value=4504811438L, lockingScript=LockingScript(bytes("76a914425d9c60c16e8364c2125f2560c8d3e847c0827988ac")) /* ops:ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("425d9c60c16e8364c2125f2560c8d3e847c08279"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914425d9c60c16e8364c2125f2560c8d3e847c0827988ac")))) */ ),TransactionOutput(value=500000000, lockingScript=LockingScript(bytes("76a914e55ce4ca624664c18f66165780eeb36ee68fbb7888ac")) /* ops:ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("e55ce4ca624664c18f66165780eeb36ee68fbb78"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914e55ce4ca624664c18f66165780eeb36ee68fbb7888ac")))) */ )), lockTime=0 /* hash:bytes("a14b51fa2fbcb686e520c463dcf5f19283af7bb8c08cd685c2a09700d027167c") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("4830450221008f70e4947b1dc666cb6b8296ebb7a9cca3d7eb55740213c7c19673dd9b1b66f40220634bc7e806fbca468aeade5da23160770188582b218174ed91c0bb619771cbc50121031c24239a829a89d7e12a0a5b1456ce60168c2c7dd29b63ea6a2aa8ef64665050")) /* ops:ScriptOpList(ops:OpPush(72,ScriptBytes(bytes("30450221008f70e4947b1dc666cb6b8296ebb7a9cca3d7eb55740213c7c19673dd9b1b66f40220634bc7e806fbca468aeade5da23160770188582b218174ed91c0bb619771cbc501"))),OpPush(33,ScriptBytes(bytes("031c24239a829a89d7e12a0a5b1456ce60168c2c7dd29b63ea6a2aa8ef64665050")))), hashType:Some(1) */, lockingScript=LockingScript(bytes("76a914425d9c60c16e8364c2125f2560c8d3e847c0827988ac")) /* ops:ScriptOpList(ops:OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("425d9c60c16e8364c2125f2560c8d3e847c08279"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914425d9c60c16e8364c2125f2560c8d3e847c0827988ac")))) */ )
      )
    )

  "scripts" should "be leave true value on top of the stack" in {
    forAll(transactionInputs) { ( subject : String, mergedScript : MergedScript ) =>
      verifyTransactionInput(subject, mergedScript.transaction, mergedScript.inputIndex, mergedScript.lockingScript);
    }
  }


}
