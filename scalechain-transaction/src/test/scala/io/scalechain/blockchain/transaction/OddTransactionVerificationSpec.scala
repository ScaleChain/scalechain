package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.NormalTransactionInput
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.TransactionHash
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.proto.UnlockingScript
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.BlockPrinterSetter
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.blockchain.script.{HashCalculator, ScriptParser, BlockPrinterSetter}
import io.scalechain.util.HexUtil._
import io.scalechain.util.HexUtil._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Suite}

/** Test cases that have transactions failed verification after the release of v0.2.
  * We got these cases by running scalechain and logging BlovkVerifier.getFailures for every 1000 blocks.
  */
class OddTransactionVerificationSpec extends FlatSpec with BeforeAndAfterEach with SignatureTestTrait {

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
        "[[ErrorCode(invalid_signature_format)]message=ScriptOp:CheckSig]",
        MergedScript(
          transaction=
            Transaction(
              version=1,
              inputs=
                List(
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("406b2b06bcd34d3c8733e6b79f7a394c8a431fbf4ff5ac705c93f4076bb77602")),
                    outputIndex=0L,
                    unlockingScript=UnlockingScript(bytes("493046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd004104b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5")),
                    /* ops:ScriptOpList(operations=Array(
                        OpPush(73,ScriptBytes(bytes("3046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd00"))),
                        OpPush(65,ScriptBytes(bytes("04b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5"))))),
                        hashType:Some(0) */
                    sequenceNumber=0L)),
              outputs=
                List(
                  TransactionOutput(
                    value=4000000L,
                    lockingScript=LockingScript(bytes("76a9149a7b0f3b80c6baaeedce0a0842553800f832ba1f88ac"))
                    /* ops:ScriptOpList(operations=Array(
                        OpDup(),
                        OpHash160(),
                        OpPush(20,ScriptBytes(bytes("9a7b0f3b80c6baaeedce0a0842553800f832ba1f"))),
                        OpEqualVerify(),
                        OpCheckSig(Script(bytes("76a9149a7b0f3b80c6baaeedce0a0842553800f832ba1f88ac"))))) */ )),
                    lockTime=0L /* hash:bytes("c99c49da4c38af669dea436d3e73780dfdb6c1ecf9958baa52960e8baee30e73") */),
          inputIndex=0,
          unlockingScript=UnlockingScript(bytes("493046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd004104b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5"))
          /* ops:ScriptOpList(operations=Array(
            OpPush(73,ScriptBytes(bytes("3046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd00"))),
            OpPush(65,ScriptBytes(bytes("04b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5"))))),
            hashType:Some(0) */,
          lockingScript=LockingScript(bytes("76a914dc44b1164188067c3a32d4780f5996fa14a4f2d988ac"))
          /* ops:ScriptOpList(operations=Array(
            OpDup(),
            OpHash160(),
            OpPush(20,ScriptBytes(bytes("dc44b1164188067c3a32d4780f5996fa14a4f2d9"))),
            OpEqualVerify(),
            OpCheckSig(Script(bytes("76a914dc44b1164188067c3a32d4780f5996fa14a4f2d988ac"))))) */
        )
      )/*,

      ( "[[ErrorCode(invalid_signature_format)]message=ScriptOp:CheckMultiSig, invalid raw signature format.]",
        MergedScript(
          transaction=
            Transaction(
              version=1,
              inputs=
                List(
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("60a20bd93aa49ab4b28d514ec10b06e1829ce6818ec06cd3aabd013ebcdc4bb1")),
                    outputIndex=0L,
                    unlockingScript=UnlockingScript(bytes("0047304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01")),
                    /* ops:ScriptOpList(operations=Array(
                          Op0(),
                          OpPush(71,ScriptBytes(bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01"))))),
                          hashType:None */
                    sequenceNumber=4294967295L
                  )
                ),
                outputs=
                  List(
                    TransactionOutput(
                      value=1000000L,
                      lockingScript=LockingScript(bytes("76a914660d4ef3a743e3e696ad990364e555c271ad504b88ac"))
                      /* ops:ScriptOpList(operations=Array(
                            OpDup(),
                            OpHash160(),
                            OpPush(20,ScriptBytes(bytes("660d4ef3a743e3e696ad990364e555c271ad504b"))),
                            OpEqualVerify(),
                            OpCheckSig(Script(bytes("76a914660d4ef3a743e3e696ad990364e555c271ad504b88ac"))))) */
                    )
                  ),
                lockTime=0L
              /* hash:bytes("23b397edccd3740a74adb603c9756370fafcde9bcc4483eb271ecad09a94dd63") */
            ),
          inputIndex=0,
          unlockingScript=UnlockingScript(bytes("0047304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01"))
          /* ops:ScriptOpList(operations=Array(
                  Op0(),
                  OpPush(71,ScriptBytes(bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01"))))),
                  hashType:None */,
          lockingScript=LockingScript(bytes("514104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af52ae"))
          /* ops:ScriptOpList(operations=Array(
                  Op1(),
                  OpPush(65,ScriptBytes(bytes("04cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4"))),
                  OpPush(65,ScriptBytes(bytes("0461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af"))),
                  OpNum(2),
                  OpCheckMultiSig(Script(bytes("514104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af52ae"))))) */
        )
      )*/
    )


  "scripts" should "be leave true value on top of the stack" in {

    forAll(transactionInputs) { ( subject : String, mergedScript : MergedScript ) =>
      //      println("subject: "+subject)
      //      println("mergedScript: " + mergedScript )
      verifyTransactionInput(subject, mergedScript.transaction, mergedScript.inputIndex, mergedScript.lockingScript);
    }
  }

}
