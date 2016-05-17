package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{LockingScript, TransactionOutput}
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.script.{ScriptValue, ScriptParser, ScriptOpList}
import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.crypto.{HashFunctions, Base58Check}

/** An entity that describes the ownership of a coin.
  * For example, a coin address can be a description of ownership of a coin.
  * Used by wallet's importAddress.
  */
trait OutputOwnership {
  /** Check if the ownership is valid.
    * Ex> The format of a coin address is valid.
    * Ex> The script operations of the public key script is one of allowed patterns.
    *
    * @return true if the ownership is valid. false otherwise.
    */
  def isValid(): Boolean

  /** Check if an ownership owns a transaction output.
    *
    * @param output The transaction output to check.
    * @return true if the ownership has owns the output. false otherwise.
    */
  def owns(output : TransactionOutput) : Boolean
}


/** The CoinAddress singleton that decodes an address to create a CoinAddress.
  *
  */
object CoinAddress {
  /** Decode an address and create a CoinAddress.
    *
    * TODO : Test Automation.
    *
    * @param address The address to decode.
    * @return The decoded CoinAddress.
    */
  def from(address : String) : CoinAddress = {
    val (versionPrefix, publicKeyHash) = Base58Check.decode(address)
    val coinAddress = CoinAddress(versionPrefix, publicKeyHash)
    if (coinAddress.isValid()) {
      coinAddress
    } else {
      throw new GeneralException(ErrorCode.RpcInvalidAddress)
    }
  }

  /** Create a CoinAddress from a public key hash.
    *
    * TODO : Test Automation.
    *
    * @param publicKeyHash The public key hash. RIPEMD160( SHA256( publicKey ) )
    * @return
    */
  def from(publicKeyHash : Array[Byte]) : CoinAddress = {
    // Step 1 : Get the chain environment to get the address version.
    val chainEnv = ChainEnvironmentFactory.getActive
    assert(chainEnv.isDefined)

    // Step 2 : Create the CoinAddress
    CoinAddress(chainEnv.get.PubkeyAddressVersion, publicKeyHash)
  }

  /** Parse the locking script to get the coin address from it.
    *
    * TODO : Test Automation.
    *
    * @param lockingScript The locking script where we extract the coin address.
    * @return Some(address) if succesfully extracted a coin adress from the locking script; None otherwise
    */
  def from(lockingScript: LockingScript) : Option[CoinAddress] = {
    val scriptOps : ScriptOpList = ScriptParser.parse(lockingScript)
    scriptOps.operations match {
      // Pay to public key
      case List( OpPush(_, encodedPublicKey : ScriptValue)) => {
        val publicKey : PublicKey = PublicKey.from(encodedPublicKey.value)
        val uncompressedPublicKey = publicKey.encode(false)
        val publicKeyHash = HashFunctions.hash160(uncompressedPublicKey)
        Some( CoinAddress.from(publicKeyHash.value) )
      }
      // Pay to public key hash
      case List( OpDup(), OpHash160(), OpPush(20, publicKeyHash), OpEqualVerify(), OpCheckSig(_) ) => {
        Some( CoinAddress.from(publicKeyHash.value) )
      }
      case _ => {
        None
      }
    }
  }
}

/** A coin address with a version and public key hash.
  *
  * @param version The version of the address.
  * @param publicKeyHash The hash value of the public key.
  */
case class CoinAddress(version:Byte, publicKeyHash:Array[Byte]) extends OutputOwnership
{
  /** See if an address has valid version prefix and length.
    *
    * @return true if the address is valid. false otherwise.
    */
  def isValid(): Boolean = {
    val env = ChainEnvironmentFactory.getActive().get

    // The public key hash uses RIPEMD160, so it should be 20 bytes.
    if (publicKeyHash.length != 20 ) {
      false
    } else if (version != env.PubkeyAddressVersion && version != env.ScriptAddressVersion ) {
      false
    } else {
      true
    }
  }

  /** Check if an ownership owns a transaction output.
    *
    * @param output The transaction output to check.
    * @return true if the ownership has owns the output. false otherwise.
    */
  def owns(output : TransactionOutput) : Boolean = {
    // TODO : implement
    assert(false)
    false
  }

  /** Return the address in base58 encoding format.
    *
    * @return The base 58 check encoded address.
    */
  def base58() : String = {
    Base58Check.encode(version, publicKeyHash)
  }
}

/** A parsed public key script.
  *
  * @param scriptOps The list of script operations from the public key script.
  */
case class ParsedPubKeyScript(scriptOps : ScriptOpList) extends OutputOwnership {
  /** Check if the scriptOps is one of the pubKeyScript patters for standard transactions.
    */
  def isValid(): Boolean = {
    // TOOD : Check if the scriptOps is one of the pubKeyScript patters for standard transactions.
    true
  }

  /** Check if an ownership owns a transaction output.
    *
    * @param output The transaction output to check.
    * @return true if the ownership has owns the output. false otherwise.
    */
  def owns(output : TransactionOutput) : Boolean = {
    // TODO : implement
    assert(false)
    false
  }
}