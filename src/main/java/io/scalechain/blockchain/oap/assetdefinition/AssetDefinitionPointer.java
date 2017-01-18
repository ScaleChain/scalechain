package io.scalechain.blockchain.oap.assetdefinition;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created by shannon on 16. 12. 28.
 */
public class AssetDefinitionPointer {
  public static byte URL_POINTER  = 'u';
  public static byte HASH_POINTER = 'h';

  byte   prefix;
  byte[] value;
  private AssetDefinitionPointer(byte prefix, byte[] value) {
    this.prefix = prefix;
    this.value = value;
  }

  public String pointerHex() {
    return HexUtil.hex(getPointer(), HexUtil.hex$default$2());
  }

  public String valueHex() {
    return HexUtil.hex(getValue(), HexUtil.hex$default$2());
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append('(').append(HexUtil.hex(getPointer(), HexUtil.hex$default$2())).append(')');
    return sb.toString();
  }

  public byte getPrefix() {
    return prefix;
  }

  public byte[] getValue() {
    return value;
  }

  public byte[] getPointer() {
    byte[] pointer = new byte[value.length + 1];
    pointer[0] = prefix;
    System.arraycopy(value, 0, pointer, 1, value.length);
    return pointer;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AssetDefinitionPointer)) return false;
    AssetDefinitionPointer that = (AssetDefinitionPointer)obj;
    if (this.prefix != that.prefix) return false;
    return Arrays.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
      return Arrays.hashCode(getPointer());
  }

  public static AssetDefinitionPointer create(byte prefix, byte[] value) throws OapException {
    if (prefix == HASH_POINTER && value.length != 20) {
      throw new OapException(OapException.DEFINITION_POINTER_ERROR, "Invalid hash value");
    }
    return new AssetDefinitionPointer(prefix, value);
  }
  public static AssetDefinitionPointer from(byte[] pointerBytes) {
    byte prefix = pointerBytes[0];
    byte[] value = new byte[pointerBytes.length - 1];
    System.arraycopy(pointerBytes, 1, value, 0, value.length);
    return new AssetDefinitionPointer(prefix, value);
  }
}
