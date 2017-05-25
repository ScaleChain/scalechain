package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.transaction.*;
import io.scalechain.crypto.Base58Check;
import io.scalechain.util.Bytes;
import kotlin.Pair;

/**
 * Created by shannon on 16. 11. 23.
 */
public class AssetAddress implements OutputOwnership {
  byte[] namespaceAndVersion = new byte[2];
  byte[] publicKeyHash = new byte[20];

  /*
   * The Open Assets Address Format representation is constructed in the following manner:
   *   base58-encode: [one-byte namespace][one-byte version][payload][4-byte checksum]
   *   The namespace used for Open Assets is 19 (0x13 in hexadecimal). The version byte is the version byte of the original address.
   *   The payload is the payload contained in the original address.
   *   The 4-byte checksum is the first four bytes of the double SHA256 hash of the namespace, version and payload.
   *
   */
  public AssetAddress(byte version, byte[] publicKeyHash) {
    this.namespaceAndVersion[0] = (byte) 0x13;
    this.namespaceAndVersion[1] = version;
    this.publicKeyHash = new byte[publicKeyHash.length];
    System.arraycopy(publicKeyHash, 0, this.publicKeyHash, 0, publicKeyHash.length);
  }

  public byte getVersion() {
    return namespaceAndVersion[1];
  }

  public byte[] getPublicKeyHash() {
    return publicKeyHash;
  }

  @Override
  public boolean isValid() {
    ChainEnvironment env = ChainEnvironment.get();
    byte ver = namespaceAndVersion[1];
    if (publicKeyHash.length != 20) return false;
    if (ver != env.getPubkeyAddressVersion() && ver != env.getScriptAddressVersion()) return false;
    if (namespaceAndVersion[0] != 0x13) return false;
    return true;
  }

  @Override
  public LockingScript lockingScript() {
    return ParsedPubKeyScript.from(publicKeyHash).lockingScript();
  }

  @Override
  public String stringKey() {
    return base58();
  }

  public String base58() {
    return Base58Check.encode(namespaceAndVersion, publicKeyHash);
  }

  public String toString() {
    return base58();
  }

  public CoinAddress coinAddress() {
    return new CoinAddress(namespaceAndVersion[1], new Bytes(publicKeyHash));
  }

  public static AssetAddress from(String base58) throws OapException {
    try {
      Pair<Byte, byte[]> decoded = Base58Check.decode(base58);
      byte[] publicKeyHash = new byte[20];
      System.arraycopy(decoded.getSecond(), 1, publicKeyHash, 0, 20);
      return new AssetAddress(decoded.getSecond()[0], publicKeyHash);
    } catch (Exception e) {
      throw new OapException(OapException.INVALID_ADDRESS, "Invalid AssetAddress: " + base58, e);
    }
  }

  public static AssetAddress fromCoinAddress(String coinAddress) throws OapException {
    try {
      Pair<Byte, byte[]> decoded = Base58Check.decode(coinAddress);
      return new AssetAddress(decoded.getFirst(), decoded.getSecond());
    } catch (Exception e) {
      throw new OapException(OapException.INVALID_ADDRESS, "Cannot create AssetAddress from CoinAddress: " + coinAddress, e);
    }
  }

  public static AssetAddress fromCoinAddress(CoinAddress coinAddress) {
    return new AssetAddress(coinAddress.getVersion(), coinAddress.getPublicKeyHash().getArray());
  }
}