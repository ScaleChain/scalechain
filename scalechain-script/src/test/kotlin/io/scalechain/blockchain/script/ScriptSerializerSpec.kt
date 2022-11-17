package io.scalechain.blockchain.script

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ops.*
import io.scalechain.test.TestMethods.filledString
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by kangmo on 11/12/15.
 */
@RunWith(KTestJUnitRunner::class)
class ScriptSerializerSpec : OperationTestTrait() {
  val serializedOperations =
    table(
      // column names
      headers("operations", "expectedSerializedBytes"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      // if true stmt1 end
      row(
        // list of operations to serialize.
        listOf(
          OpIf(),
            OpNum(4),
            OpIf(),
              OpNum(8),
            OpEndIf(),
          OpEndIf()),
        byteArrayOf(0x63, 0x54, 0x63, 0x58, 0x68, 0x68) // (expected) serialized bytes.
      )
    )

  init {
    "serializedOperations" should "be serialized to a correct byte array." {
      forAll(serializedOperations) { operations: List<ScriptOp>, expectedSerializedBytes: ByteArray ->
        val serializedBytes = ScriptSerializer.serialize(operations)
        // TODO : how to write this in "should .. " form?
        assert( Arrays.equals( serializedBytes, expectedSerializedBytes) )
      }
    }

    "serialize" should "produce byte arrays that can be parsed into the original script" {
      val expectedPublicKeyHash = HexUtil.bytes( filledString(40, '1'.code.toByte() ) )
      assert(expectedPublicKeyHash.size == 20)
      val expectedOperations = listOf( OpDup(), OpHash160(), OpPush(20, ScriptValue.valueOf(expectedPublicKeyHash)), OpEqualVerify(), OpCheckSig() )
      val serializedBytes = ScriptSerializer.serialize(expectedOperations)

      // TODO : BUGBUG check actualOperations
      val actualOperations = ScriptParser.parse(LockingScript(Bytes(serializedBytes))).operations

      // The expected operations should match the original one.
      if (expectedOperations.size == 5) {
        val opDup = expectedOperations[0]
        val opHash160 = expectedOperations[1]
        val opPush = expectedOperations[2]
        val opEqualVerify = expectedOperations[3]
        val opCheckSig = expectedOperations[4]

        if (opDup is OpDup &&
            opHash160 is OpHash160 &&
            opPush is OpPush && opPush.byteCount == 20 &&
            opEqualVerify is OpEqualVerify &&
            opCheckSig is OpCheckSig
        ) {
          val actualPublicKeyHashScript = opPush.inputValue
          actualPublicKeyHashScript!!.value.toList() shouldBe expectedPublicKeyHash.toList()
        } else {
          assert(false)
        }
      } else {
        assert(false)
      }
    }
  }
}

