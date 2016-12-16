package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.proto.CoinbaseData
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.script.ScriptSerializer
import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ops.*
import io.scalechain.crypto.Base58Check
import io.scalechain.crypto.ECKey
import io.scalechain.crypto.Hash160
import io.scalechain.crypto.HashFunctions
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import java.util.*

/** An entity that describes the ownership of a coin.
  * For example, a coin address can be a description of ownership of a coin.
  * Used by wallet's importAddress.
  */
interface OutputOwnership /*: ProtocolMessage*/ {
  /** Check if the ownership is valid.
    * Ex> The format of a coin address is valid.
    * Ex> The script operations of the public key script is one of allowed patterns.
    *
    * @return true if the ownership is valid. false otherwise.
    */
  fun isValid(): Boolean

  /** The locking script that this output ownership can unlock.
    *
    * @return The locking script.
    */
  fun lockingScript() : LockingScript

  /** A string key used for prefixed objects in the wallet database.
    *
    * @return The string to be used as a string key in the wallet database.
    */
  fun stringKey() : String
}

/** A coin address with a version and public key hash.
  *
  * @param version The version of the address.
  * @param publicKeyHash The hash value of the public key.
  */
data class CoinAddress(val version:Byte, val publicKeyHash : Bytes) : OutputOwnership
{

  /** See if an address has valid version prefix and length.
    *
    * @return true if the address is valid. false otherwise.
    */
  override fun isValid(): Boolean {
    val env = ChainEnvironment.get()

    // The public key hash uses RIPEMD160, so it should be 20 bytes.
    if (publicKeyHash.array.size != 20 ) {
      return false
    } else if (version != env.PubkeyAddressVersion && version != env.ScriptAddressVersion ) {
      return false
    } else {
      return true
    }
  }

  /** Return the address in base58 encoding format.
    *
    * @return The base 58 check encoded address.
    */
  fun base58() : String {
    assert(isValid())
    return Base58Check.encode(version, publicKeyHash.array)
  }

  /** Convert the address to string. The converted value is used for a prefixed string key in the wallet database.
    *
    * @return The string to be used as a key in the wallet database.
    */
  override fun stringKey() : String {
    return base58()
  }

  /** Get the locking script of this coin address.
    *
    * TODO : Test automation.
    *
    * @return The locking script of this coin address.
    */
  override fun lockingScript() : LockingScript {
    return ParsedPubKeyScript.from(publicKeyHash.array).lockingScript()
  }


  /** The CoinAddress singleton that decodes an address to create a CoinAddress.
   *
   */
  companion object {
    /** Decode an address and create a CoinAddress.
     *
     * @param address The address to decode.
     * @return The decoded CoinAddress.
     */
    fun from(address : String) : CoinAddress {
      val (versionPrefix, publicKeyHash) = Base58Check.decode(address)
      val coinAddress = CoinAddress(versionPrefix, Bytes(publicKeyHash))
      if (coinAddress.isValid()) {
        return coinAddress
      } else {
        throw GeneralException(ErrorCode.RpcInvalidAddress)
      }
    }

    /** Create a CoinAddress from a public key hash.
     *
     * @param publicKeyHash The public key hash. RIPEMD160( SHA256( publicKey ) )
     * @return The created CoinAddress.
     */
    fun from(publicKeyHash : ByteArray) : CoinAddress {
      // Step 1 : Get the chain environment to get the address version.
      val chainEnv = ChainEnvironment.get()

      // Step 2 : Create the CoinAddress
      return CoinAddress(chainEnv.PubkeyAddressVersion, Bytes(publicKeyHash))
    }


    /** Create a CoinAddress from a private key.
     *
     * @param privateKey The private key to use to generate public key and public key hash for the new coin address.
     * @return The created CoinAddress.
     */
    fun from(privateKey : PrivateKey) : CoinAddress {
      // Step 1 : Create a public key.
      val publicKey : PublicKey = PublicKey.from(privateKey)

      // Step 2 : Hash the public key.

      // Step 3 : Create an address.
      return CoinAddress.from(publicKey.getHash().value.array)
    }
  }
}

/** A parsed public key script.
  *
  * @param scriptOps The list of script operations from the public key script.
  */
data class ParsedPubKeyScript(val scriptOps : ScriptOpList) : OutputOwnership {
  /** Check if the scriptOps is one of the pubKeyScript patters for standard transactions.
    */
  override fun isValid(): Boolean {
    // TOOD : Check if the scriptOps is one of the pubKeyScript patters for standard transactions.
    return true
  }

  /** Encode the parsed public key script into a byte array to get a locking script.
    *
    * @return The locking script we got.
    */
  override fun lockingScript() : LockingScript {
    val serializedScript = ScriptSerializer.serialize(scriptOps.operations)
    return LockingScript(Bytes(serializedScript))
  }

  override fun stringKey() : String {
    return HexUtil.hex(lockingScript().data.array)
  }

  companion object {
    /** Parse a locking script to get the ParsedPubKeyScript.
     *
     * @param lockingScript The locking script to parse.
     * @return The ParsedPubKeyScript that has the parsed locking script.
     */
    fun from(lockingScript:LockingScript) : ParsedPubKeyScript{
      return ParsedPubKeyScript( ScriptParser.parse(lockingScript) )
    }

    /** Create a ParsedPubKeyScript from a private key.
     *
     * @param privateKey The private key to use to generate public key and public key hash for the new coin address.
     * @return The created CoinAddress.
     */
    fun from(privateKey : PrivateKey) : ParsedPubKeyScript {
      // Step 1 : Create a public key.
      val publicKey : ByteArray = ECKey.publicKeyFromPrivate(privateKey.value, false /* uncompressed */)

      // Step 2 : Hash the public key.
      val publicKeyHash : Hash160 = HashFunctions.hash160(publicKey)

      return from(publicKeyHash.value.array)
    }

    /** Create a ParsedPubKeyScript from a public key hash.
     *
     * @param publicKeyHash The public key hash. RIPEMD160( SHA256( publicKey ) )
     * @return The created ParsedPubKeyScript.
     */
    fun from(publicKeyHash : ByteArray) : ParsedPubKeyScript {
      assert(publicKeyHash.size == 20)
      val scriptOps = listOf( OpDup(), OpHash160(), OpPush(20, ScriptValue.valueOf(publicKeyHash)), OpEqualVerify(), OpCheckSig() )

      return ParsedPubKeyScript(ScriptOpList(scriptOps))
    }
  }
}
