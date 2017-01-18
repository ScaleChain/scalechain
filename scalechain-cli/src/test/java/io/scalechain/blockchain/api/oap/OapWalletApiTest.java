package io.scalechain.blockchain.api.oap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.api.RpcInvoker;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.IAddressDataProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * The method level unit tests are done in oap module.
 * Do RPC level tests here.
 * Test 2 APIs
 *     GetAssetBalance
 *     GetAssetTransactions
 *
 * Created by shannon on 17. 1. 9.
 */
public class OapWalletApiTest extends ApiTestWithSampleTransactions {


  @Before
  public void setUp() {
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
  }
  //
  //  GetBalance API
  //
  @Test
  public void getBalanceTest() throws RpcInvoker.RpcCallException {
    String[] accounts = provider.accounts();
    int index = 0;
    for(String account : accounts) {
      BigDecimal expectedBalance = provider.balanceOf(account);

      BigDecimal balanceWithWatchOlny = getBalance(account, 1, true);
      assertTrue("Balacne including watch-only address should be", expectedBalance.compareTo(balanceWithWatchOlny) == 0);

      index++;
    }
  }

  @Test
  public void getBalanceWatchOnlyFalseTest() throws RpcInvoker.RpcCallException {
    String[] accounts = provider.accounts();
    int index = 0;
    for(String account : accounts) {
      BigDecimal expectedBalanceWatchOnlyFalse = provider.balanceOf(provider.receivingAddressOf(account).address.base58());

      BigDecimal balanceWithoutWatchOnly = getBalance(account, 1, false);
      assertTrue("Balacne excluding watch-only address should be", expectedBalanceWatchOnlyFalse.compareTo(balanceWithoutWatchOnly) == 0);

      index++;
    }
  }


  //
  // GetAssetBalance API
  //
  private void checkAssetBalance(HashMap<String, Long> assetBalances, JsonArray assetBalanceResult) {
    for(JsonElement e : assetBalanceResult) {
      JsonObject blanceDescriptor = e.getAsJsonObject();
      String assetId = blanceDescriptor.getAsJsonObject().get("assetId").getAsString();
      long actualBalance = blanceDescriptor.getAsJsonObject().get("quantity").getAsLong();
      assertEquals("Balacne of each asset should be ", assetBalances.get(assetId).longValue(), actualBalance);
    }
  }
  @Test
  public void getAssetBalanceTest() throws RpcInvoker.RpcCallException {
    // We issued 10000 assets for EACH ASSET ID
    int index = 0;
    for(String account : provider.accounts()) {
      List<String> assetIds = new ArrayList<String>();

      JsonArray array = getAssetBalnce(account, 1, true, assetIds);

      assertEquals("getAssetBalance() sould return 2 asset balance", 2, array.size());
      // CHECK ASSET BALANCE
      checkAssetBalance(provider.assetBalanceOf(account), array);
      index++;
    }
  }

  @Test
  public void getAssetBalanceInvalidAccontTest() throws RpcInvoker.RpcCallException {
    // We issued 10000 assets for EACH ASSET ID
    long expectedBalance = 10000;
    String account = "NON-EXSISTING-ACCOUNT";
    List<String> assetIds = new ArrayList<String>();
    JsonArray array = getAssetBalnce(account, 1, true, assetIds);
    assertEquals("getAssetBalance() sould return no asset balance", 0, array.size());
  }

  @Test
  public void getAssetBalanceAssetIdFilterTest() throws RpcInvoker.RpcCallException {
    // We issued 10000 assets for EACH ASSET ID
    int index = 0;
    for(String account : provider.accounts()) {
      AddressData addressData = provider.receivingAddressOf(account);
      List<String> assetIds = new ArrayList<String>();
      assetIds.add(addressData.assetId.base58());

      JsonArray array = getAssetBalnce(account, 1, true, assetIds);

      assertEquals("getAssetBalance() with 1 Asset Id filter sould return 1 asset balance", 1, array.size());
      // CHECK ASSET BALANCE
      checkAssetBalance(provider.assetBalanceOf(account), array);
      index++;
    }
  }

  @Test
  public void getAssetBalanceNonExistingAssetIdFilterTest() throws RpcInvoker.RpcCallException {
    // We issued 10000 assets for EACH ASSET ID
    long expectedBalance = 10000;
    int index = 0;
    for(String account : provider.accounts()) {
      AddressData addressData = provider.receivingAddressOf(account);
      List<String> assetIds = new ArrayList<String>();
      assetIds.add("oUaLSXc9v37q6TZZhTaF5WrEEAVC86DmCu");
      JsonArray array = getAssetBalnce(account, 1, true, assetIds);
      assertEquals("getAssetBalance() with non-exisisting Asset Id filter sould return 0 asset balance", 0, array.size());

      index++;
    }
  }

  //
  // ListAssetTransactions API
  //
  @Test
  public void listAssetTransacionsTest() throws RpcInvoker.RpcCallException {
    String account = IAddressDataProvider.ACCOUNT_SENDER;

    JsonArray descriptors = listAssetTransactions(account, 20, 0, true);
    assertEquals("Senders transaction list should have", provider.receivingTransactionCountOf(account) + provider.sendingTransactionCountOf(account), descriptors.size());
  }

  public boolean checkCointains(JsonArray array, JsonElement desc) {
    for(JsonElement e : array) {
      String txid = e.getAsJsonObject().get("txid").getAsString();
      int vout = e.getAsJsonObject().get("vout").getAsInt();
      if (txid.equals(desc.getAsJsonObject().get("txid").getAsString())) {
        if (vout == desc.getAsJsonObject().get("vout").getAsInt()) {
          return true;
        }
      }
    }
    return false;
  }

  @Test
  public void listAssetTransacionsWatchOnlyTest() throws RpcInvoker.RpcCallException {
    String account = IAddressDataProvider.ACCOUNT_SENDER;

    JsonArray descriptorsIncludeWatchOnlyTrue = listAssetTransactions(account, 20, 0, true);
    assertEquals(
      "Senders transaction list should have",
      provider.receivingTransactionCountOf(account) + provider.sendingTransactionCountOf(account),
      descriptorsIncludeWatchOnlyTrue.size()
    );
    JsonArray descriptorsIncludeWatchOnlyFalse = listAssetTransactions(account, 20, 0, false);

    int watchOnlyCount = 0;
    for(JsonElement e : descriptorsIncludeWatchOnlyTrue) {
      if (!e.getAsJsonObject().getAsJsonObject().get("involvesWatchonly").getAsBoolean()) {
        assertTrue(
          "listAssetTransactionResult(includeWatchOnly => false) sholuld contain all of watch-only transaction of (listAssetTransactionResult(includeWatchOnly => true)",
          checkCointains(descriptorsIncludeWatchOnlyFalse, e)
        );
      } else {
        watchOnlyCount++;
      }
    }

    for(JsonElement e : descriptorsIncludeWatchOnlyFalse) {
      assertTrue(
        "listAssetTransactionResult(includeWatchOnly => true) sholuld contain all of watch-only transaction of (listAssetTransactionResult(includeWatchOnly => false)",
        checkCointains(descriptorsIncludeWatchOnlyTrue, e)
      );
    }

    assertEquals("Descriptor count with involve_watch_only true should be",
      watchOnlyCount,
      descriptorsIncludeWatchOnlyTrue.size() - descriptorsIncludeWatchOnlyFalse.size()
    );

  }
  @Test
  public void listAssetTransacionsNonExsitingAccountTest() throws RpcInvoker.RpcCallException {
    String account = "NON_EXSISTING_ACCOUNT";

    JsonArray descriptors = listAssetTransactions(account, 20, 0, true);
    assertEquals("Senders transaction list should have", 0, descriptors.size());
  }
}
