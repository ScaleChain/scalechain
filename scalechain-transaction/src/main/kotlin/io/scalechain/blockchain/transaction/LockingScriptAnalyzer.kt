package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ops.*
import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.script.ScriptOpList
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
  fun extractAddresses(scriptOps: ScriptOpList ) : List<CoinAddress> {
    val opCount = scriptOps.operations.size

    if (opCount == 2) {
      val opPush = scriptOps.operations[0]
      val opCheckSig = scriptOps.operations[1]
      if (opPush is OpPush &&
          opCheckSig is OpCheckSig ) {
        val encodedPublicKey = opPush.inputValue!!
        val publicKey : PublicKey = PublicKey.from(encodedPublicKey.value)
        val uncompressedPublicKey = publicKey.encode()
        val publicKeyHash = HashFunctions.hash160(uncompressedPublicKey)
        return listOf( CoinAddress.from(publicKeyHash.value.array) )
      } else {
        return listOf()
      }
    } else if (opCount == 5) {
      val opDup = scriptOps.operations[0]
      val opHash160 = scriptOps.operations[1]
      val opPush = scriptOps.operations[2]
      val opEqualVerify = scriptOps.operations[3]
      val opCheckSig = scriptOps.operations[4]

      if (opDup is OpDup &&
          opHash160 is OpHash160 &&
          opPush is OpPush && opPush.byteCount == 20 &&
          opEqualVerify is OpEqualVerify &&
          opCheckSig is OpCheckSig) {
        val publicKeyHash = opPush.inputValue!!
        return listOf( CoinAddress.from(publicKeyHash.value) )
      } else {
        return listOf()
      }
    } else {
      return listOf()
    }
  }

  /** Extract addresses from a locking script.
    *
    * @param lockingScript The locking script where we extract addreses.
    */
  fun extractAddresses(lockingScript: LockingScript) : List<CoinAddress> {
    val scriptOperations: ScriptOpList = ScriptParser.parse(lockingScript)
    return extractAddresses(scriptOperations)
  }

  /** Extract output ownership from a locking script.
    *
    * @param lockingScript The locking script where we extract an output ownership.
    * @return The extracted output ownership.
    */
  fun extractOutputOwnership(lockingScript : LockingScript ) : OutputOwnership {
    // Step 1 : parse the script operations
    val scriptOperations : ScriptOpList = ScriptParser.parse(lockingScript)

    // Step 2 : try to extract coin addresses from it.
    val addresses = extractAddresses(scriptOperations)

    if (addresses.isEmpty()) {
      // Step 2 : construct a pared public key script as an output ownership.
      //
      return ParsedPubKeyScript(scriptOperations)
    } else {
      // TODO : BUGBUG : We are using the first coin address only. is this ok?
      return addresses[0]
    }
  }


  /**
    * Extract all possible output ownerships from a locking script matching with known patterns of script operations for the locking script.
    * @param lockingScript The locking script to analyze
    * @return The list of all possible output ownerships.
    */
  fun extractPossibleOutputOwnerships(lockingScript : LockingScript ) : List<OutputOwnership> {
    // Step 1 : parse the script operations
    val scriptOperations : ScriptOpList = ScriptParser.parse(lockingScript)

    // Step 2 : try to extract coin addresses from it.
    val addresses = extractAddresses(scriptOperations)

    // TODO : Need to return ParsedPubKeyScript.
    // ParsedPubKeyScript is not supported for an output ownership. Check Wallet.importOutputOwnership
    return addresses //::: listOf(ParsedPubKeyScript(scriptOperations))
  }
}
