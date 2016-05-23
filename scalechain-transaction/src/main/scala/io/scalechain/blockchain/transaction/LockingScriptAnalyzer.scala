package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.script.{ScriptValue, ScriptParser, ScriptOpList}
import io.scalechain.crypto.HashFunctions

/**
  * Analyze the locking script attached to UTXOs.
  */
object LockingScriptAnalyzer {
  /** Extract addresses from a parsed script operations.
    *
    * A locking script has only one address if it is P2PK or P2PKH.
    * A locking script can have multiple addresses if it is using either checkmultisig without p2sh or checkmultisig with p2sh.
    *
    * @param scriptOps The parsed script operations.
    * @return The list of extracted addresses.
    */
  def extractAddresses(scriptOps: ScriptOpList ) : List[CoinAddress] = {
    scriptOps.operations match {
      // TODO : Extract multisig addresses
      // Pay to public key
      case List( OpPush(_, encodedPublicKey : ScriptValue), OpCheckSig(_) ) => {
        val publicKey : PublicKey = PublicKey.from(encodedPublicKey.value)
        val uncompressedPublicKey = publicKey.encode()
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

  /** Extract addresses from a locking script.
    *
    * @param lockingScript The locking script where we extract addreses.
    */
  def extractAddresses(lockingScript: LockingScript) : List[CoinAddress] = {
    val scriptOperations: ScriptOpList = ScriptParser.parse(lockingScript)
    extractAddresses(scriptOperations)
  }

  /** Extract output ownership from a locking script.
    *
    * @param lockingScript The locking script where we extract an output ownership.
    * @return The extracted output ownership.
    */
  def extractOutputOwnership(lockingScript : LockingScript ) : OutputOwnership = {
    // Step 1 : parse the script operations
    val scriptOperations : ScriptOpList = ScriptParser.parse(lockingScript)

    // Step 2 : try to extract coin addresses from it.
    val addresses = extractAddresses(scriptOperations)

    if (addresses.isEmpty) {
      // Step 2 : construct a pared public key script as an output ownership.
      //
      ParsedPubKeyScript(scriptOperations)
    } else {
      // TODO : BUGBUG : We are using the first coin address only. is this ok?
      addresses(0)
    }
  }
}
