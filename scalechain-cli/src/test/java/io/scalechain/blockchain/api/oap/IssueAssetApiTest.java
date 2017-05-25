package io.scalechain.blockchain.api.oap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.api.RpcInvoker;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.transaction.OapTransaction;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The method level unit tests are done in oap module.
 * Do RPC level tests here.
 * Test IssueAsset API.
 *
 * Created by shannon on 17. 1. 11.
 */
public class IssueAssetApiTest extends ApiTestWithSampleTransactions {
  //
  //  IssueAsset API test.
  //

  private JsonObject createMetadta(String jsonString) {
    return new JsonParser().parse(jsonString).getAsJsonObject();
  }

  @Test
  public void issueAssetWithInternalAddressTest() throws RpcInvoker.RpcCallException, OapException {
    int quantity = 1000;
    String account = provider.internalAccount();
    AddressData internalAddressData = provider.internalAddressData();
    AssetId assetId = internalAddressData.assetId;
    String issuerAddress = internalAddressData.address.base58();
    String toAddress = internalAddressData.assetAddress.base58();
    String change = issuerAddress;

    // GET ASSET DEFITION AND REMOVE asset_ids
    String definitionString = internalAddressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");

    JsonObject metadata = createMetadta(in.toString());

    JsonObject res = issueAsset(
      issuerAddress,
      toAddress,
      quantity,
      "",
      new ArrayList<String>(),
      issuerAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI,
      metadata
    );

    assertNotNull("Should return IssueAssetResult", res);
    assertTrue("Result should contain txid filed ", res.has("txid"));
    assertTrue("Result should contain metadata_hash filed ", res.has("metadata_hash"));
    assertTrue("Result should contain asset_id filed ", res.has("asset_id"));
    assertEquals("Asset Id should match", res.get("asset_id").getAsString(), internalAddressData.assetId.base58());
    checkTx(res.get("txid").getAsString(), issuerAddress, res.get("asset_id").getAsString(), issuerAddress, quantity);
  }

  private void checkTx(String hashHex, String issuerAddress, String actualAssetId, String toAddress, int quantity) throws OapException {
    assertEquals("Asset Id should be", AssetId.fromCoinAddress(issuerAddress).base58(), actualAssetId);

    Hash hash = new Hash(new Bytes(HexUtil.bytes(hashHex)));
    OapTransaction tx = OpenAssetsProtocol.get().coloringEngine().color(hash);

    // Check
    assertTrue("Tx Output(1) should be MARKER OUPUT", (tx.getOapOutputs().get(1) instanceof OapMarkerOutput));
    int[] assetQuantities = ((OapMarkerOutput)tx.getOapOutputs().get(1)).getQuantities();
    assertTrue("Asset Quantity Count should be  1", assetQuantities.length == 1);
    assertEquals("Asset Quantity should be  ", quantity, assetQuantities[0]);
  }

  @Test
  public void issueAssetWithPrivateKeyTest() throws OapException, RpcInvoker.RpcCallException {
    int quantity = 1000;
    String account = provider.accounts()[1]; // "SENDER"
    AddressData addressData = provider.receivingAddressOf(account);
    String issuerAddress = addressData.address.base58();
    AssetId assetId = addressData.assetId;

    // GET ASSET DEFITION AND REMOVE asset_ids
    String definitionString = addressData.assetDefinition;
    JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
    JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
    in.remove("asset_ids");
    JsonObject metadata = createMetadta(in.toString());
    //CHECK UNSPENT

    // MAKE PrivateKeys TO SIGN TRANSACTION
    List<String> list = new ArrayList<String>();
    list.add(addressData.privateKey.base58());

    JsonObject result = issueAsset(
      addressData.address.base58(),
      addressData.assetAddress.base58(),
      quantity,
      "",
      list,
      addressData.address.base58(),
      IOapConstants.DEFAULT_FEES_IN_SATOSHI,
      metadata
    );

    assertNotNull("Should return IssueAssetResult", result);
    assertTrue("Result should contain txid filed ", result.has("txid"));
    assertTrue("Result should contain metadata_hash filed ", result.has("metadata_hash"));
    assertTrue("Result should contain asset_id filed ", result.has("asset_id"));
    assertEquals("Asset Id should match", result.get("asset_id").getAsString(), assetId.base58());
    checkTx(result.get("txid").getAsString(), issuerAddress, result.get("asset_id").getAsString(), issuerAddress, quantity);
  }

  @Test
  public void issueAssetSignErrorTest() throws OapException, RpcInvoker.RpcCallException {
    try {
      int quantity = 1000;
      String account = provider.accounts()[1]; // "SENDER"
      AddressData addressData = provider.watchOnlyAddressesOf(account)[0];
      AssetId assetId = addressData.assetId;

      // GET ASSET DEFITION AND REMOVE asset_ids
      String definitionString = addressData.assetDefinition;
      JsonObject gsonObject = new com.google.gson.JsonParser().parse(definitionString).getAsJsonObject();
      JsonObject in = new com.google.gson.JsonParser().parse(gsonObject.toString()).getAsJsonObject();
      in.remove("asset_ids");

      JsonObject metadata = createMetadta(in.toString());

      List<String> privateKeys = new ArrayList<String>();

      // DO TEST : AN EXCEPTION SHOULD BE THROWN
      JsonObject result = issueAsset(
        addressData.address.base58(),
        addressData.assetAddress.base58(),
        quantity,
        "",
        privateKeys,
        addressData.address.base58(),
        IOapConstants.DEFAULT_FEES_IN_SATOSHI,
        metadata
      );
    }catch(RpcInvoker.RpcCallException e) {
      assertEquals("Exception data sholuld be", "Cannot sign all inputs", e.getData());
    }
  }
}
