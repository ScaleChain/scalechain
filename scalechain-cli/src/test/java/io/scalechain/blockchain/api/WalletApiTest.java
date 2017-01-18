package io.scalechain.blockchain.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The method level unit tests are done in oap module.
 * Do RPC level tests here.
 * Test 2 APIs
 *     GetAddressBtAccount
 *     GetBalacne
 *
 * Created by shannon on 17. 1. 9.
 */
public class WalletApiTest extends ApiTestWithSampleTransactions {
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

  //
  // GetAddressesByAccount API
  //
  @Test
  public void getAddressesByAcountTest() throws RpcInvoker.RpcCallException {
    for(String account : provider.accounts()) {
      AddressData[] expected = provider.addressesOf(account);
      JsonArray result = getAddressesByAccount(account);
      assertEquals("Number of Address returned should be", expected.length, result.size());
      for(AddressData data : expected) {
        assertTrue("Address should be in returned addresses", result.contains(new JsonPrimitive(data.address.base58())));
      }
    }
  }

  @Test
  public void getAddressesByAcountNonExistingAccountTest() throws RpcInvoker.RpcCallException {
    String account = "NON-EXSISTING-ACCOUNT";
    JsonArray array = getAddressesByAccount(account);
    assertEquals("getAddressesByAccount should return no addresses", 0, array.size());
  }

  //
  //  GetBalance API
  //
  @Test
  public void getBalanceTest() throws RpcInvoker.RpcCallException {
    for(String account : provider.accounts()) {
      BigDecimal expected = provider.balanceOf(account);
      BigDecimal actual = getBalance(account, 1, true);

      assertTrue("Balance of account " + account + " shoud be euqal to", expected.compareTo(actual) == 0);
    }
  }

  @Test
  public void getBalanceWathOnlyFalseTest() throws RpcInvoker.RpcCallException {
    for(String account : provider.accounts()) {
      AddressData receivingAddress = provider.receivingAddressOf(account);
      BigDecimal expected = provider.balanceOf(receivingAddress.address.base58());
      BigDecimal actual = getBalance(account, 1, false);

      assertTrue("Balance of account " + account + " shoud be euqal to", expected.compareTo(actual) == 0);
    }
  }

  @Test
  public void getBalanceNotExistingAccountTest() throws RpcInvoker.RpcCallException {
    String account = "NOT_EXSITING_ACCOUNT";
    BigDecimal actual = getBalance(account, 1, true);
    assertTrue("Balance of non-exsisting account shlud be", BigDecimal.valueOf(0).compareTo(actual) == 0);
  }

  @Test
  public void getBalanceAccountAllTest() throws RpcInvoker.RpcCallException {
    String[] accounts = provider.accounts();

    waitForChain(1);
    BigDecimal actualBalance = getBalance("*", 1, true);

    BigDecimal expectedBalance = getBalance(provider.internalAccount(), 1, true);
    for(String account : accounts) {
      BigDecimal expectedBalanceWatchOnlyTrue = provider.balanceOf(account);

      BigDecimal balance = getBalance(account, 1, true);
      assertTrue("Balacne should be", expectedBalanceWatchOnlyTrue.compareTo(balance) == 0);
      expectedBalance = expectedBalance.add(balance);
    }
    assertTrue("Balacne of accounts should be sum of three accounts and internal account", expectedBalance.compareTo(actualBalance) == 0);
  }
}
