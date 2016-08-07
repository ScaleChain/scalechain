package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{ProtocolMessage, LockingScript, TransactionOutput}
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.script.{ScriptSerializer, ScriptValue, ScriptParser, ScriptOpList}
import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.crypto.{Base58Check, Hash160, ECKey, HashFunctions}
import io.scalechain.util.{HexUtil, ByteArray}

import scala.collection.generic.SeqFactory



/** The CoinAddress singleton that decodes an address to create a CoinAddress.
  *
  */
object CoinAddress {
  /** Decode an address and create a CoinAddress.
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
    * @param publicKeyHash The public key hash. RIPEMD160( SHA256( publicKey ) )
    * @return The created CoinAddress.
    */
  def from(publicKeyHash : Array[Byte]) : CoinAddress = {
    // Step 1 : Get the chain environment to get the address version.
    val chainEnv = ChainEnvironment.get

    // Step 2 : Create the CoinAddress
    CoinAddress(chainEnv.PubkeyAddressVersion, publicKeyHash)
  }


  /** Create a CoinAddress from a private key.
    *
    * @param privateKey The private key to use to generate public key and public key hash for the new coin address.
    * @return The created CoinAddress.
    */
  def from(privateKey : PrivateKey) : CoinAddress = {
    // Step 1 : Create a public key.
    val publicKey : PublicKey = PublicKey.from(privateKey)

    // Step 2 : Hash the public key.

    // Step 3 : Create an address.
    CoinAddress.from(publicKey.getHash.value)
  }
}

/** ParsedPubKeyScript singleton that parses a locking script to get a ParsedPubKeyScript.
  *
  */
object ParsedPubKeyScript {
  /** Parse a locking script to get the ParsedPubKeyScript.
    *
    * @param lockingScript The locking script to parse.
    * @return The ParsedPubKeyScript that has the parsed locking script.
    */
  def from(lockingScript:LockingScript) : ParsedPubKeyScript= {
    ParsedPubKeyScript( ScriptParser.parse(lockingScript) )
  }

  /** Create a ParsedPubKeyScript from a private key.
    *
    * @param privateKey The private key to use to generate public key and public key hash for the new coin address.
    * @return The created CoinAddress.
    */
  def from(privateKey : PrivateKey) : ParsedPubKeyScript = {
    // Step 1 : Create a public key.
    val publicKey : Array[Byte] = ECKey.publicKeyFromPrivate(privateKey.value, false /* uncompressed */)

    // Step 2 : Hash the public key.
    val publicKeyHash : Hash160 = HashFunctions.hash160(publicKey)

    from(publicKeyHash.value)
  }

  /** Create a ParsedPubKeyScript from a public key hash.
    *
    * @param publicKeyHash The public key hash. RIPEMD160( SHA256( publicKey ) )
    * @return The created ParsedPubKeyScript.
    */
  def from(publicKeyHash : Array[Byte]) : ParsedPubKeyScript = {
    assert(publicKeyHash.length == 20)
    val scriptOps = List( OpDup(), OpHash160(), OpPush(20, ScriptValue.valueOf(publicKeyHash)), OpEqualVerify(), OpCheckSig() )

    ParsedPubKeyScript(ScriptOpList(scriptOps))
  }

}

/** An entity that describes the ownership of a coin.
  * For example, a coin address can be a description of ownership of a coin.
  * Used by wallet's importAddress.
  */
sealed trait OutputOwnership extends ProtocolMessage {
  /** Check if the ownership is valid.
    * Ex> The format of a coin address is valid.
    * Ex> The script operations of the public key script is one of allowed patterns.
    *
    * @return true if the ownership is valid. false otherwise.
    */
  def isValid(): Boolean

  /** The locking script that this output ownership can unlock.
    *
    * @return The locking script.
    */
  def lockingScript() : LockingScript

  /** A string key used for prefixed objects in the wallet database.
    *
    * @return The string to be used as a string key in the wallet database.
    */
  def stringKey() : String
}

/** A coin address with a version and public key hash.
  *
  * @param version The version of the address.
  * @param publicKeyHash The hash value of the public key.
  */
case class CoinAddress(version:Byte, publicKeyHash : ByteArray) extends OutputOwnership
{
  /** See if an address has valid version prefix and length.
    *
    * @return true if the address is valid. false otherwise.
    */
  def isValid(): Boolean = {
    val env = ChainEnvironment.get

    // The public key hash uses RIPEMD160, so it should be 20 bytes.
    if (publicKeyHash.length != 20 ) {
      false
    } else if (version != env.PubkeyAddressVersion && version != env.ScriptAddressVersion ) {
      false
    } else {
      true
    }
  }

  /** Return the address in base58 encoding format.
    *
    * @return The base 58 check encoded address.
    */
  def base58() : String = {
    assert(isValid)
    Base58Check.encode(version, publicKeyHash)
  }

  /** Convert the address to string. The converted value is used for a prefixed string key in the wallet database.
    *
    * @return The string to be used as a key in the wallet database.
    */
  def stringKey() : String = {
    base58()
  }

  /** Get the locking script of this coin address.
    *
    * TODO : Test automation.
    *
    * @return The locking script of this coin address.
    */
  def lockingScript() : LockingScript = {
    ParsedPubKeyScript.from(publicKeyHash).lockingScript()
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

  /** Encode the parsed public key script into a byte array to get a locking script.
    *
    * @return The locking script we got.
    */
  def lockingScript() : LockingScript = {
    val serializedScript = ScriptSerializer.serialize(scriptOps.operations)
    LockingScript(serializedScript)
  }

  override def stringKey() : String = {
    HexUtil.hex(lockingScript().data)
  }
}