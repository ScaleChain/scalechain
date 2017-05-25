package io.scalechain.blockchain.script

import io.scalechain.blockchain.script.ops.ScriptOp

/**
 * Created by kangmo on 11/12/15.
 */
object ScriptSerializer {
  /** Serialize the script operations into a Byte array.
   * This is also necessary in order to pass them to script parser and executor while we write test cases.
   * The input of the script parser is a byte array. So the serializer will write a list of ScriptOp(s) into ByteArray.
   * @param operations
   */
  fun serialize(operations:List<ScriptOp>) : ByteArray {
    val buffer = arrayListOf<Byte>()

    for (op : ScriptOp in operations ) {
      op.serialize(buffer)
    }

    return buffer.toByteArray()
  }
}
