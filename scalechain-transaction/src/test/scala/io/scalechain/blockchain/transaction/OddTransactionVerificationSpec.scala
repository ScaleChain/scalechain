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
      ),
      (
        "[[ErrorCode(not_enough_input)]message=ScriptOp:OpDup",
        MergedScript(
          transaction=
            Transaction(
              version=1,
              inputs=
                List(
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("8697331c3124c8a4cf2f43afb5732374ea13769e42f10aa3a98148a08989af5e")),
                    outputIndex=0L,
                    unlockingScript=UnlockingScript(bytes("41042f40514a99be39b3e94345455f78c4dfbbcd7f9cc6e9d368e0438f444f05cf7169712955520c6dee6239b4274e7bfb173c32ab97c7faad5692fa9745256a21f8ac"))
                    /* ops:ScriptOpList(operations=Array(OpPush(65,ScriptBytes(bytes("042f40514a99be39b3e94345455f78c4dfbbcd7f9cc6e9d368e0438f444f05cf7169712955520c6dee6239b4274e7bfb173c32ab97c7faad5692fa9745256a21f8"))),OpCheckSig(Script(bytes("41042f40514a99be39b3e94345455f78c4dfbbcd7f9cc6e9d368e0438f444f05cf7169712955520c6dee6239b4274e7bfb173c32ab97c7faad5692fa9745256a21f8ac"))))), hashType:Some(-8) */,
                    sequenceNumber=4294967295L),
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("acb2e895a9b3664cfcfb13dd0321ef0c1205cf2aa082681dc6ba424af6456a42")),
                    outputIndex=1L,
                    unlockingScript=UnlockingScript(bytes(""))
                    /* ops:ScriptOpList(operations=Array()), hashType:None */,
                    sequenceNumber=4294967295L)),
              outputs=
                List(
                  TransactionOutput(
                    value=25000000000L,
                    lockingScript=LockingScript(bytes("76a9148483603b2aeb7287fd962836eb948f77181de2f188ac"))
                    /* ops:ScriptOpList(operations=Array(
                        OpDup(),
                        OpHash160(),
                        OpPush(20,ScriptBytes(bytes("8483603b2aeb7287fd962836eb948f77181de2f1"))),
                        OpEqualVerify(),
                        OpCheckSig(Script(bytes("76a9148483603b2aeb7287fd962836eb948f77181de2f188ac")))))
                     */ )),
              lockTime=0L /* hash:bytes("9ba14b850fb31afd5bc350fd1d9f53e3b99b2427e871ef90d1e125f47e87d7a0") */),
          inputIndex=1,
          unlockingScript=
            UnlockingScript(bytes(""))
            /* ops:ScriptOpList(operations=Array()), hashType:None */,
          lockingScript=LockingScript(bytes("76a91427d25a1ff9a6da31eeb991c48bb6cd95191a6b2c88ac"))
            /* ops:ScriptOpList(operations=Array(
              OpDup(),
              OpHash160(),
              OpPush(20,ScriptBytes(bytes("27d25a1ff9a6da31eeb991c48bb6cd95191a6b2c"))),
              OpEqualVerify(),
              OpCheckSig(Script(bytes("76a91427d25a1ff9a6da31eeb991c48bb6cd95191a6b2c88ac")))))
             */ )
      ),
      (
        "[[ErrorCode(not_enough_input)]message=ScriptOp:CheckSig]",
        MergedScript(
          transaction=
            Transaction(
              version=1,
              inputs=
                List(
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("cfbba5a04f23049b63941e3ff5d57aa1431671281f2e3ade1a7050ffdd284377")),
                    outputIndex=0L,
                    unlockingScript=UnlockingScript(bytes("4104ceee1b864e673eb4492b9b0c8c938fea6343dba5556cff41beee4ad994a6d2365b80751ba263d160e1fdc4523ac46cee651ad21c2f5c629ec0ed060ac1f51b39ac"))
                    /* ops:ScriptOpList(operations=Array(
                         OpPush(65,ScriptBytes(bytes("04ceee1b864e673eb4492b9b0c8c938fea6343dba5556cff41beee4ad994a6d2365b80751ba263d160e1fdc4523ac46cee651ad21c2f5c629ec0ed060ac1f51b39"))),
                         OpCheckSig(Script(bytes("4104ceee1b864e673eb4492b9b0c8c938fea6343dba5556cff41beee4ad994a6d2365b80751ba263d160e1fdc4523ac46cee651ad21c2f5c629ec0ed060ac1f51b39ac"))))),
                         hashType:Some(57) */,
                    sequenceNumber=4294967295L),
                  NormalTransactionInput(
                    outputTransactionHash=TransactionHash(bytes("282b861e411dc3b61aa06e9e13abf49bce5c571e21a19c37f738244cee33b778")),
                    outputIndex=0L,
                    unlockingScript=UnlockingScript(bytes(""))
                    /* ops:ScriptOpList(operations=Array()), hashType:None */,
                    sequenceNumber=4294967295L)),
              outputs=List(
                TransactionOutput(
                  value=10000000000L,
                  lockingScript=LockingScript(bytes("41042c0960594d5e48ccc8edb8a7ac622d5b542a1058257aecbf5f2e75ae8cfffeb98ac6cd8aa268a671857053c2d094725ffa2d22c5fc7872ddb6ffc8ee298a3ec9ac"))
                  /* ops:ScriptOpList(operations=Array(
                       OpPush(65,ScriptBytes(bytes("042c0960594d5e48ccc8edb8a7ac622d5b542a1058257aecbf5f2e75ae8cfffeb98ac6cd8aa268a671857053c2d094725ffa2d22c5fc7872ddb6ffc8ee298a3ec9"))),
                       OpCheckSig(Script(bytes("41042c0960594d5e48ccc8edb8a7ac622d5b542a1058257aecbf5f2e75ae8cfffeb98ac6cd8aa268a671857053c2d094725ffa2d22c5fc7872ddb6ffc8ee298a3ec9ac"))))) */
                )),
              lockTime=0L
              /* hash:bytes("28c225b3e2b54e9bb11f814f033c70fc12d25a504aee54735aad669152907694") */
            ), inputIndex=1,
          unlockingScript=UnlockingScript(bytes(""))
          /* ops:ScriptOpList(operations=Array()), hashType:None */,
          lockingScript=LockingScript(bytes("410480b412face904ee2993e5846328fa538411accaf20961ae0ca2880077802158b9df38f7c11bf78b0bca275035165c2b2889315ffefa05ea7b4b405c4e6d22244ac"))
          /* ops:ScriptOpList(operations=Array(
               OpPush(65,ScriptBytes(bytes("0480b412face904ee2993e5846328fa538411accaf20961ae0ca2880077802158b9df38f7c11bf78b0bca275035165c2b2889315ffefa05ea7b4b405c4e6d22244"))),
               OpCheckSig(Script(bytes("410480b412face904ee2993e5846328fa538411accaf20961ae0ca2880077802158b9df38f7c11bf78b0bca275035165c2b2889315ffefa05ea7b4b405c4e6d22244ac"))))) */
        )
      ),
      ( "[[ErrorCode(invalid_signature_format)]message=ScriptOp:CheckMultiSig, invalid raw signature format.]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("60a20bd93aa49ab4b28d514ec10b06e1829ce6818ec06cd3aabd013ebcdc4bb1")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("0047304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(71,ScriptBytes(bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01"))))), hashType:None */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=1000000L, lockingScript=LockingScript(bytes("76a914660d4ef3a743e3e696ad990364e555c271ad504b88ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("660d4ef3a743e3e696ad990364e555c271ad504b"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914660d4ef3a743e3e696ad990364e555c271ad504b88ac"))))) */ )), lockTime=0L /* hash:bytes("23b397edccd3740a74adb603c9756370fafcde9bcc4483eb271ecad09a94dd63") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("0047304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(71,ScriptBytes(bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01"))))), hashType:None */, lockingScript=LockingScript(bytes("514104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af52ae")) /* ops:ScriptOpList(operations=Array(Op1(),OpPush(65,ScriptBytes(bytes("04cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4"))),OpPush(65,ScriptBytes(bytes("0461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af"))),OpNum(2),OpCheckMultiSig(Script(bytes("514104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af52ae"))))) */ )
      ),

      ( "[Not enough stack values after the script execution.]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("055707ce7fea7b9776fdc70413f65ceec413d46344424ab01acd5138767db137")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("76a914f7d46c08dd53bc6bbb52178d60b3fc99a9c1fb8788ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("f7d46c08dd53bc6bbb52178d60b3fc99a9c1fb87"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914f7d46c08dd53bc6bbb52178d60b3fc99a9c1fb8788ac"))))), hashType:None */, sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("055707ce7fea7b9776fdc70413f65ceec413d46344424ab01acd5138767db137")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")) /* ops:ScriptOpList(operations=Array()), hashType:None */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=4800000L, lockingScript=LockingScript(bytes("76a9140d7713649f9a0678f4e880b40f86b93289d1bb2788ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("0d7713649f9a0678f4e880b40f86b93289d1bb27"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9140d7713649f9a0678f4e880b40f86b93289d1bb2788ac"))))) */ )), lockTime=0L /* hash:bytes("37bb4a87199131cf94e07017fb1129944e0e4bee3c54fc1c9a2f79acab6c15c6") */), inputIndex=1, unlockingScript=UnlockingScript(bytes("")) /* ops:ScriptOpList(operations=Array()), hashType:None */, lockingScript=LockingScript(bytes("142a9bc5447d664c1d0141392a842d23dba45c4f13b175")) /* ops:ScriptOpList(operations=Array(OpPush(20,ScriptBytes(bytes("2a9bc5447d664c1d0141392a842d23dba45c4f13"))),OpNopN(2),OpDrop())) */ )
      ),

      ( " [[ErrorCode(not_enough_input)]message=ScriptOp:OpSHA256]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("263de5bc678c8eb49cd774c0707d95d2b38d591ebce5ce5f62c3e1a3f3286c6b")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("76a91447b5412406a637b07caa759d6b55422d90671d2388ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("47b5412406a637b07caa759d6b55422d90671d23"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a91447b5412406a637b07caa759d6b55422d90671d2388ac"))))), hashType:None */, sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("9969603dca74d14d29d1d5f56b94c7872551607f8c2d6837ab9715c60721b50e")), outputIndex=2L, unlockingScript=UnlockingScript(bytes("")) /* ops:ScriptOpList(operations=Array()), hashType:None */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=1000000L, lockingScript=LockingScript(bytes("76a91420e9fc9cf541aa0439ff5ddbeff3ebd316b1a10788ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("20e9fc9cf541aa0439ff5ddbeff3ebd316b1a107"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a91420e9fc9cf541aa0439ff5ddbeff3ebd316b1a10788ac"))))) */ )), lockTime=0L /* hash:bytes("c0b428eed3f012dbf14e712ad3de6d286125f415e6e7accb6ddfcd1c645455ee") */), inputIndex=1, unlockingScript=UnlockingScript(bytes("")) /* ops:ScriptOpList(operations=Array()), hashType:None */, lockingScript=LockingScript(bytes("0804678afd04678afd75a820894eeb82f9a851f5d1cb1be3249f58bc8d259963832c5e7474a76f7a859ee95c87")) /* ops:ScriptOpList(operations=Array(OpPush(8,ScriptBytes(bytes("04678afd04678afd"))),OpDrop(),OpSHA256(),OpPush(32,ScriptBytes(bytes("894eeb82f9a851f5d1cb1be3249f58bc8d259963832c5e7474a76f7a859ee95c"))),OpEqual())) */ )
      )
    )

  "scripts" should "be leave true value on top of the stack" in {

    forAll(transactionInputs) { ( subject : String, mergedScript : MergedScript ) =>
      //      println("subject: "+subject)
      //      println("mergedScript: " + mergedScript )
      verifyTransactionInput(subject, mergedScript.transaction, mergedScript.inputIndex, mergedScript.lockingScript);
    }
  }


}
