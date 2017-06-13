package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.script.ops.*

object ScriptOperations {
  val SCRIPT_OPS : Map<Short, ScriptOp> = mapOf(
    0x00.toShort() to Op0(),
    /*
      OpPush(1) ~ OpPush(75) was generated with this code.
      Reason : I don't want my source complicated by writing a for loop
      that puts these operations onto a mutable map, and then merge with
      the immutable SCRIPT_OPS map to produce another immutable map.

      for (i : Int <- 1 to 75 ) {
         println(s"0x${Integer.toHexString(i)}, OpPush($i)),")
      }
    */
    0x01.toShort() to OpPush(1),
    0x02.toShort() to OpPush(2),
    0x03.toShort() to OpPush(3),
    0x04.toShort() to OpPush(4),
    0x05.toShort() to OpPush(5),
    0x06.toShort() to OpPush(6),
    0x07.toShort() to OpPush(7),
    0x08.toShort() to OpPush(8),
    0x09.toShort() to OpPush(9),
    0x0a.toShort() to OpPush(10),
    0x0b.toShort() to OpPush(11),
    0x0c.toShort() to OpPush(12),
    0x0d.toShort() to OpPush(13),
    0x0e.toShort() to OpPush(14),
    0x0f.toShort() to OpPush(15),
    0x10.toShort() to OpPush(16),
    0x11.toShort() to OpPush(17),
    0x12.toShort() to OpPush(18),
    0x13.toShort() to OpPush(19),
    0x14.toShort() to OpPush(20),
    0x15.toShort() to OpPush(21),
    0x16.toShort() to OpPush(22),
    0x17.toShort() to OpPush(23),
    0x18.toShort() to OpPush(24),
    0x19.toShort() to OpPush(25),
    0x1a.toShort() to OpPush(26),
    0x1b.toShort() to OpPush(27),
    0x1c.toShort() to OpPush(28),
    0x1d.toShort() to OpPush(29),
    0x1e.toShort() to OpPush(30),
    0x1f.toShort() to OpPush(31),
    0x20.toShort() to OpPush(32),
    0x21.toShort() to OpPush(33),
    0x22.toShort() to OpPush(34),
    0x23.toShort() to OpPush(35),
    0x24.toShort() to OpPush(36),
    0x25.toShort() to OpPush(37),
    0x26.toShort() to OpPush(38),
    0x27.toShort() to OpPush(39),
    0x28.toShort() to OpPush(40),
    0x29.toShort() to OpPush(41),
    0x2a.toShort() to OpPush(42),
    0x2b.toShort() to OpPush(43),
    0x2c.toShort() to OpPush(44),
    0x2d.toShort() to OpPush(45),
    0x2e.toShort() to OpPush(46),
    0x2f.toShort() to OpPush(47),
    0x30.toShort() to OpPush(48),
    0x31.toShort() to OpPush(49),
    0x32.toShort() to OpPush(50),
    0x33.toShort() to OpPush(51),
    0x34.toShort() to OpPush(52),
    0x35.toShort() to OpPush(53),
    0x36.toShort() to OpPush(54),
    0x37.toShort() to OpPush(55),
    0x38.toShort() to OpPush(56),
    0x39.toShort() to OpPush(57),
    0x3a.toShort() to OpPush(58),
    0x3b.toShort() to OpPush(59),
    0x3c.toShort() to OpPush(60),
    0x3d.toShort() to OpPush(61),
    0x3e.toShort() to OpPush(62),
    0x3f.toShort() to OpPush(63),
    0x40.toShort() to OpPush(64),
    0x41.toShort() to OpPush(65),
    0x42.toShort() to OpPush(66),
    0x43.toShort() to OpPush(67),
    0x44.toShort() to OpPush(68),
    0x45.toShort() to OpPush(69),
    0x46.toShort() to OpPush(70),
    0x47.toShort() to OpPush(71),
    0x48.toShort() to OpPush(72),
    0x49.toShort() to OpPush(73),
    0x4a.toShort() to OpPush(74),
    0x4b.toShort() to OpPush(75),
    0x4c.toShort() to OpPushData(1),
    0x4d.toShort() to OpPushData(2),
    0x4e.toShort() to OpPushData(4),
    0x4f.toShort() to Op1Negate(),
    0x51.toShort() to Op1(),
    0x52.toShort() to OpNum(2),
    0x53.toShort() to OpNum(3),
    0x54.toShort() to OpNum(4),
    0x55.toShort() to OpNum(5),
    0x56.toShort() to OpNum(6),
    0x57.toShort() to OpNum(7),
    0x58.toShort() to OpNum(8),
    0x59.toShort() to OpNum(9),
    0x5a.toShort() to OpNum(10),
    0x5b.toShort() to OpNum(11),
    0x5c.toShort() to OpNum(12),
    0x5d.toShort() to OpNum(13),
    0x5e.toShort() to OpNum(14),
    0x5f.toShort() to OpNum(15),
    0x60.toShort() to OpNum(16),
    0x61.toShort() to OpNop(),
    0x63.toShort() to OpIf(),
    0x64.toShort() to OpNotIf(),
    0x67.toShort() to OpElse(),
    0x68.toShort() to OpEndIf(),
    0x69.toShort() to OpVerify(),
    0x6a.toShort() to OpReturn(),
    0x6b.toShort() to OpToAltStack(),
    0x6c.toShort() to OpFromAltStack(),
    0x73.toShort() to OpIfDup(),
    0x74.toShort() to OpDepth(),
    0x75.toShort() to OpDrop(),
    0x76.toShort() to OpDup(),
    0x77.toShort() to OpNip(),
    0x78.toShort() to OpOver(),
    0x79.toShort() to OpPick(),
    0x7a.toShort() to OpRoll(),
    0x7b.toShort() to OpRot(),
    0x7c.toShort() to OpSwap(),
    0x7d.toShort() to OpTuck(),
    0x6d.toShort() to Op2Drop(),
    0x6e.toShort() to Op2Dup(),
    0x6f.toShort() to Op3Dup(),
    0x70.toShort() to Op2Over(),
    0x71.toShort() to Op2Rot(),
    0x72.toShort() to Op2Swap(),
    0x7e.toShort() to OpCat(),
    0x7f.toShort() to OpSubstr(),
    0x80.toShort() to OpLeft(),
    0x81.toShort() to OpRight(),
    0x82.toShort() to OpSize(),
    0x83.toShort() to OpInvert(),
    0x84.toShort() to OpAnd(),
    0x85.toShort() to OpOr(),
    0x86.toShort() to OpXor(),
    0x87.toShort() to OpEqual(),
    0x88.toShort() to OpEqualVerify(),
    0x8b.toShort() to Op1Add(),
    0x8c.toShort() to Op1Sub(),
    0x8d.toShort() to Op2Mul(),
    0x8e.toShort() to Op2Div(),
    0x8f.toShort() to OpNegate(),
    0x90.toShort() to OpAbs(),
    0x91.toShort() to OpNot(),
    0x92.toShort() to Op0NotEqual(),
    0x93.toShort() to OpAdd(),
    0x94.toShort() to OpSub(),
    0x95.toShort() to OpMul(),
    0x96.toShort() to OpDiv(),
    0x97.toShort() to OpMod(),
    0x98.toShort() to OpLShift(),
    0x99.toShort() to OpRShift(),
    0x9a.toShort() to OpBoolAnd(),
    0x9b.toShort() to OpBoolOr(),
    0x9c.toShort() to OpNumEqual(),
    0x9d.toShort() to OpNumEqualVerify(),
    0x9e.toShort() to OpNumNotEqual(),
    0x9f.toShort() to OpLessThan(),
    0xa0.toShort() to OpGreaterThan(),
    0xa1.toShort() to OpLessThanOrEqual(),
    0xa2.toShort() to OpGreaterThanOrEqual(),
    0xa3.toShort() to OpMin(),
    0xa4.toShort() to OpMax(),
    0xa5.toShort() to OpWithin(),
    0xa6.toShort() to OpRIPEMD160(),
    0xa7.toShort() to OpSHA1(),
    0xa8.toShort() to OpSHA256(),
    0xa9.toShort() to OpHash160(),
    0xab.toShort() to OpCodeSparator(),
    0xac.toShort() to OpCheckSig(),
    0xad.toShort() to OpCheckSigVerify(),
    0xae.toShort() to OpCheckMultiSig(),
    0xaf.toShort() to OpCheckMultiSigVerify(),
    0xf9.toShort() to OpSmallData(),
    0xfa.toShort() to OpSmallInteger(),
    0xfd.toShort() to OpPubKeyHash(),
    0xfe.toShort() to OpPubKey(),
    0xff.toShort() to OpInvalidOpCode(),
    0x50.toShort() to OpReserved(),
    0x62.toShort() to OpVer(),
    0x65.toShort() to OpVerIf(),
    0x66.toShort() to OpVerNotIf(),
    0x89.toShort() to OpReserved1(),
    0x8a.toShort() to OpReserved2(),
    0xb0.toShort() to OpNopN(1),
    0xb1.toShort() to OpNopN(2),
    0xb2.toShort() to OpNopN(3),
    0xb3.toShort() to OpNopN(4),
    0xb4.toShort() to OpNopN(5),
    0xb5.toShort() to OpNopN(6),
    0xb6.toShort() to OpNopN(7),
    0xb7.toShort() to OpNopN(8),
    0xb8.toShort() to OpNopN(9),
    0xb9.toShort() to OpNopN(10)
  )
  /** Return the ScriptOp object that implements a specific operation code of the script.
   *
   * @param opCode The op code of a script word.
   * @return
   */
  fun get(opCode : Short) : ScriptOp? {
    return SCRIPT_OPS.get(opCode)
  }
}

class ScriptEnvironment(val transaction : Transaction?, val transactionInputIndex : Int?) {
  /** Alternative constructor : pass null for transaction and tranasctionInput.
   * These two parameters are necessary only for OP_CHECKSIG, OP_CHECKSIGVERIFY, OP_CHECKMULTISIG, OP_CHECKMULTISIGVERIFY.
   */
  constructor() : this(null, null)

  // BUGBUG : if OP_CHECKSIG or OP_CHECKMULTISIG runs without OP_CODESEPARATOR,
  //          can we keep signatureOffset as zero?
  // The offset in the raw script where the data for checking signature starts.
  private var sigCheckOffset : Int = 0

  /** Set the offset of raw script where the data for checking signature starts.
   *
   * @param offset the offset of raw script
   */
  fun setSigCheckOffset(offset : Int) {
    sigCheckOffset = offset
  }

  /** Get the offset of raw script where the data for checking signature starts.
    *
    * @return The offset.
    */
  fun getSigCheckOffset() : Int = sigCheckOffset

  val stack = ScriptStack()
  // The altStack is necessary to support OP_TOALTSTACK and OP_FROMALTSTACK,
  // which moves items on top of the stack and the alternative stack.
  val altStack = ScriptStack()
}

/**
 * Created by kangmo on 11/6/15.
 */
object ScriptInterpreter {
  /** Execute a parsed script. Return the value on top of the stack after the script execution.
   *
   * @param scriptOps A chunk of byte array after we get from ScriptParser.
   * @return the value on top of the stack after the script execution.
   */
  fun eval(scriptOps : ScriptOpList) : ScriptValue {
    val env = ScriptEnvironment()

    eval_internal(env, scriptOps)

    return env.stack.pop()
  }

  /** Execute a list of script operations, but do not pop any item from the stack.
    * This method is called either by eval or ScriptOp.execute.
    *
    * Ex> OpCond may want to execute list of ScriptOp(s) on the then-statement-list.
    *
    * @param env The script execution environment.
    * @param scriptOps The list of script operations to execute.
    */
  fun eval_internal(env : ScriptEnvironment, scriptOps : ScriptOpList) {
    for (operation : ScriptOp in scriptOps.operations) {
      operation.execute(env)
    }
  }
}

