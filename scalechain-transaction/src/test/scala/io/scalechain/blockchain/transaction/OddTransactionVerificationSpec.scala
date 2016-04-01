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
/*
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
      ),
*/
      // TODO : need to fix this.
      /*
        (
          "[[ErrorCode(invalid_signature_format)]message=ScriptOp:CheckSig]-signature length = 75",
          MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("aa85c2dd8bc29ef4015ed110ba543ea8adbccee2d1f3f51af33fc145c4aa1623")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("4b3048022200002b83d59c1d23c08efd82ee0662fec23309c3adbcbd1f0b8695378db4b14e736602220000334a96676e58b1bb01784cb7c556dd8ce1c220171904da22e18fe1e7d1510db5014104d0fe07ff74c9ef5b00fed1104fad43ecf72dbab9e60733e4f56eacf24b20cf3b8cd945bcabcc73ba0158bf9ce769d43e94bd58c5c7e331a188922b3fe9ca1f5a")) /* ops:ScriptOpList(operations=Array(OpPush(75,ScriptBytes(bytes("3048022200002b83d59c1d23c08efd82ee0662fec23309c3adbcbd1f0b8695378db4b14e736602220000334a96676e58b1bb01784cb7c556dd8ce1c220171904da22e18fe1e7d1510db501"))),OpPush(65,ScriptBytes(bytes("04d0fe07ff74c9ef5b00fed1104fad43ecf72dbab9e60733e4f56eacf24b20cf3b8cd945bcabcc73ba0158bf9ce769d43e94bd58c5c7e331a188922b3fe9ca1f5a"))))), hashType:Some(1) */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=3000000L, lockingScript=LockingScript(bytes("76a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("7a2a3b481ca80c4ba7939c54d9278e50189d94f9"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac"))))) */ )), lockTime=0L /* hash:bytes("fb0a1d8d34fa5537e461ac384bac761125e1bfa7fec286fa72511240fa66864d") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("4b3048022200002b83d59c1d23c08efd82ee0662fec23309c3adbcbd1f0b8695378db4b14e736602220000334a96676e58b1bb01784cb7c556dd8ce1c220171904da22e18fe1e7d1510db5014104d0fe07ff74c9ef5b00fed1104fad43ecf72dbab9e60733e4f56eacf24b20cf3b8cd945bcabcc73ba0158bf9ce769d43e94bd58c5c7e331a188922b3fe9ca1f5a")) /* ops:ScriptOpList(operations=Array(OpPush(75,ScriptBytes(bytes("3048022200002b83d59c1d23c08efd82ee0662fec23309c3adbcbd1f0b8695378db4b14e736602220000334a96676e58b1bb01784cb7c556dd8ce1c220171904da22e18fe1e7d1510db501"))),OpPush(65,ScriptBytes(bytes("04d0fe07ff74c9ef5b00fed1104fad43ecf72dbab9e60733e4f56eacf24b20cf3b8cd945bcabcc73ba0158bf9ce769d43e94bd58c5c7e331a188922b3fe9ca1f5a"))))), hashType:Some(1) */, lockingScript=LockingScript(bytes("76a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("7a2a3b481ca80c4ba7939c54d9278e50189d94f9"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac"))))) */ )
        )
*/
        (
          "[[ErrorCode(not_enough_input)]message=ScriptOp:CheckMultiSig, the total number of stack items]",
          MergedScript(
            transaction=
              Transaction(
                version=1,
                inputs=List(
                  NormalTransactionInput(
                    outputTransactionHash=
                      TransactionHash(bytes("9c08a4d78931342b37fd5f72900fb9983087e6f46c4a097d8a1f52c74e28eaf6")),
                    outputIndex=1L,
                    unlockingScript=UnlockingScript(bytes("255121029b6d2c97b8b7c718c325d7be3ac30f7c9d67651bce0c929f55ee77ce58efcf8451ae")),
                    /* ops:ScriptOpList(operations=Array(
                          OpPush(37,ScriptBytes(bytes("5121029b6d2c97b8b7c718c325d7be3ac30f7c9d67651bce0c929f55ee77ce58efcf8451ae"))))),
                          hashType:Some(-82) */
                    sequenceNumber=4294967295L)),
                outputs=List(
                  TransactionOutput(
                    value=350000L,
                    lockingScript=LockingScript(bytes("76a9145a3acbc7bbcc97c5ff16f5909c9d7d3fadb293a888ac"))
                    /* ops:ScriptOpList(operations=Array(
                      OpDup(),
                      OpHash160(),
                      OpPush(20,ScriptBytes(bytes("5a3acbc7bbcc97c5ff16f5909c9d7d3fadb293a8"))),
                      OpEqualVerify(),
                      OpCheckSig(Script(bytes("76a9145a3acbc7bbcc97c5ff16f5909c9d7d3fadb293a888ac"))))) */ )
                ),
                lockTime=0L /* hash:bytes("6a26d2ecb67f27d1fa5524763b49029d7106e91e3cc05743073461a719776192") */),
            inputIndex=0,
            unlockingScript=UnlockingScript(bytes("255121029b6d2c97b8b7c718c325d7be3ac30f7c9d67651bce0c929f55ee77ce58efcf8451ae"))
            /* ops:ScriptOpList(operations=Array(
                  OpPush(37,ScriptBytes(bytes("5121029b6d2c97b8b7c718c325d7be3ac30f7c9d67651bce0c929f55ee77ce58efcf8451ae"))))),
                  hashType:Some(-82) */,
            lockingScript=LockingScript(bytes("a91419a7d869032368fd1f1e26e5e73a4ad0e474960e87"))
            /* ops:ScriptOpList(operations=Array(
                  OpHash160(),
                  OpPush(20,ScriptBytes(bytes("19a7d869032368fd1f1e26e5e73a4ad0e474960e"))),
                  OpEqual())) */ )
        )
/*
      (
        "[[ErrorCode(invalid_signature_format)]message=ScriptOp:CheckMultiSig, invalid raw signature format.]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("14237b92d26850730ffab1bfb138121e487ddde444734ef195eb7928102bc939")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("0047304402208fc06d216ebb4b6a3a3e0f906e1512c372fa8a9c2a92505d04e9b451ea7acd0c0220764303bb7e514ddd77855949d941c934e9cbda8e3c3827bfdb5777477e73885b014730440220569ec6d2e81625dd18c73920e0079cdb4c1d67d3d7616759eb0c18cf566b3d3402201c60318f0a62e3ba85ca0f158d4dfe63c0779269eb6765b6fc939fc51e7a8ea901")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(71,ScriptBytes(bytes("304402208fc06d216ebb4b6a3a3e0f906e1512c372fa8a9c2a92505d04e9b451ea7acd0c0220764303bb7e514ddd77855949d941c934e9cbda8e3c3827bfdb5777477e73885b01"))),OpPush(71,ScriptBytes(bytes("30440220569ec6d2e81625dd18c73920e0079cdb4c1d67d3d7616759eb0c18cf566b3d3402201c60318f0a62e3ba85ca0f158d4dfe63c0779269eb6765b6fc939fc51e7a8ea901"))))), hashType:None */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=25000000L, lockingScript=LockingScript(bytes("76a914641ad5051edd97029a003fe9efb29359fcee409d88ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("641ad5051edd97029a003fe9efb29359fcee409d"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a914641ad5051edd97029a003fe9efb29359fcee409d88ac"))))) */ )), lockTime=0L /* hash:bytes("10c61e258e0a2b19b245a96a2d0a1538fe81cd4ecd547e0a3df7ed6fd3761ada") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("0047304402208fc06d216ebb4b6a3a3e0f906e1512c372fa8a9c2a92505d04e9b451ea7acd0c0220764303bb7e514ddd77855949d941c934e9cbda8e3c3827bfdb5777477e73885b014730440220569ec6d2e81625dd18c73920e0079cdb4c1d67d3d7616759eb0c18cf566b3d3402201c60318f0a62e3ba85ca0f158d4dfe63c0779269eb6765b6fc939fc51e7a8ea901")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(71,ScriptBytes(bytes("304402208fc06d216ebb4b6a3a3e0f906e1512c372fa8a9c2a92505d04e9b451ea7acd0c0220764303bb7e514ddd77855949d941c934e9cbda8e3c3827bfdb5777477e73885b01"))),OpPush(71,ScriptBytes(bytes("30440220569ec6d2e81625dd18c73920e0079cdb4c1d67d3d7616759eb0c18cf566b3d3402201c60318f0a62e3ba85ca0f158d4dfe63c0779269eb6765b6fc939fc51e7a8ea901"))))), hashType:None */, lockingScript=LockingScript(bytes("52410496ec45f878b62c46c4be8e336dff7cc58df9b502178cc240eb3d31b1266f69f5767071aa3e017d1b82a0bb28dab5e27d4d8e9725b3e68ed5f8a2d45c730621e34104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af53ae")) /* ops:ScriptOpList(operations=Array(OpNum(2),OpPush(65,ScriptBytes(bytes("0496ec45f878b62c46c4be8e336dff7cc58df9b502178cc240eb3d31b1266f69f5767071aa3e017d1b82a0bb28dab5e27d4d8e9725b3e68ed5f8a2d45c730621e3"))),OpPush(65,ScriptBytes(bytes("04cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4"))),OpPush(65,ScriptBytes(bytes("0461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af"))),OpNum(3),OpCheckMultiSig(Script(bytes("52410496ec45f878b62c46c4be8e336dff7cc58df9b502178cc240eb3d31b1266f69f5767071aa3e017d1b82a0bb28dab5e27d4d8e9725b3e68ed5f8a2d45c730621e34104cc71eb30d653c0c3163990c47b976f3fb3f37cccdcbedb169a1dfef58bbfbfaff7d8a473e7e2e6d317b87bafe8bde97e3cf8f065dec022b51d11fcdd0d348ac4410461cbdcc5409fb4b4d42b51d33381354d80e550078cb532a34bfa2fcfdeb7d76519aecc62770f5b0e4ef8551946d8a540911abe3e7854a26f39f58b25c15342af53ae"))))) */ )
      )
*/
      ,
      (
        "[message=Incorrect length for infinity encoding]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("274f8be3b7b9b1a220285f5f71f61e2691dd04df9d69bb02a8b3b85f91fb1857")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("004830450221008c2107ed4e026ab4319a591e8d9ec37719cdea053951c660566e3a3399428af502202ecd823d5f74a77cc2159d8af2d3ea5d36a702fef9a7edaaf562aef22ac35da401")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(72,ScriptBytes(bytes("30450221008c2107ed4e026ab4319a591e8d9ec37719cdea053951c660566e3a3399428af502202ecd823d5f74a77cc2159d8af2d3ea5d36a702fef9a7edaaf562aef22ac35da401"))))), hashType:None */, sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("70c15eb4cc3890960dbe1ae0cf13eedaeaef04d8e4820398fb4e991b23528f03")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("0047304402200487cd787fde9b337ab87f9fe54b9fd46d5d1692aa58e97147a4fe757f6f944202203cbcfb9c0fc4e3c453938bbea9e5ae64030cf7a97fafaf460ea2cb54ed5651b501")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(71,ScriptBytes(bytes("304402200487cd787fde9b337ab87f9fe54b9fd46d5d1692aa58e97147a4fe757f6f944202203cbcfb9c0fc4e3c453938bbea9e5ae64030cf7a97fafaf460ea2cb54ed5651b501"))))), hashType:None */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=4000000L, lockingScript=LockingScript(bytes("76a9144dc39248253538b93d3a0eb122d16882b998145888ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("4dc39248253538b93d3a0eb122d16882b9981458"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9144dc39248253538b93d3a0eb122d16882b998145888ac"))))) */ )), lockTime=0L /* hash:bytes("70c4e749f2b8b907875d1483ae43e8a6790b0c8397bbb33682e3602617f9a77a") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("004830450221008c2107ed4e026ab4319a591e8d9ec37719cdea053951c660566e3a3399428af502202ecd823d5f74a77cc2159d8af2d3ea5d36a702fef9a7edaaf562aef22ac35da401")) /* ops:ScriptOpList(operations=Array(Op0(),OpPush(72,ScriptBytes(bytes("30450221008c2107ed4e026ab4319a591e8d9ec37719cdea053951c660566e3a3399428af502202ecd823d5f74a77cc2159d8af2d3ea5d36a702fef9a7edaaf562aef22ac35da401"))))), hashType:None */, lockingScript=LockingScript(bytes("51210351efb6e91a31221652105d032a2508275f374cea63939ad72f1b1e02f477da782100f2b7816db49d55d24df7bdffdbc1e203b424e8cd39f5651ab938e5e4a193569e52ae")) /* ops:ScriptOpList(operations=Array(Op1(),OpPush(33,ScriptBytes(bytes("0351efb6e91a31221652105d032a2508275f374cea63939ad72f1b1e02f477da78"))),OpPush(33,ScriptBytes(bytes("00f2b7816db49d55d24df7bdffdbc1e203b424e8cd39f5651ab938e5e4a193569e"))),OpNum(2),OpCheckMultiSig(Script(bytes("51210351efb6e91a31221652105d032a2508275f374cea63939ad72f1b1e02f477da782100f2b7816db49d55d24df7bdffdbc1e203b424e8cd39f5651ab938e5e4a193569e52ae"))))) */ )
      ),
      (
        "[Result of unlocking script execution : [B@14432d16]",
        MergedScript(transaction=Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("761d8c5210fdfd505f6dff38f740ae3728eb93d7d0971fb433f685d40a4c04f6")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("48304502205853c7f1395785bfabb03c57e962eb076ff24d8e4e573b04db13b45ed3ed6ee20221009dc82ae43be9d4b1fe2847754e1d36dad48ba801817d485dc529afc516c2ddb481210305584980367b321fad7f1c1f4d5d723d0ac80c1d80c8ba12343965b48364537a")) /* ops:ScriptOpList(operations=Array(OpPush(72,ScriptBytes(bytes("304502205853c7f1395785bfabb03c57e962eb076ff24d8e4e573b04db13b45ed3ed6ee20221009dc82ae43be9d4b1fe2847754e1d36dad48ba801817d485dc529afc516c2ddb481"))),OpPush(33,ScriptBytes(bytes("0305584980367b321fad7f1c1f4d5d723d0ac80c1d80c8ba12343965b48364537a"))))), hashType:Some(-127) */, sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=TransactionHash(bytes("40cd1ee71808037f2ae01faef88de4788cbcbe257e319ed1debc6966dff06a9c")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("4930460221008269c9d7ba0a7e730dd16f4082d29e3684fb7463ba064fd093afc170ad6e0388022100bc6d76373916a3ff6ee41b2c752001fda3c9e048bcff0d81d05b39ff0f4217b2812103aae303d825421545c5bc7ccd5ac87dd5add3bcc3a432ba7aa2f2661699f9f659")) /* ops:ScriptOpList(operations=Array(OpPush(73,ScriptBytes(bytes("30460221008269c9d7ba0a7e730dd16f4082d29e3684fb7463ba064fd093afc170ad6e0388022100bc6d76373916a3ff6ee41b2c752001fda3c9e048bcff0d81d05b39ff0f4217b281"))),OpPush(33,ScriptBytes(bytes("03aae303d825421545c5bc7ccd5ac87dd5add3bcc3a432ba7aa2f2661699f9f659"))))), hashType:Some(-127) */, sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=300000L, lockingScript=LockingScript(bytes("76a9145c11f917883b927eef77dc57707aeb853f6d389488ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("5c11f917883b927eef77dc57707aeb853f6d3894"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9145c11f917883b927eef77dc57707aeb853f6d389488ac"))))) */ )), lockTime=0L /* hash:bytes("51bf528ecf3c161e7c021224197dbe84f9a8564212f6207baa014c01a1668e1e") */), inputIndex=0, unlockingScript=UnlockingScript(bytes("48304502205853c7f1395785bfabb03c57e962eb076ff24d8e4e573b04db13b45ed3ed6ee20221009dc82ae43be9d4b1fe2847754e1d36dad48ba801817d485dc529afc516c2ddb481210305584980367b321fad7f1c1f4d5d723d0ac80c1d80c8ba12343965b48364537a")) /* ops:ScriptOpList(operations=Array(OpPush(72,ScriptBytes(bytes("304502205853c7f1395785bfabb03c57e962eb076ff24d8e4e573b04db13b45ed3ed6ee20221009dc82ae43be9d4b1fe2847754e1d36dad48ba801817d485dc529afc516c2ddb481"))),OpPush(33,ScriptBytes(bytes("0305584980367b321fad7f1c1f4d5d723d0ac80c1d80c8ba12343965b48364537a"))))), hashType:Some(-127) */, lockingScript=LockingScript(bytes("76a9148551e48a53decd1cfc63079a4581bcccfad1a93c88ac")) /* ops:ScriptOpList(operations=Array(OpDup(),OpHash160(),OpPush(20,ScriptBytes(bytes("8551e48a53decd1cfc63079a4581bcccfad1a93c"))),OpEqualVerify(),OpCheckSig(Script(bytes("76a9148551e48a53decd1cfc63079a4581bcccfad1a93c88ac"))))) */ )
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
