package io.scalechain.blockchain.oap.assetdefinition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.helper.UnitTestHelper;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by shannon on 16. 12. 29.
 */
public class AssetDefinitionPointerTest {
  @BeforeClass
  public static void setUpForClass() throws Exception {
    ChainEnvironment.create("mainnet");
    UnitTestHelper.init();
  }

  @Test
  public void pointerTest() throws OapException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);
    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    assertEquals("Prefix should be HASH_POINTER", pointer.getPrefix(), AssetDefinitionPointer.HASH_POINTER);
    assertArrayEquals("Value should be equal to Asset Definition Hash", pointer.getValue(), definition.hash());
  }

  @Test
  public void createTest() throws OapException {
    byte[] value = HexUtil.bytes("1234567890123456789012345678901234567890");
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );
    assertEquals("Prefix shoul be equal to", AssetDefinitionPointer.HASH_POINTER, pointer.getPrefix());
    assertEquals("The first byte of pointer shluld be equal to", AssetDefinitionPointer.HASH_POINTER, pointer.getPointer()[0]);
    assertArrayEquals("Value should be equal to", value, pointer.getValue());
  }
  @Test
  public void valueHexTest() throws OapException {
    String valueHex = "1234567890123456789012345678901234567890";
    byte[] value = HexUtil.bytes(valueHex);
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );
    assertEquals("Value hex should be equal to", valueHex, pointer.valueHex());
  }
  @Test
  public void toStringAndtoHexTest() throws OapException {
    String valueHex = "1234567890123456789012345678901234567890";
    byte[] value = HexUtil.bytes(valueHex);
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );

    String s = pointer.toString();
    StringBuilder sb = new StringBuilder(AssetDefinitionPointer.class.getSimpleName());
    sb.append('(')
      .append(HexUtil.hex(new byte[] { AssetDefinitionPointer.HASH_POINTER }))
      .append(valueHex)
      .append(')');
    assertEquals("toString() should be equal to", sb.toString(), s);

    StringBuilder sb2 = new StringBuilder(AssetDefinitionPointer.class.getSimpleName());
    sb2.append('(')
      .append(HexUtil.hex(HexUtil.bytes(pointer.pointerHex())))
      .append(')');
    assertEquals("toString() should contain pointerHex()", sb2.toString(), s);

  }
  @Test
  public void equalsTest() throws OapException {
    byte[] value = HexUtil.bytes("1234567890123456789012345678901234567890");
    AssetDefinitionPointer pointer1 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );

    AssetDefinitionPointer pointer2 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );
    assertEquals("Two pointers created from same value", pointer1, pointer2);

    byte[] value2 = HexUtil.bytes("123456789012345678901234567890123456789A");
    AssetDefinitionPointer pointer3 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value2
    );
    assertNotEquals("Two pointers whith different value", pointer2, pointer3);

    assertTrue("AssetDefinitionPointer should not be equal to null", !pointer1.equals(null));
    assertTrue("AssetDefinitionPointer should not be equal to non AsssetDefinitionPointer instance", !pointer1.equals("s"));
  }

  @Test
  public void hashCodeTest() throws OapException {
    byte[] value = HexUtil.bytes("1234567890123456789012345678901234567890");
    AssetDefinitionPointer pointer1 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );

    AssetDefinitionPointer pointer2 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value
    );
    assertEquals("Two pointers created from same prefix and value", pointer1.hashCode(), pointer2.hashCode());


    byte[] value2 = HexUtil.bytes("123456789012345678901234567890123456789A");
    AssetDefinitionPointer pointer3 = AssetDefinitionPointer.create(
      AssetDefinitionPointer.HASH_POINTER, value2
    );

    assertNotEquals("Two pointers whith different value", pointer2.hashCode(), pointer3.hashCode());
  }

  @Test
  public void fromTest() throws OapException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);
    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    byte[] bytes = pointer.getPointer();

    AssetDefinitionPointer actual = AssetDefinitionPointer.from(bytes);
    assertEquals("", actual, pointer);
  }
}
