package io.scalechain.blockchain.api.oap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.api.Json;
import io.scalechain.blockchain.net.RpcSubSystem;
import io.scalechain.blockchain.oap.OapStorage;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.util.HexUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

//import spray.json.JsObject;
//import spray.json.JsonParser;
//import spray.json.ParserInput;

/**
 * Most of the method level unit tests are done in oap module and inb IssueAssetTest.
 * Do unit tests for RpcSubsystem.issueAsset method.
 *
 * Created by shannon on 17. 1. 3.
 */
public class IssueAssetTest extends ApiTestWithSampleTransactions {

  //
  // The method level unit tests are done in oap module.
  // Test RPC level tests.
  //


  RpcSubSystem rpcSubSystem;
  String senderAccount;
  AddressData[] senderAddressData;
  String receiverAccount;
  AddressData[] receiverAddressData;

  OapStorage storage;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  @Before
  public void setUp() {
    super.setUp();
    rpcSubSystem = RpcSubSystem.get();
    senderAccount = provider.accounts()[1];
    senderAddressData = provider.addressesOf(senderAccount);
    receiverAccount = provider.accounts()[2];
    receiverAddressData = provider.addressesOf(receiverAccount);
    storage = OpenAssetsProtocol.get().storage();
  }

  @Override
  @After
  public void tearDown() {
    super.tearDown();
  }


  //
  // issueAsset internal Method Test.
  //
  @Test
  public void getAssetDefinitionPointerWithHashOptionNoneTest() throws OapException {
    // GET ASSET DEFITION AND REMOVE asset_ids
    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    // DELETE Asset Definition and Asset Definition Pointer for AssetId
    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }
    // CRATE AND PUT Asset Definition and Asset Definition Pointer for AssetId
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), in.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);


    // DO TEST
    AssetDefinitionPointer pointerOption = rpcSubSystem.getAssetDefinitionPointer(assetId, null);
    assertTrue("Asset Definition Pointer should be defined", pointerOption != null );
    // CHECK MORE ?
    // TODO : OAP : ASK to Shannon : Need more check?
  }

  @Test
  public void getAssetDefinitionPointerWithHashOptionTest() throws OapException {
    // GET ASSET DEFITION AND REMOVE asset_ids
    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    // DELETE Asset Definition and Asset Definition Pointer for AssetId
    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }
    // CRATE AND PUT Asset Definition and Asset Definition Pointer for AssetId
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), in.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    // DO TEST
    AssetDefinitionPointer pointerOption = rpcSubSystem.getAssetDefinitionPointer(
      assetId,
      HexUtil.hex(pointer.getValue())
    );

    assertTrue("Asset Definition Pointer should be defined", pointerOption != null);
    // CHECK MORE ?
    // TODO : Check asset definition pointer.
  }

  @Test
  public void getAssetDefinitionPointerWithInvalidHashOptionTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Asset Definition Pointer does not match with Asset Id");

    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    // DELETE Asset Definition and Asset Definition Pointer for AssetId
    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }
    // CRATE AND PUT Asset Definition and Asset Definition Pointer for AssetId
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), in.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);
    String invalidHashOption = "0000000000000000000000000000000000000000";
    // DO TEST
    AssetDefinitionPointer pointerOption = rpcSubSystem.getAssetDefinitionPointer(assetId, invalidHashOption);

    // TODO : Add check for pointerOption
  }


  @Test
  public void getAssetDefinitionPointerWithNoAssetDefinitionTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid Asset Definition Pointer");

    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    // DELETE Asset Definition and Asset Definition Pointer for AssetId
    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }
    // CRATE AND PUT Asset Definition and Asset Definition Pointer for AssetId
    AssetDefinition definition = AssetDefinition.from(assetId.base58(), in.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
//    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);


    // DO TEST
    AssetDefinitionPointer pointerOption = rpcSubSystem.getAssetDefinitionPointer(
      assetId,
      HexUtil.hex(pointer.getValue())
    );

    // TODO : OAP : ASK to Shannon : Need more check?
  }

  private JsonObject createMetadtaOption(String jsonString) {
    JsonElement jelement = new JsonParser().parse(jsonString);
    JsonObject  jobject = jelement.getAsJsonObject();
    return jobject;
  }

  @Test
  public void createAssetDefinitionTest() throws OapException {
    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    // GET ASSET DEFITION AND REMOVE asset_ids
    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    JsonObject metadataOption = createMetadtaOption(in.toString());

    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }

    // DO TEST
    AssetDefinitionPointer pointerOption = rpcSubSystem.createAssetDefinition(assetId, metadataOption);

    // CHECK RESULT
    assertTrue("Asset Definition Pointer sould be defined", pointerOption != null);
    String s = storage.getAssetDefinition(pointerOption);
    assertTrue("Asset Definition sould not be defined", s != null);

    //  Asset Definition should have Asset Id in "asset_ids" field.
    AssetDefinition definition = AssetDefinition.from(s);
    assertTrue("Asset Definition should have asset_ids", definition.toJson().has("asset_ids"));
    boolean found = false;
    for (JsonElement e : definition.toJson().get("asset_ids").getAsJsonArray()) {
      if (e.getAsString().equals(assetId.base58())) found = true;
    }
    assertTrue("Asset Definition should contain assetId", found);
  }

  @Test
  public void createAssetDefinitionMetadataNoneTest() throws OapException {
    thrown.expect(Exception.class);
    thrown.expectMessage("No Asset Definition found");

    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    // GET ASSET DEFITION AND REMOVE asset_ids
    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");
    JsonObject metadataOption = createMetadtaOption(in.toString());

    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }

    // DO TEST : Exception should be thrown
    AssetDefinitionPointer pointerOption = rpcSubSystem.createAssetDefinition(assetId, null);
    // TODO : OAP : ASK to Shannon : need to check exception thrown?
  }

  @Test
  public void createAssetDefinitionWithInvalidMetadataTest() throws OapException {
    thrown.expect(Exception.class);
    thrown.expectMessage("No Asset Definition found");

    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    AssetId assetId = addressData.assetId;

    // GET ASSET DEFITION AND REMOVE asset_ids
    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");
    in.remove("name"); // REMOVE

    JsonObject metadataOption = createMetadtaOption(in.toString());

    // REMOVE ASSET DEFINITION AND ASSET POINTER
    AssetDefinitionPointer p = storage.getAssetDefinitionPointer(assetId.base58());
    if (p != null) {
      storage.delAssetDefinitionPointer(assetId.base58());
      storage.delAssetDefinition(p);
    }

    // DO TEST : Exception should be thrown
    AssetDefinitionPointer pointerOption = rpcSubSystem.createAssetDefinition(assetId, null);
    // TODO : OAP : ASK to Shannon : need to check exception thrown?
  }
}
