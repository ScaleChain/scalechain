package io.scalechain.blockchain.api.oap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.api.RpcInvoker;
import io.scalechain.blockchain.oap.OapStorage;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The method level unit tests are done in oap module.
 * Do RPC level tests here.
 * Test 2 APIs
 *     CreateAssetDefinition
 *     GetAssetDefinition
 *
 * Created by shannon on 17. 1. 9.
 */
public class AssetDefinitionTest extends ApiTestWithSampleTransactions {


  @Override
  @Before
  public void setUp() {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() {
    super.tearDown();
  }
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private JsonObject addAssetId(JsonObject metadata, String  assetId) {
    JsonArray array = new JsonArray();
    array.add(assetId);
    metadata.add("asset_ids", array);
    return metadata;
  }

  //
  // CreateAssetDefinition API
  //
  @Test
  public void createAssetDefinitionTest() throws RpcInvoker.RpcCallException, OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    String account  = provider.ACCOUNT_IMPORTER;
    AddressData recevingAddress = provider.receivingAddressOf(account);

    // DELETE ASSET DEFINITION.
    AssetDefinitionPointer pointerOption = storage.getAssetDefinitionPointer(recevingAddress.assetId.base58());
    if (pointerOption != null) {
      storage.delAssetDefinition(pointerOption);
      storage.delAssetDefinitionPointer(recevingAddress.assetId.base58());
    }

    String metadataString = recevingAddress.assetDefinition;
    JsonObject metadata = new JsonParser().parse(metadataString).getAsJsonObject();
    metadata = addAssetId(metadata, recevingAddress.assetId.base58());

    JsonObject result = createAssetDefinition(recevingAddress.assetId.base58(), metadata);


    // Check Asset Id
    assertEquals("Asset Id should be equal to ", recevingAddress.assetId.base58(), result.get("asset_id").getAsString());
  }

  @Test
  public void createAssetDefinitionNoMetaDataTest() throws RpcInvoker.RpcCallException, OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    String account  = provider.ACCOUNT_IMPORTER;
    AddressData recevingAddress = provider.receivingAddressOf(account);

    try {
      JsonObject result = createAssetDefinition(recevingAddress.assetId.base58(), null);
      assertTrue("Test should fail", false);
    } catch(RpcInvoker.RpcCallException e) {
      assertTrue("A mandatory paramameter metadata at index 1 is missing", e.getData().contains("A mandatory parameter metadata at index 1 is missing"));
    }
  }

  //
  // GetAssetDefinition API
  //
  @Test
  public void getAssetDefinitionTest() throws RpcInvoker.RpcCallException, OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    String account  = provider.ACCOUNT_IMPORTER;
    AddressData recevingAddress = provider.receivingAddressOf(account);

    // PUT ASSET DEFINITION.
    JsonObject metadata = addAssetId(
      new JsonParser().parse(recevingAddress.assetDefinition).getAsJsonObject(), recevingAddress.assetId.base58()
    );
    System.out.println(metadata);
    AssetDefinition definition = new AssetDefinition(metadata);
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(recevingAddress.assetId.base58(), pointer);

    // DO TEST
    JsonObject result = getAssetDefinition(recevingAddress.assetId.base58());
    assertEquals("Asset Id should be match", recevingAddress.assetId.base58(), result.get("asset_ids").getAsJsonArray().get(0).getAsString());
    assertEquals("Asset Definition should be match", metadata, result.get("asset_definition").getAsJsonObject());
  }

  @Test
  public void getAssetDefinitionWithHashTest() throws RpcInvoker.RpcCallException, OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    String account  = provider.ACCOUNT_IMPORTER;
    AddressData recevingAddress = provider.receivingAddressOf(account);

    // PUT ASSET DEFINITION.
    JsonObject metadata = addAssetId(
      new JsonParser().parse(recevingAddress.assetDefinition).getAsJsonObject(), recevingAddress.assetId.base58()
    );
    AssetDefinition definition = new AssetDefinition(metadata);
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(recevingAddress.assetId.base58(), pointer);

    // DO TEST
    JsonObject result = getAssetDefinition(pointer.pointerHex());
    assertEquals("Asset Id should be match", recevingAddress.assetId.base58(), result.get("asset_ids").getAsJsonArray().get(0).getAsString());
    assertEquals("Asset Definition should be match", metadata, result.get("asset_definition").getAsJsonObject());
  }


  @Test
  public void getAssetDefinitionNotExstingDefinitionTest() {
    AddressData recevingAddress = provider.internalAddressData();

    try {
      // DO TEST
      JsonObject result = getAssetDefinition(recevingAddress.assetId.base58());
    } catch(RpcInvoker.RpcCallException e) {
      assertTrue("Asset Definition error should be thrown", e.getData().contains("Asset Definition for"));
    }
  }
}
