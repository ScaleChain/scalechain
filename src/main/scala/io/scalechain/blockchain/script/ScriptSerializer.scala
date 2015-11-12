package io.scalechain.blockchain.script

import java.nio.ByteBuffer

import io.scalechain.blockchain.script.ops.ScriptOp

import scala.collection.mutable.ArrayBuffer

/**
 * Created by kangmo on 11/12/15.
 */
object ScriptSerializer {
  /** Serialize the script operations into a Byte array.
   * This is also necessary in order to pass them to script parser and executor while we write test cases.
   * The input of the script parser is a byte array. So the serializer will write a list of ScriptOp(s) into Array[Byte].
   * @param operations
   */
  def serialize(operations:List[ScriptOp]) : Array[Byte] = {
    val buffer = new ArrayBuffer[Byte]()

    for (op : ScriptOp <- operations ) {
      op.serialize(buffer)
    }

    buffer.toArray
  }
}
