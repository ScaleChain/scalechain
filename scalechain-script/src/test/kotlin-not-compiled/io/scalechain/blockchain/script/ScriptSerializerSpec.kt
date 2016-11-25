package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ops._
import io.scalechain.util.HexUtil
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/**
 * Created by kangmo on 11/12/15.
 */
class ScriptSerializerSpec : FlatSpec with BeforeAndAfterEach with OperationTestTrait {

  this: Suite =>

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val serializedOperations =
    Table(
      // column names
      ("operations", "expectedSerializedBytes"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      // if true stmt1 end
      (
        // list of operations to serialize.
        List(
          OpIf(),
            OpNum(4),
            OpIf(),
              OpNum(8),
            OpEndIf(),
          OpEndIf()),
        ByteArray(0x63, 0x54, 0x63, 0x58, 0x68, 0x68) // (expected) serialized bytes.
      )
    )

  "serializedOperations" should "be serialized to a correct byte array." in {
    forAll(serializedOperations) { (operations: List<ScriptOp>, expectedSerializedBytes: ByteArray) =>
      val serializedBytes = ScriptSerializer.serialize(operations)
      // TODO : how to write this in "should .. " form?
      assert( serializedBytes.sameElements(expectedSerializedBytes) )
    }
  }

  "serialize" should "produce byte arrays that can be parsed into the original script" in {
    val expectedPublicKeyHash = HexUtil.bytes("1"*40)
    assert(expectedPublicKeyHash.length == 20)
    val expectedOperations = List( OpDup(), OpHash160(), OpPush(20, ScriptValue.valueOf(expectedPublicKeyHash)), OpEqualVerify(), OpCheckSig() )
    val serializedBytes = ScriptSerializer.serialize(expectedOperations)

    val actualOperations = ScriptParser.parse(LockingScript(serializedBytes)).operations

    // The expected operations should match the original one.
    expectedOperations match {
      case List( OpDup(), OpHash160(), OpPush(20, actualPublicKeyHashScript), OpEqualVerify(), OpCheckSig(_) ) => {
        actualPublicKeyHashScript.value.toList shouldBe expectedPublicKeyHash.toList
      }
      case _ => {
        // If it does not match, hit an assertion.
        assert(false)
      }
    }
  }
}

