package io.scalechain.blockchain.script

import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/**
 * Created by kangmo on 11/12/15.
 */
class ScriptSerializerSpec extends FlatSpec with BeforeAndAfterEach with OperationTestTrait {

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
        Array[Byte](0x63, 0x54, 0x63, 0x58, 0x68, 0x68) // (expected) serialized bytes.
      )
    )

  "serializedOperations" should "be serialized to a correct byte array." in {
    forAll(serializedOperations) { (operations: List[ScriptOp], expectedSerializedBytes: Array[Byte]) =>
      val serializedBytes = ScriptSerializer.serialize(operations)
      // TODO : how to write this in "should .. " form?
      assert( serializedBytes.sameElements(expectedSerializedBytes) )
    }
  }
}

