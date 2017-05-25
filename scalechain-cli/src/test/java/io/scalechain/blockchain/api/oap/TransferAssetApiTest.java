package io.scalechain.blockchain.api.oap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.api.RpcInvoker;
import io.scalechain.blockchain.net.RpcSubSystem;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.transaction.PrivateKey;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by shannon on 17. 1. 14.
 */
public class TransferAssetApiTest extends ApiTestWithSampleTransactions {
  @Override
  @Before
  public void setUp(){
    super.setUp();
  }

  @Override
  @After
  public void tearDown() {
    super.tearDown();
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  //
  // TransferAsset API test
  //
  //case class AssetTransferTo(to_address   : String, asset_id     : String, quantity    : Int)
  JsonObject transferTo(String assetAddress, String assetId, int quantity) {
    JsonObject result = new JsonObject();
    result.addProperty("to_address", assetAddress);
    result.addProperty("asset_id", assetId);
    result.addProperty("quantity", quantity);
    return result;
  }

  JsonArray transferToList(JsonObject... transfers) {
    JsonArray result = new JsonArray();
    for(JsonObject to : transfers) {
      result.add(to);
    }
    return result;
  }

  @Test
  public void transferAssetAPIInvalidPrivateKeyTest() {
    try {
      RpcSubSystem rpc = RpcSubSystem.get();
      AddressData senderAddressData = provider.watchOnlyAddressesOf(provider.ACCOUNT_SENDER)[0];
      String fromAddress = senderAddressData.assetAddress.base58();
      PrivateKey privateKey = senderAddressData.privateKey;
      String assetId = provider.receivingAddressOf(provider.ACCOUNT_SENDER).assetId.base58();


      AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
      String toAddress = receiverAddressData.assetAddress.base58();
      List<String> privateKeys = new ArrayList<String>();
      privateKeys.add("AAAA" + senderAddressData.privateKey.base58());
      // Provide no private key.
      JsonArray tos = transferToList(
        transferTo(toAddress, assetId, 1000)
      );

      JsonObject result = transferAsset(fromAddress, tos, privateKeys, fromAddress, IOapConstants.DUST_IN_SATOSHI);
    } catch(RpcInvoker.RpcCallException e) {
      assertEquals("Invalid private key should be return", "Invalid private key", e.getData());
    }
  }


  @Ignore
  @Test
  public void transferSingleAssetTest() {
  }

  @Ignore
  @Test
  public void transferMultipleAssetsTest() {
  }

}
