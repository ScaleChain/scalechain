package io.scalechain.blockchain.oap.coloring;

import io.scalechain.wallet.WalletTransactionDescriptor;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.TestWithWalletSampleData;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.UnspentCoinDescriptor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by shannon on 16. 12. 8.
 */
public class OapWalletTest extends TestWithWalletSampleData {
  //
  // ASSET
  //  WE HAVE 6 ASSET TYPES ISSUED.
  //    EACH ASSET 10000 QUANTITY IN TOTAL.
  //
  // COIN
  //   SUM ALL COINS = 200BTC - 12 * DEFAULT FEES
  //   4 50BTC GENERATION TRANSACTION : 200 BTC
  //  12 TRANSACTION : 12 * DEFAULT FEES
  //     3 TRANFER TRANSACTION
  //     6 ASSET ISSUE TRANACTION
  //     3 ASSET TRANSFER TRANSACTION

  private HashMap<AssetId, Long> pushAssetQuantity(HashMap<AssetId, Long> quantityMap, AssetId assetId, int quantity) {
    if (quantityMap == null) {
      quantityMap = new HashMap<AssetId, Long>();
    }
    Long sum = quantityMap.get(assetId);
    sum = (sum == null ? 0 : sum.longValue()) + quantity;
    quantityMap.put(assetId, sum);

    return quantityMap;
  }

  @Test
  public void listUnspentAllTest() throws OapException {
    long expCoinAmountSum = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 200 - 12 * IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int assetCount = 0, coinCount = 0;
    HashMap<AssetId, Long> expAssetQuantaties = null;
    List<UnspentAssetDescriptor> unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, new ArrayList<CoinAddress>(), true);
    long coinAmountSum = 0;
    for (UnspentAssetDescriptor unspent : unspents) {
      if (unspent.isColored()) {
        assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
        UnspentAssetDescriptor assetDescrptor = (UnspentAssetDescriptor)unspent;
        expAssetQuantaties = pushAssetQuantity(expAssetQuantaties, assetDescrptor.getAssetId(), assetDescrptor.getQuantity());
        assetCount++;
        coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
      } else {
        coinCount++;
        coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
      }
    }

    // _FOR_TEST ONLY : 1 ADDRESS - 1 UNSPENT COIN
    // IMPORTER       : 2 ADDRESSES - 1 UNSPENTS COIN FOR EACH ADDRESS
    // SENDER         : 2 ADDRESSES - 1 UNSPENTS COIN FOR EACH ADDRESS
    // RECEIVER       : 2 ADDRESSES - 1 UNSPENTS COIN FOR EACH ADDRESS
    assertEquals("Unspent coin count should be 7", 7, coinCount);
    assertEquals("Issued asset count should be 6", 6, expAssetQuantaties.size());
    for(Map.Entry<AssetId, Long> entry : expAssetQuantaties.entrySet()) {
      assertEquals("The sum asset of each asset quantity should be 10000", 10000, entry.getValue().longValue());
    }
    assertEquals("Total Coin Amounts should be ", expCoinAmountSum, coinAmountSum);
  }


  // COIN
  //   SUM ALL COINS = 200BTC - 12 * DEFAULT FEES
  //   4 50BTC GENERATION TRANSACTION : 200 BTC
  //  12 TRANSACTION : 12 * DEFAULT FEES
  //     3 TRANFER TRANSACTION
  //     6 ASSET ISSUE TRANACTION
  //     3 ASSET TRANSFER TRANSACTION
  // WITHOUT ASSETS : 200BTC - 12 * DEFAULT FEES - DUST * 9
  @Test
  public void listUnspentAllNoAsset() throws OapException {
    int expCoinCount = 0;
    List<UnspentAssetDescriptor> unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, new ArrayList<CoinAddress>(), true);
    for (UnspentAssetDescriptor unspent : unspents) {
      if (unspent.isColored()) {
        assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
      } else {
        expCoinCount++;

      }
    }

    long expCoinAmountSum = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 200 - 12 * IOapConstants.DEFAULT_FEES_IN_SATOSHI - 9 * IOapConstants.DUST_IN_SATOSHI;
    int assetCount = 0, coinCount = 0;
    long coinAmountSum = 0;
    unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, new ArrayList<CoinAddress>(), false);
    for (UnspentAssetDescriptor unspent : unspents) {
      if (unspent.isColored()) {
        assetCount++;
        coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
      } else {
        coinCount++;
        coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
      }
    }

    assertEquals("Asset Count should be 0", 0, assetCount);
    assertEquals("Coin Count should be 0", expCoinCount, coinCount);
    assertEquals("Total Coin Amounts should be ", expCoinAmountSum, coinAmountSum);
  }

  @Test
  public void listUnspentForEachAddressTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    long expCoinAmountSum = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 200 - 12 * IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int expCoinCount = 0, expAssetCount = 0;
    HashMap<AssetId, Long> expAssetQuantaties = null;
    List<UnspentAssetDescriptor> unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, new ArrayList<CoinAddress>(), true);
    for (UnspentAssetDescriptor unspent : unspents) {
      if (unspent.isColored()) {
        assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
        UnspentAssetDescriptor assetDescrptor = (UnspentAssetDescriptor)unspent;
        expAssetQuantaties = pushAssetQuantity(expAssetQuantaties, assetDescrptor.getAssetId(), assetDescrptor.getQuantity());
        expAssetCount++;
      } else {
        expCoinCount++;
      }
    }

    // GET UNSPENTS FOR EACH ACCOUNT.
    String[] accounts = new String[provider.accounts().length + 1];
    accounts[0] = provider.internalAccount();
    System.arraycopy(getDataProvider().accounts(), 0, accounts, 1, accounts.length - 1);

    int assetCount = 0, coinCount = 0;
    long coinAmountSum = 0;
    for(String account : accounts) {
      List<CoinAddress> list = new ArrayList<CoinAddress>();
      AddressData[] accountAddresses = provider.addressesOf(account);
      for(AddressData a : accountAddresses) {
        list.add(a.address);
      }
      unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, list, true);
      for (UnspentAssetDescriptor unspent : unspents) {
        if (unspent.isColored()) {
          assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
          assetCount++;
          coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();

        } else {
          coinCount++;
          coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
        }
      }
    }

    assertEquals("Coin Count should be", expCoinCount, coinCount);
    assertEquals("Asset Count should be", expAssetCount, assetCount);
    assertEquals("Unspent coin count should be 7", 7, coinCount);
    for(Map.Entry<AssetId, Long> entry : expAssetQuantaties.entrySet()) {
      assertEquals("The sum asset of each asset quantity should be 10000", 10000, entry.getValue().longValue());
    }
    assertEquals("Total Coin Amounts should be ", expCoinAmountSum, coinAmountSum);
  }

  @Test
  public void listUnspentForEachAddressNoAssetTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    long expCoinAmountSum = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 200 - 12 * IOapConstants.DEFAULT_FEES_IN_SATOSHI - 9 * IOapConstants.DUST_IN_SATOSHI;

    int expCoinCount = 0, expAssetCount = 0;
    HashMap<AssetId, Long> expAssetQuantaties = null;
    List<UnspentAssetDescriptor> unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, new ArrayList<CoinAddress>(), false);
    for (UnspentAssetDescriptor unspent : unspents) {
      if (unspent.isColored()) {
        assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
        UnspentAssetDescriptor assetDescrptor = (UnspentAssetDescriptor)unspent;
        expAssetQuantaties = pushAssetQuantity(expAssetQuantaties, assetDescrptor.getAssetId(), assetDescrptor.getQuantity());
        expAssetCount++;
      } else {
        expCoinCount++;
      }
    }

    // GET UNSPENTS FOR EACH ACCOUNT.
    String[] accounts = new String[provider.accounts().length + 1];
    accounts[0] = provider.internalAccount();
    System.arraycopy(getDataProvider().accounts(), 0, accounts, 1, accounts.length - 1);

    int assetCount = 0, coinCount = 0;
    long coinAmountSum = 0;
    for(String account : accounts) {
      List<CoinAddress> list = new ArrayList<CoinAddress>();
      AddressData[] accountAddresses = provider.addressesOf(account);
      for(AddressData a : accountAddresses) {
        list.add(a.address);
      }
      unspents = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, list, false);
      for (UnspentAssetDescriptor unspent : unspents) {
        if (unspent.isColored()) {
          assertEquals("Value of uspent should be DUST", IOapConstants.DUST_IN_SATOSHI, new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits());
          assetCount++;
          coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();

        } else {
          coinCount++;
          coinAmountSum += new CoinAmount(unspent.getUnspentCoinDescriptor().getAmount()).coinUnits();
        }
      }
    }

    assertEquals("Coin Count should be", expCoinCount, coinCount);
    assertEquals("Asset Count should be", expAssetCount, assetCount);
    assertEquals("Unspent coin count should be 7", 7, coinCount);
    assertEquals("Total Coin Amounts should be ", expCoinAmountSum, coinAmountSum);
  }


  @Ignore
  @Test
  public void listTransactionsTest() {
  }
}
