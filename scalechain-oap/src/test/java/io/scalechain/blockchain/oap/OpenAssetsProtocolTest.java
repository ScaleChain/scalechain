package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import io.scalechain.blockchain.oap.coloring.ColoringEngine;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.helper.UnitTestHelper;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.OapTransactionDescriptor;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.Wallet;
import io.scalechain.wallet.WalletTransactionDescriptor;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shannon on 16. 12. 26.
 */
public class OpenAssetsProtocolTest extends TestWithWalletSampleData {
  @Test
  public void getAddressesByAccountInternalAccountTest() throws OapException {
    OpenAssetsProtocol oap = OpenAssetsProtocol.get();
    CoinAddress receivingAddress = oap.wallet().getReceivingAddress("_FOR_TEST_ONLY");

    List<CoinAddress> addresses = oap.getAddressesByAccount("_FOR_TEST_ONLY", true);
    assertNotNull("Addresses", addresses);
    assertTrue("Addresses should have at least one address", addresses.size() > 0);
    assertTrue("Addresses should have receiving address", addresses.contains(receivingAddress));
  }

  @Ignore
  @Test
  public void getAddressesByAcountImportedAddressTest() throws OapException {
    OpenAssetsProtocol oap = OpenAssetsProtocol.get();
    CoinAddress receivingAddress = oap.wallet().getReceivingAddress(UnitTestHelper.ACCOUNT_GET_ADDRESSES);

    for (int index : UnitTestHelper.SEND_MANY_RECEIVER_ADDRESS_INDEXES) {
      String address = UnitTestHelper.getBitcoinAddressAt(index);
      UnitTestHelper.importAddress(UnitTestHelper.ACCOUNT_GET_ADDRESSES, address);
    }

    System.out.println("receivingAddress=" + receivingAddress);
    List<CoinAddress> addresses = oap.getAddressesByAccount(UnitTestHelper.ACCOUNT_GET_ADDRESSES, true);
    for (CoinAddress a : addresses) {
      System.out.println(a);
    }
    assertNotNull("Addresses", addresses);
    assertTrue("Addresses should have 3 addresses", addresses.size() == 3);
    assertTrue("Addresses should have receiving address", addresses.contains(receivingAddress));
    for (int index : UnitTestHelper.SEND_MANY_RECEIVER_ADDRESS_INDEXES) {
      CoinAddress address = CoinAddress.from(UnitTestHelper.getBitcoinAddressAt(index));
      assertTrue("Addresses should have imported address", addresses.contains(address));
    }
  }

  @Test
  public void getAddressesByAcccountNonExsitingAccount() throws OapException {
    OpenAssetsProtocol oap = OpenAssetsProtocol.get();
    List<CoinAddress> addresses = oap.getAddressesByAccount("NON_EXISTING_ACCOUNT", true);
    assertNotNull("Addresses", addresses);
    assertTrue("Addresses should have no addresses", addresses.size() == 0);
  }

  @Ignore
  @Test
  public void getAddressesByAccountWatchOnlyAddressTest() throws OapException {
    OpenAssetsProtocol oap = OpenAssetsProtocol.get();
    CoinAddress receivingAddress = Wallet.get().getReceivingAddress(oap.chain().db(), UnitTestHelper.ACCOUNT_GET_ADDRESSES);
//        System.out.println("receivingAddress="+receivingAddress);

    for (int index : UnitTestHelper.SEND_MANY_RECEIVER_ADDRESS_INDEXES) {
      String address = UnitTestHelper.getBitcoinAddressAt(index);
      UnitTestHelper.importAddress(UnitTestHelper.ACCOUNT_GET_ADDRESSES, address);
    }

    List<CoinAddress> addresses = oap.getAddressesByAccount(UnitTestHelper.ACCOUNT_GET_ADDRESSES, false);
//        for(CoinAddress a : addresses) {
//            System.out.println(a);
//        }
    assertNotNull("Addresses", addresses);
    for (int index : UnitTestHelper.SEND_MANY_RECEIVER_ADDRESS_INDEXES) {
      CoinAddress address = CoinAddress.from(UnitTestHelper.getBitcoinAddressAt(index));
      assertTrue("Addresses should not have an imported address", !addresses.contains(address));
    }
  }

  //
  // TestWithWalletSampleData
  //

  @Test
  public void getAddressesByAccountAPITest() throws OapException {
    WalletHandler handler = WalletHandler.get();
    WalletSampleDataProvider provider = getDataProvider();
    String account = null;

    boolean includeWatchOnly = true;
    CoinAddress coinAddress = null;
    List<CoinAddress> coinAddresses = null;

    account = provider.internalAccount();
    coinAddress = provider.internalAddressData().address;
    coinAddresses = OpenAssetsProtocol.get().getAddressesByAccount(account, includeWatchOnly);
    assertTrue("getAddressByAccount should contain INTERNAL ADDRESS", coinAddresses.contains(coinAddress));
    for (String a : provider.accounts()) {
      coinAddresses = handler.getAddressesByAccount(a, includeWatchOnly);
      for (AddressData data : provider.addressesOf(a)) {
        assertTrue("getAddressByAccount should contain CoinAdress", coinAddresses.contains(data.address));
      }
    }
  }

  @Test
  public void getBalanceTest() throws OapException {
    BigDecimal exp = BigDecimal.valueOf(50)
      .subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 4).getValue());

    for (String account : provider.accounts()) {
      BigDecimal balance = OpenAssetsProtocol.get().getBalance(account, 0, true);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
    }
  }

  @Test
  public void getAssetBalanceTest() throws OapException {
    WalletHandler handler = WalletHandler.get();
    WalletSampleDataProvider provider = getDataProvider();
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    HashSet<AssetId> assetIdFilter = new HashSet<AssetId>();
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      List<AssetBalanceDesc> descs = OpenAssetsProtocol.get().getAssetBalance(account, 1, true, new ArrayList<String>());
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", descs.size() == 2);
      for(AssetBalanceDesc desc : descs) {
        assertEquals("ASSET BALACNE SHOULD BE", 10000L, desc.getQuantity());
      }
    }
  }

  @Test
  public void listTransactionTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = OpenAssetsProtocol.get().coloringEngine();

    List<OapTransactionDescriptor> descs = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset(
      provider.internalAccount(),
      1000,
      0,
      true
    );
    assertEquals(provider.internalAccount() + "HAS 1 TRANSACTION", 1, descs.size());
    assertEquals(provider.internalAccount() + "THE ONLY TRANSACTION should be generation transaction", "generate", descs.get(0).getCategory());

    for(String account : provider.accounts()) {
      List<WalletTransactionDescriptor> wdescs = OpenAssetsProtocol.get().wallet().walletInterface().listTransactions(
        account,
        1000,
        0,
        true
      );
      descs = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset(
        account,
        1000,
        0,
        true
      );
      assertEquals("The result count of listTransaction and listTransactionWithAsset shoul be eaual", wdescs.size(), descs.size());

      HashMap<String, Long> assetQuantities = new HashMap<String, Long>();
      for(int i = 0;i < wdescs.size();i++) {
        assertTrue(
          "OapTransactionDescriptor should match WalletTransactionDescriptor",
          compareTransactionDescriptors(wdescs.get(i), descs.get(i))
        );
        if (new CoinAmount(wdescs.get(i).getAmount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
          assertTrue("OpenAssetProtocol output should have asset_id", descs.get(i).getAsset_id() != null);
          assertTrue("OpenAssetProtocol output should have quantity", descs.get(i).getQuantity() != null);

          String assetId = descs.get(i).getAsset_id();
          int quantity = (Integer) descs.get(i).getQuantity();
          Long l = assetQuantities.get(assetId);
          l = (l == null) ? 0 : l;
          if (descs.get(i).getCategory().equals("send")) {
            l -= quantity;
          } else {
            l += quantity;
          }
          assetQuantities.put(assetId, l);
        }
      }
      for(Long q : assetQuantities.values()) {
          assertEquals("THE TOTAL QUANTITY OF EACH ASSET SHOULD BE 10000", 10000L, q.longValue());
      }
    }
  }

  private boolean compareTransactionDescriptors(WalletTransactionDescriptor w, OapTransactionDescriptor o) {
    return w.getCategory().equals(o.getCategory()) &&
      w.getTxid().equals(o.getTxid()) &&
      w.getVout().equals(o.getVout()) &&
      w.getAmount().equals(o.getAmount()) &&
      w.getConfirmations().equals(o.getConfirmations()) &&
      w.getAddress().equals(o.getAddress());
  }
}
