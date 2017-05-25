package io.scalechain.blockchain.oap.assetdefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.helper.UnitTestHelper;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.crypto.HashFunctions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

/**
 * Created by shannon on 16. 12. 29.
 */
public class AssetDefinitionTest {
  @BeforeClass
  public static void setUpForClass() throws Exception {
    ChainEnvironment.create("mainnet");
    UnitTestHelper.init();
  }

  @Test
  public void createAssetDefinitionTest() throws OapException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);

    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
    assertTrue("Asset Definitin sould be valid", definition.isValid());
    assertTrue("Asset Definitin sould have json array asset_ids", definition.toJson().has("asset_ids"));
    assertTrue("Asset Definitin sould have name", definition.toJson().has("name"));
    assertTrue("Asset Definitin sould have name_short", definition.toJson().has("name_short"));
    assertEquals("Asset Definition should match", expectedAssetDefinition, definition.toJson());
    assertTrue("asset_id should contain Asset Id", definition.getAssetIds().contains(
      UnitTestHelper.getAssetId(UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX))
    ));
  }

  JsonObject cloneDefinition(JsonObject json, String property) {
    JsonObject result = new JsonObject();
    for(Map.Entry<String, JsonElement> e : json.entrySet()) {
      if (!e.getKey().equals(property))
        result.add(e.getKey(), e.getValue());
    }
    return result;
  }
  @Test
  public void createAssetDefinitionIsValidTest() throws OapException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = null;
    JsonArray array = null;

    assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.ASSET_IDS);
    json.addProperty(AssetDefinition.ASSET_IDS, "KDKDKDKDKD");
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.ASSET_IDS);
    array = new JsonArray();
    array.add("KDKDKDKDKD");
    json.add(AssetDefinition.ASSET_IDS, array);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.ASSET_IDS);
    array = new JsonArray();
    array.add("");
    json.add(AssetDefinition.ASSET_IDS, array);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.ASSET_IDS);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME);
    json.addProperty(AssetDefinition.NAME, "");
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }


    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME_SHORT);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }

    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME_SHORT);
    json.addProperty(AssetDefinition.NAME_SHORT, "");
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }
    json = cloneDefinition(expectedAssetDefinition, AssetDefinition.NAME_SHORT);
    try {
      AssetDefinition.from(json.toString());
      assert(false);
    }catch(OapException e) {
      assertEquals("ErorCode sholud be equal", OapException.INVLAID_DEFINITION, e.getErrorCode());
      assertTrue("isValid return false", !(new AssetDefinition(json).isValid()));
    }
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createAssetDefinitionNoNameTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Metadata has no \"name\""));

    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);
    json.remove(AssetDefinition.NAME);
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
  }

  @Test
  public void createAssetDefinitionEmptyNameTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Metadata has empty \"name\" field"));

    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.NAME);
    json.addProperty(AssetDefinition.NAME, "");
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
  }

  @Test
  public void createAssetDefinitionNoNameShortTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Metadata has no \"name_short\""));

    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.NAME_SHORT);
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
  }


  @Test
  public void createAssetDefinitionEmptyNameShortTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Metadata has empty \"name_short\" field"));

    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.NAME_SHORT);
    json.addProperty(AssetDefinition.NAME_SHORT, "");
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
  }

  @Test
  public void hashTest() throws OapException, UnsupportedEncodingException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);
    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
    byte[] hash = HashFunctions.hash160(definition.toJson().toString().getBytes("UTF-8")).getValue().getArray();
    assertArrayEquals("Hash sould be equal to hash of JSON String", hash, definition.hash());
  }
}
