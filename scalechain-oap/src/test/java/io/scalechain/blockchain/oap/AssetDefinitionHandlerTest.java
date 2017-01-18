package io.scalechain.blockchain.oap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.Option;

import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

/**
 * Created by shannon on 17. 1. 3.
 */
public class AssetDefinitionHandlerTest extends TestWithWalletSampleData {
  static OapStorage storage;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();;
    storage = OpenAssetsProtocol.get().storage();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  //  public Pair<AssetDefinitionPointer, AssetDefinition> createAssetDefinition(AssetId assetId, String metadata) throws OapException {
  @Test
  public void createAssetDefinitionTest() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();
    
    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      OapStorage.get().delAssetDefintion(pointer);
      OapStorage.get().delAssetDefinitionPointer(assetId.base58());
    }

    // POINT SHOLUD NOT EXIST
    pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    assertNull("Pointer should be null", pointer);

    Pair<AssetDefinitionPointer, AssetDefinition> pair = definitionHandler.createAssetDefinition(assetId, json.toString());
    assertEquals("Metadata", expectedMetadata, pair.getSecond().toJson());
  }


  @Test
  public void createAssetDefinitionAleadyExistsTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("already exists");

    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }

    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    Pair<AssetDefinitionPointer, AssetDefinition> pair = definitionHandler.createAssetDefinition(assetId, json.toString());
    assertEquals("Metadata", expectedMetadata, pair.getSecond().toJson());
  }

  // TEST : public String getAssetDefintion(String hashOrAssetId) throws OapException;
  //  public AssetDefinitionPointer getAssetDefinitionPointer(AssetId assetId) throws OapException {
  @Test
  public void getAssetDefinitionPointerTest() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }

    // CREATE ASSET DEFITION
    Pair<AssetDefinitionPointer, AssetDefinition> pair = definitionHandler.createAssetDefinition(assetId, json.toString());

    // POINT SHOLUD NOT EXIST
    pointer = definitionHandler.getAssetDefinitionPointer(assetId);

    assertNotNull("Pointer should be not null", pointer);
  }

  @Test
  public void getAssetDefinitionPointerNoAssetDefintionTest() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // POINT SHOLUD NOT EXIST
    pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    assertNull("Pointer should be null", pointer);
  }

  //
  //  TESTS for getAssetDefintion()
  //  AssetDefinition getAssetDefintion(String hashOrAssetId) throws OapException {
  //
  @Test
  public void getAssetDefinitionAssetIdTest() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    AssetDefinition actual = definitionHandler.getAssetDefintion(assetId.base58());
    assertNotNull("Asset definition should be not null", actual);
  }

  @Test
  public void getAssetDefinitionHashTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Invalid hash value"));

    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    AssetDefinition actual = definitionHandler.getAssetDefintion(pointer.pointerHex());
    assertNotNull("Asset definition should be not null", actual);
  }


  @Test
  public void getAssetDefinitionInalidInputTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Invalid hash value"));

    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    AssetDefinition actual = definitionHandler.getAssetDefintion("INVALID_DATA");
    assertNull("Asset definition should be null", actual);
  }

  @Test
  public void getAssetDefinitionInalidHashTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Asset Definition File for"));

    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    AssetDefinition actual = definitionHandler.getAssetDefintion("0000000000000000000000000000000000000000");
    assertNull("Asset definition should be null", actual);
  }

  //
  // public Option<AssetDefinition> getAssetDefintion(AssetDefinitionPointer pointer) throws OapException {
  //

  @Test
  public void getAssetDefinitionWithPointerTest() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT POINTER AND DEFINITION
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    Option<AssetDefinition> actual = definitionHandler.getAssetDefintion(pointer);
    assertTrue("Asset Definition File should be defined", actual.isDefined());
    assertEquals("Asset Definition should be equal to", definition.toJson(), actual.get().toJson());
  }

  @Test
  public void getAssetDefinitionNoAssetDefinition() throws OapException {
    AddressData addressData = provider.receivingAddressOf(provider.accounts()[1]); // ""SENDER"
    String address = addressData.address.base58();
    AssetId assetId = addressData.assetId;
    String definitionString = addressData.assetDefinition;
    JsonObject expectedMetadata = new JsonParser().parse(definitionString).getAsJsonObject();

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedMetadata.entrySet()) {
      if (!e.getKey().equals("asset_ids")) json.add(e.getKey(), e.getValue());
    }
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), json.toString());

    // DELETE pointer if exists.
    AssetDefinitionPointer pointer = definitionHandler.getAssetDefinitionPointer(assetId);
    if (pointer != null) {
      storage.delAssetDefintion(pointer);
      storage.delAssetDefinitionPointer(assetId.base58());
    }
    // PUT THE POINTER ONLY
    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    Option<AssetDefinition> actual = definitionHandler.getAssetDefintion(pointer);
    assertTrue("Asset Definition sould not exist", actual.isEmpty());
  }
}
