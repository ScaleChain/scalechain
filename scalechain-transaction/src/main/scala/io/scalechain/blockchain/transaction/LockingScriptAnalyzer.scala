package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.script.{ScriptValue, ScriptParser, ScriptOpList}
import io.scalechain.crypto.HashFunctions

/**
  * Analyze the locking script attached to UTXOs.
  */
object LockingScriptAnalyzer {
  /** Extract addresses from a locking script.
    *
    * A locking script has only one address if it is P2PK or P2PKH.
    * A locking script can have multiple addresses if it is using either checkmultisig without p2sh or checkmultisig with p2sh.
    *
    * @param lockingScript The locking script where we extract addreses.
    * @return The list of extracted addresses.
    */
  def extractAddresses(lockingScript: LockingScript) : List[CoinAddress] = {
    val scriptOps : ScriptOpList = ScriptParser.parse(lockingScript)
    scriptOps.operations match {
      // TODO : Extract multisig addresses
      // Pay to public key
      case List( OpPush(_, encodedPublicKey : ScriptValue), OpCheckSig(_) ) => {
        val publicKey : PublicKey = PublicKey.from(encodedPublicKey.value)
        val uncompressedPublicKey = publicKey.encode(false)
        val publicKeyHash = HashFunctions.hash160(uncompressedPublicKey)
        List( CoinAddress.from(publicKeyHash.value) )
      }
      // Pay to public key hash
      case List( OpDup(), OpHash160(), OpPush(20, publicKeyHash), OpEqualVerify(), OpCheckSig(_) ) => {
        List( CoinAddress.from(publicKeyHash.value) )
      }
      case _ => {
        List()
      }
    }
  }
}
