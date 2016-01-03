package io.scalechain.blockchain.script

import io.scalechain.blockchain.block.Transaction
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.{ScriptEvalException, FatalException, ErrorCode}

import scala.collection.immutable.HashMap

object ScriptOperations {
  val SCRIPT_OPS : Map[Short, ScriptOp] = Map(
    (0x00,Op0()),
    /*
      OpPush(1) ~ OpPush(75) was generated with this code.
      Reason : I don't want my source complicated by writing a for loop
      that puts these operations onto a mutable map, and then merge with
      the immutable SCRIPT_OPS map to produce another immutable map.

      for (i : Int <- 1 to 75 ) {
         println(s"(0x${Integer.toHexString(i)}, OpPush($i)),")
      }
    */
    (0x01, OpPush(1)),
    (0x02, OpPush(2)),
    (0x03, OpPush(3)),
    (0x04, OpPush(4)),
    (0x05, OpPush(5)),
    (0x06, OpPush(6)),
    (0x07, OpPush(7)),
    (0x08, OpPush(8)),
    (0x09, OpPush(9)),
    (0x0a, OpPush(10)),
    (0x0b, OpPush(11)),
    (0x0c, OpPush(12)),
    (0x0d, OpPush(13)),
    (0x0e, OpPush(14)),
    (0x0f, OpPush(15)),
    (0x10, OpPush(16)),
    (0x11, OpPush(17)),
    (0x12, OpPush(18)),
    (0x13, OpPush(19)),
    (0x14, OpPush(20)),
    (0x15, OpPush(21)),
    (0x16, OpPush(22)),
    (0x17, OpPush(23)),
    (0x18, OpPush(24)),
    (0x19, OpPush(25)),
    (0x1a, OpPush(26)),
    (0x1b, OpPush(27)),
    (0x1c, OpPush(28)),
    (0x1d, OpPush(29)),
    (0x1e, OpPush(30)),
    (0x1f, OpPush(31)),
    (0x20, OpPush(32)),
    (0x21, OpPush(33)),
    (0x22, OpPush(34)),
    (0x23, OpPush(35)),
    (0x24, OpPush(36)),
    (0x25, OpPush(37)),
    (0x26, OpPush(38)),
    (0x27, OpPush(39)),
    (0x28, OpPush(40)),
    (0x29, OpPush(41)),
    (0x2a, OpPush(42)),
    (0x2b, OpPush(43)),
    (0x2c, OpPush(44)),
    (0x2d, OpPush(45)),
    (0x2e, OpPush(46)),
    (0x2f, OpPush(47)),
    (0x30, OpPush(48)),
    (0x31, OpPush(49)),
    (0x32, OpPush(50)),
    (0x33, OpPush(51)),
    (0x34, OpPush(52)),
    (0x35, OpPush(53)),
    (0x36, OpPush(54)),
    (0x37, OpPush(55)),
    (0x38, OpPush(56)),
    (0x39, OpPush(57)),
    (0x3a, OpPush(58)),
    (0x3b, OpPush(59)),
    (0x3c, OpPush(60)),
    (0x3d, OpPush(61)),
    (0x3e, OpPush(62)),
    (0x3f, OpPush(63)),
    (0x40, OpPush(64)),
    (0x41, OpPush(65)),
    (0x42, OpPush(66)),
    (0x43, OpPush(67)),
    (0x44, OpPush(68)),
    (0x45, OpPush(69)),
    (0x46, OpPush(70)),
    (0x47, OpPush(71)),
    (0x48, OpPush(72)),
    (0x49, OpPush(73)),
    (0x4a, OpPush(74)),
    (0x4b, OpPush(75)),
    (0x4c, OpPushData(1)),
    (0x4d, OpPushData(2)),
    (0x4e, OpPushData(4)),
    (0x4f, Op1Negate()),
    (0x51, Op1()),
    (0x52, OpNum(2)),
    (0x53, OpNum(3)),
    (0x54, OpNum(4)),
    (0x55, OpNum(5)),
    (0x56, OpNum(6)),
    (0x57, OpNum(7)),
    (0x58, OpNum(8)),
    (0x59, OpNum(9)),
    (0x5a, OpNum(10)),
    (0x5b, OpNum(11)),
    (0x5c, OpNum(12)),
    (0x5d, OpNum(13)),
    (0x5e, OpNum(14)),
    (0x5f, OpNum(15)),
    (0x60, OpNum(16)),
    (0x61, OpNop()),
    (0x63, OpIf()),
    (0x64, OpNotIf()),
    (0x67, OpElse()),
    (0x68, OpEndIf()),
    (0x69, OpVerify()),
    (0x6a, OpReturn()),
    (0x6b, OpToAltStack()),
    (0x6c, OpFromAltStack()),
    (0x73, OpIfDup()),
    (0x74, OpDepth()),
    (0x75, OpDrop()),
    (0x76, OpDup()),
    (0x77, OpNip()),
    (0x78, OpOver()),
    (0x79, OpPick()),
    (0x7a, OpRoll()),
    (0x7b, OpRot()),
    (0x7c, OpSwap()),
    (0x7d, OpTuck()),
    (0x6d, Op2Drop()),
    (0x6e, Op2Dup()),
    (0x6f, Op3Dup()),
    (0x70, Op2Over()),
    (0x71, Op2Rot()),
    (0x72, Op2Swap()),
    (0x7e, OpCat()),
    (0x7f, OpSubstr()),
    (0x80, OpLeft()),
    (0x81, OpRight()),
    (0x82, OpSize()),
    (0x83, OpInvert()),
    (0x84, OpAnd()),
    (0x85, OpOr()),
    (0x86, OpXor()),
    (0x87, OpEqual()),
    (0x88, OpEqualVerify()),
    (0x8b, Op1Add()),
    (0x8c, Op1Sub()),
    (0x8d, Op2Mul()),
    (0x8e, Op2Div()),
    (0x8f, OpNegate()),
    (0x90, OpAbs()),
    (0x91, OpNot()),
    (0x92, Op0NotEqual()),
    (0x93, OpAdd()),
    (0x94, OpSub()),
    (0x95, OpMul()),
    (0x96, OpDiv()),
    (0x97, OpMod()),
    (0x98, OpLShift()),
    (0x99, OpRShift()),
    (0x9a, OpBoolAnd()),
    (0x9b, OpBoolOr()),
    (0x9c, OpNumEqual()),
    (0x9d, OpNumEqualVerify()),
    (0x9e, OpNumNotEqual()),
    (0x9f, OpLessThan()),
    (0xa0, OpGreaterThan()),
    (0xa1, OpLessThanOrEqual()),
    (0xa2, OpGreaterThanOrEqual()),
    (0xa3, OpMin()),
    (0xa4, OpMax()),
    (0xa5, OpWithin()),
    (0xa6, OpRIPEMD160()),
    (0xa7, OpSHA1()),
    (0xa8, OpSHA256()),
    (0xa9, OpHash160()),
    (0xab, OpCodeSparator()),
    (0xac, OpCheckSig()),
    (0xad, OpCheckSigVerify()),
    (0xae, OpCheckMultiSig()),
    (0xaf, OpCheckMultiSigVerify()),
    (0xf9, OpSmallData()),
    (0xfa, OpSmallInteger()),
    (0xfd, OpPubKeyHash()),
    (0xfe, OpPubKey()),
    (0xff, OpInvalidOpCode()),
    (0x50, OpReserved()),
    (0x62, OpVer()),
    (0x65, OpVerIf()),
    (0x66, OpVerNotIf()),
    (0x89, OpReserved1()),
    (0x8a, OpReserved2()),
    (0xb0, OpNopN(1)),
    (0xb1, OpNopN(2)),
    (0xb2, OpNopN(3)),
    (0xb3, OpNopN(4)),
    (0xb4, OpNopN(5)),
    (0xb5, OpNopN(6)),
    (0xb6, OpNopN(7)),
    (0xb7, OpNopN(8)),
    (0xb8, OpNopN(9)),
    (0xb9, OpNopN(10))
  )
  /** Return the ScriptOp object that implements a specific operation code of the script.
   *
   * @param opCode The op code of a script word.
   * @return
   */
  def get(opCode : Short) : Option[ScriptOp] = {
    val scriptOp = SCRIPT_OPS.get(opCode)
    scriptOp
  }
}

class ScriptEnvironment(val transaction : Transaction, val transactionInputIndex : Option[Int]) {
  /** Alternative constructor : pass null for transaction and tranasctionInput.
   * These two parameters are necessary only for OP_CHECKSIG, OP_CHECKSIGVERIFY, OP_CHECKMULTISIG, OP_CHECKMULTISIGVERIFY.
   */
  def this() = this(null, null)

  // BUGBUG : if OP_CHECKSIG or OP_CHECKMULTISIG runs without OP_CODESEPARATOR,
  //          can we keep signatureOffset as zero?
  // The offset in the raw script where the data for checking signature starts.
  protected var sigCheckOffset : Int = 0

  /** Set the offset of raw script where the data for checking signature starts.
   *
   * @param offset the offset of raw script
   */
  def setSigCheckOffset(offset : Int) : Unit = sigCheckOffset = offset

  /** Get the offset of raw script where the data for checking signature starts.
    *
    * @return The offset.
    */
  def getSigCheckOffset() : Int = sigCheckOffset

  val stack = new ScriptStack()
  // The altStack is necessary to support OP_TOALTSTACK and OP_FROMALTSTACK,
  // which moves items on top of the stack and the alternative stack.
  val altStack = new ScriptStack()


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
  def eval(scriptOps : ScriptOpList) : ScriptValue = {
    val env = new ScriptEnvironment()

    eval_internal(env, scriptOps)

    env.stack.pop()
  }

  /** Execute a list of script operations, but do not pop any item from the stack.
    * This method is called either by eval or ScriptOp.execute.
    *
    * Ex> OpCond may want to execute list of ScriptOp(s) on the then-statement-list.
    *
    * @param env The script execution environment.
    * @param scriptOps The list of script operations to execute.
    */
  def eval_internal(env : ScriptEnvironment, scriptOps : ScriptOpList) = {
    for (operation : ScriptOp <- scriptOps.operations) {
      operation.execute(env)
    }
  }
}

