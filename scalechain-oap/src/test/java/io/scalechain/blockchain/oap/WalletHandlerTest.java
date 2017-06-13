package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.IAddressDataProvider;
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.OapTransactionDescriptor;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.WalletTransactionDescriptor;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by shannon on 17. 1. 5.
 */
public class WalletHandlerTest extends TestWithWalletSampleData {
  //
  // GetBalance Related Internal Methods
  //
  @Test
  public void getBalanceTest() throws OapException {
    //
    // EXPECTED BALANCE = 50BTC - DEFAULT_FEE * 3
    //   EACH ACCOUNT
    //     RECEVIVED A GENERATION TRANSACTION
    //       TRANSFERED COINS TO ITS WATCH-ONLY ADDRESS
    //       ISSUED AN ASSET FROM ITS RECEIVING ADDRESS TO ITS WATCH-ONLY ADDRESS
    //       TRANSFERED ASSET FROM ITS RECEIVING ADDRESS TO ITS WATCH-ONLY ADDRESS
    //       ISSUED AN ASSET FROM ITS WATCH-ONLY ADDRESS TO ITS RECEIVING ADDRESS
    BigDecimal exp = BigDecimal.valueOf(50)
      .subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 4).getValue());
    for (String account : provider.accounts()) {
      BigDecimal balance = new BigDecimal(0);
      int count = 1000;
      int skip = 0;
      int readCount;
      do {
        Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, account, count, skip, 1, true);
        readCount = pair.getFirst();
        balance = pair.getSecond();
        skip += readCount;
      } while (readCount == count);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
    }
  }

  @Ignore
  @Test
  public void getBalanceReadingMultipleTimesTest() throws OapException {
    //
    // FIXME
    //   It seems that there is a bug in Wallet.listTransaction().
    //   It return less descripts than expected when skip != 0.
    //   Check listAssetTransacionsTest() Method.
    //

    //
    // EXPECTED BALANCE = 50BTC - DEFAULT_FEE * 3
    //   EACH ACCOUNT
    //     RECEVIVED A GENERATION TRANSACTION
    //       TRANSFERED COINS TO ITS WATCH-ONLY ADDRESS
    //       ISSUED AN ASSET FROM ITS RECEIVING ADDRESS TO ITS WATCH-ONLY ADDRESS
    //       TRANSFERED ASSET FROM ITS RECEIVING ADDRESS TO ITS WATCH-ONLY ADDRESS
    //       ISSUE AN ASSET FROM ITS WATCH-ONLY ADDRESS TO ITS RECEIVING ADDRESS ADDRESS

    BigDecimal exp = BigDecimal.valueOf(50).subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 4).getValue());
    for (String account : provider.accounts()) {
      BigDecimal balance = new BigDecimal(0);
      int count = 2;
      long skip = 0;
      int readCount;
      do {
        Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, account, count, skip, 1, true);
        readCount = pair.getFirst();
        balance = balance.add(pair.getSecond());
        skip += readCount;
      } while (readCount == count);
      System.out.println(skip);
      assertTrue("Total read count should be greater than 10", skip > 5);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
    }
  }

  @Test
  public void getBalanceWatchOnlyFalseTest() throws OapException {
    //
    // EXPECTED
    // RECEIVING ADDRESS BALANCE = 50BTC
    //                      - DEFAULT_FEE * 3           ==> NUMBER OF TX FOR THE RECEIVING ADDRESS
    //                      - 5 * (ACCOUNT INDEX + 1)   ==> AMOUNT TRANFERED TO WATCH_ONLY ADDRESS
    int index = 0;
    for (String account : provider.accounts()) {
      BigDecimal exp =
        BigDecimal.valueOf(50)
          .subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 3).getValue())
          .subtract(BigDecimal.valueOf(5 * (index + 1)));

      BigDecimal balance = new BigDecimal(0);
      int count = 1000;
      int skip = 0;
      int readCount;
      do {
        Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, account, count, skip, 1, false);
        readCount = pair.getFirst();
        balance = pair.getSecond();
        skip += readCount;
      } while (readCount == count);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
      index++;
    }
  }

  @Test
  public void getBalanceInvalidAccountTest() throws OapException {
    BigDecimal exp = BigDecimal.valueOf(0);
    BigDecimal balance = new BigDecimal(0);
    int count = 1000;
    int skip = 0;
    int readCount;
    Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, "A", count, skip, 1, false);
    assertEquals("Read count should be 0", 0, pair.getFirst().intValue());
  }


  //
  // getBalance() Method uses Wallet.listTransactionsWithAsset() internally.
  //
  // account : The name of an account to get transactinos from.
  //   Use an empty string (“”) to get transactions for the default account.
  //   Default is * to get transactions for all accounts.
  //

  @Test
  public void getBalanceAccountNoneTest() throws OapException {
    
    // INTERNAL ACCOUNT THAT HAS 50BTC
    //  3 ACCOUNTS
    // EXPECTED BALANCE = 50BTC
    //                      - DEFAULT_FEE * 4           ==> NUMBER OF TX FOR THE RECEIVING ADDRESS
    //
    // EXPECTED BALANCE = 50BTC + (50BTC - DEFAULT_FEE * 3) * 4
    //                  ==> 200 BTC - DEFAULT_FEE * 12

    BigDecimal exp = BigDecimal.valueOf(200)
      .subtract(BigDecimal.valueOf(12).multiply(IOapConstants.DEFAULT_FEES_IN_BITCOIN));

    BigDecimal balance = new BigDecimal(0);
    int count = 1000;
    int skip = 0;
    Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, null, count, skip, 1, true);
    balance = pair.getSecond();

    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }

  @Test
  public void getBalanceAccountNoneWatchOnlyFalseTest() throws OapException {
    // INTERNAL ACCOUNT   = 50BTC
    // 3 ACCOUNTS
    //   EXPECTED BALANCE = 50BTC
    //                      - DEFAULT_FEE * 4           ==> NUMBER OF TX FOR THE RECEIVING ADDRESS
    //
    // EXPECTED = 50BTC * 4 - 12 * DEFAULT_FEE
    //          ==> 170BTC - 12 * DEFAULT_FEE

    BigDecimal exp =
      BigDecimal.valueOf(170)
        .subtract(BigDecimal.valueOf(9).multiply(IOapConstants.DEFAULT_FEES_IN_BITCOIN));

    BigDecimal balance = new BigDecimal(0);
    int count = 1000;
    int skip = 0;
    Pair<Integer, BigDecimal> pair = walletHandler.getBalance(balance, null, count, skip, 1, false);
    balance = pair.getSecond();

    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }

  //
  // GetAssetBalance Related Internal Methods
  //


  //  public int getAssetBalance(
  //    HashMap<String, Long> balances,
  //    Option<String> accountOption, int count, long skip, long minConf, boolean includeWatchOnly,
  //    HashSet<AssetId> assetIds
  //  ) throws OapException {
  @Test
  public void getAssetBalanceTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.

    HashSet<String> assetIdFilter = new HashSet<String>();
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      long skip = 0;
      int count = 1000;
      int readCount;

      HashMap<String, Long> map = new HashMap<String, Long>();
      do {
        readCount = walletHandler.getAssetBalance(map, account, count, skip, 1, true, assetIdFilter);
        skip += readCount;
      } while (readCount == count);
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", map.size() == 2);
      for (AddressData d : accountAddresses) {
        String assetId = d.assetId.base58();
        assertEquals("ASSET BALACNE SHOULD BE", 10000L, map.get(assetId).longValue());
      }
    }
  }

  @Test
  public void getAssetBalanceAssetFilterTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.

    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      HashSet<String> assetIdFilter = new HashSet<String>();
      assetIdFilter.add(accountAddresses[0].assetId.base58());

      long skip = 0;
      int count = 1000;
      int readCount;

      HashMap<String, Long> map = new HashMap<String, Long>();
      do {
        readCount = walletHandler.getAssetBalance(map, account, count, skip, 1, true, assetIdFilter);
        skip += readCount;
      } while (readCount == count);
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", map.size() == 1);
      assertEquals("ASSET BALACNE SHOULD BE", 10000L, map.get(accountAddresses[0].assetId.base58()).longValue());
    }
  }

  @Ignore
  @Test
  public void getAssetBalanceReadingMultipleTimesTest() throws OapException {
    //
    // FIXME
    //   It seems that there is a bug in Wallet.listTransaction().
    //   It return less descripts than expected when skip != 0.
    //   Check listAssetTransacionsTest() Method.
    //

    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.

    HashSet<String> assetIdFilter = new HashSet<String>();
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      int count = 2;
      long skip = 0;
      int readCount;

      HashMap<String, Long> map = new HashMap<String, Long>();
      do {
        readCount = walletHandler.getAssetBalance(map, account, count, skip, 1, true, assetIdFilter);
        skip += readCount;
      } while (readCount == count);
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", map.size() == 2);
      for (AddressData d : accountAddresses) {
        String assetId = d.assetId.base58();
        assertEquals("ASSET BALACNE SHOULD BE", 10000L, map.get(assetId).longValue());
      }
    }
  }

  @Test
  public void getAssetBalanceWatchOnlyFalseTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    HashSet<String> assetIdFilter = new HashSet<String>();
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      long skip = 0;
      int count = 1000;
      int readCount;

      HashMap<String, Long> map = new HashMap<String, Long>();
      do {
        readCount = walletHandler.getAssetBalance(map, account, count, skip, 1, false, assetIdFilter);
        skip += readCount;
      } while (readCount == count);
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", map.size() == 2);
      assertEquals("ASSET BALACNE OF THE ASSET ISSED FROM WATCH-ONLY ADDRESS SHOULD BE",
        10000L,
        map.get(accountAddresses[0].assetId.base58()).longValue()
      );
      assertEquals("ASSET BALACNE OF THE ASSET ISSED FROM RECEIVING ADDRESS SHOULD BE",
        10000L - (index + 1) * 2000,
        map.get(accountAddresses[accountAddresses.length - 1].assetId.base58()).longValue()
      );
      index++;
    }
  }

  @Test
  public void getAssetBalanceInvalidAccountTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    HashSet<String> assetIdFilter = new HashSet<String>();
    String account = "NON_EXISTING_ACCOUNT";
    AddressData[] accountAddresses = provider.addressesOf(account);
    long skip = 0;
    int count = 1000;
    int readCount;

    HashMap<String, Long> map = new HashMap<String, Long>();
    do {
      readCount = walletHandler.getAssetBalance(map, account, count, skip, 1, true, assetIdFilter);
      skip += readCount;
    } while (readCount == count);
    assertTrue("ASSET BALACNES SHOULD HAVE NO ENTRIES", map.size() == 0);
  }


  //
  // getBalance() Method uses Wallet.listTransactionsWithAsset() internally.
  //
  // account : The name of an account to get transactinos from.
  //   Use an empty string (“”) to get transactions for the default account.
  //   Default is * to get transactions for all accounts.
  //

  @Test
  public void getAssetBalanceAccountNoneTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    HashSet<String> assetIdFilter = new HashSet<String>();
    long skip = 0;
    int count = 1000;
    int readCount;

    HashMap<String, Long> map = new HashMap<String, Long>();
    do {
      readCount = walletHandler.getAssetBalance(map, null, count, skip, 1, true, assetIdFilter);
      skip += readCount;
    } while (readCount == count);
    assertTrue("ASSET BALACNES SHOULD HAVE 6 ENTRIES", map.size() == 6);
    for (long quanity : map.values()) {
      assertEquals("ASSET QUANTITY SHOULD BE 1000", 10000, quanity);
    }
  }

  @Test
  public void getAssetBalanceAccountNoneWatchOnlyFalseTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    HashSet<String> assetIdFilter = new HashSet<String>();
    long skip = 0;
    int count = 1000;
    int readCount;

    HashMap<String, Long> map = new HashMap<String, Long>();
    do {
      readCount = walletHandler.getAssetBalance(map, null, count, skip, 1, false, assetIdFilter);
      skip += readCount;
    } while (readCount == count);
    assertTrue("ASSET BALACNES SHOULD HAVE 6 ENTRIES", map.size() == 6);

    for(String account : provider.accounts()) {
      AddressData[] accountAddress = provider.addressesOf(account);
      assertEquals(
        "QUANTITY OF ASSET ISSED FROM WATCH-ONLY ADDRESS SHOULD BE",
        10000L, ((Long) map.get(accountAddress[0].assetId.base58())).longValue()
      );
      assertEquals(
        "QUANTITY OF ASSET ISSED FROM RECEIVING ADDRESS SHOULD BE",
        10000L - (index + 1) * 2000, ((Long) map.get(accountAddress[accountAddress.length - 1].assetId.base58())).longValue()
      );
      index++;
    }
  }

  //
  //  API METHOD TESTS
  //
  @Test
  public void getAddressesByAccountAPITest() throws OapException {
    String account = null;

    boolean includeWatchOnly = true;
    CoinAddress coinAddress = null;
    List<CoinAddress> coinAddresses = null;

    account = provider.internalAccount();
    coinAddress = provider.internalAddressData().address;
    coinAddresses = walletHandler.getAddressesByAccount(account, includeWatchOnly);
    assertTrue("getAddressByAccount should contain INTERNAL ADDRESS", coinAddresses.contains(coinAddress));
    for (String a : provider.accounts()) {
      coinAddresses = walletHandler.getAddressesByAccount(a, includeWatchOnly);
      for (AddressData data : provider.addressesOf(a)) {
        assertTrue("getAddressByAccount should contain CoinAdress", coinAddresses.contains(data.address));
      }
    }
  }

  @Test
  public void getAddressesByAccountAPIWatchOlnyFalseTest() throws OapException {
    String account = null;

    boolean includeWatchOnly = false;
    CoinAddress coinAddress = null;
    List<CoinAddress> coinAddresses = null;

    account = provider.internalAccount();
    coinAddress = provider.internalAddressData().address;
    coinAddresses = walletHandler.getAddressesByAccount(account, includeWatchOnly);
    assertTrue("getAddressByAccount should contain INTERNAL ADDRESS", coinAddresses.contains(coinAddress));

    for (String a : provider.accounts()) {
      coinAddresses = walletHandler.getAddressesByAccount(a, includeWatchOnly);
      assertEquals("getAddressByAccount should return a list of size 1", 1, coinAddresses.size());
      AddressData[] data = provider.addressesOf(a);
      assertEquals("The returned address sould be receving address", coinAddresses.get(0), data[data.length - 1].address);
    }
  }

  @Test
  public void getAddressesByAccountAPINonExistingAccountTest() throws OapException {
    String account = null;

    boolean includeWatchOnly = true;
    CoinAddress coinAddress = null;
    List<CoinAddress> coinAddresses = null;

    account = provider.internalAccount();
    coinAddresses = walletHandler.getAddressesByAccount(account + account, includeWatchOnly);
    assertTrue("getAddressByAccount should contain No address", coinAddresses.size() == 0);
  }


  @Test
  public void getBalanceAPITest() throws OapException {
    BigDecimal exp = BigDecimal.valueOf(50)
      .subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 4).getValue());
    for (String account : provider.accounts()) {
      BigDecimal balance = walletHandler.getBalance(account, 1, true);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
    }
  }

  @Test
  public void getBalanceAPIIncludeWatchOnlyFalseTest() throws OapException {
    int index = 0;
    for (String account : provider.accounts()) {
      BigDecimal exp =
        BigDecimal.valueOf(50)
          .subtract(CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI * 3).getValue())
          .subtract(BigDecimal.valueOf(5 * (index + 1)));

      BigDecimal balance = walletHandler.getBalance(account, 1, false);
      assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
      index++;
    }
  }


  @Test
  public void getBalanceAPIInvalidAccountOptionTest() throws OapException {
    BigDecimal exp = BigDecimal.valueOf(0);
    String account = getDataProvider().accounts()[0];

    BigDecimal balance = walletHandler.getBalance(account + account, 1, true);
    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }

  @Test
  public void getBalanceAPIEmptyAccountTest() throws OapException {
    BigDecimal exp = BigDecimal.valueOf(0);
    String account = getDataProvider().accounts()[0];

    BigDecimal balance = walletHandler.getBalance("", 1, true);

    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }

  @Test
  public void getBalanceAPIStarAccountTest() throws OapException {
    BigDecimal exp =
      BigDecimal.valueOf(200)
        .subtract(BigDecimal.valueOf(12).multiply(IOapConstants.DEFAULT_FEES_IN_BITCOIN));
    BigDecimal balance = walletHandler.getBalance("*", 1, true);
    System.out.println(exp);
    System.out.println(balance);
    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }

  @Test
  public void getBalanceAPIStarAccountIncludeWatchOnlyFalseTest() throws OapException {
    BigDecimal exp =
      BigDecimal.valueOf(170)
        .subtract(BigDecimal.valueOf(9).multiply(IOapConstants.DEFAULT_FEES_IN_BITCOIN));

    BigDecimal balance = walletHandler.getBalance("*", 1, false);
    System.out.println(exp);
    System.out.println(balance);
    assertTrue("Balance should be equal to " + exp.toString(), exp.compareTo(balance) == 0);
  }


  @Test
  public void getAssetBalanceAPITest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    HashSet<AssetId> assetIdFilter = new HashSet<AssetId>();
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      List<AssetBalanceDesc> descs = walletHandler.getAssetBalance(account, 1, true, new ArrayList<String>());
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", descs.size() == 2);
      for(AssetBalanceDesc desc : descs) {
        assertEquals("ASSET BALACNE SHOULD BE", 10000L, desc.getQuantity());
      }
    }
  }

  @Test
  public void getAssetBalanceAPIWatchOnlyFalseTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      List<AssetBalanceDesc> descs = walletHandler.getAssetBalance(account, 1, false, new ArrayList<String>());
      assertTrue("ASSET BALACNES SHOULD HAVE 2 ENTRIES", descs.size() == 2);
      for(AssetBalanceDesc desc : descs) {
        if (accountAddresses[0].assetId.base58().equals(desc.getAssetId())) {
          assertEquals("ASSET BALACNE SHOULD BE", 10000L, desc.getQuantity());
        } else {
          assertEquals(
            "QUANTITY OF ASSET ISSED FROM RECEIVING ADDRESS SHOULD BE",
            10000L - (index + 1) * 2000,
            desc.getQuantity()
          );
        }
      }
      index++;
    }
  }

  @Test
  public void getAssetBalanceAPIAssetFilterTest() throws OapException {
    //
    // EACH ACCOUNT HAS 2 TYPES OF ASSET      WATCH-ONLY ADDRESS          RECEIVING ADDRESS
    //    1 ISSUED FROM RECEIVING ADDRESS     (account index + 1) * 2000  10000 - (account index + 1) * 2000
    //    2 ISSUED FROM WATCH-ONLY ADDRESS                                10000
    // ASSET QUANTITY IS 10000.
    int index = 0;
    for (String account : provider.accounts()) {
      AddressData[] accountAddresses = provider.addressesOf(account);
      List<String> assetFitler = new ArrayList<String>();
      assetFitler.add(accountAddresses[0].assetId.base58());
      List<AssetBalanceDesc> descs = walletHandler.getAssetBalance(account, 1, true, assetFitler);
      assertTrue("ASSET BALACNES SHOULD HAVE 1 ENTRIES", descs.size() == 1);
      assertEquals("ASSET BALACNE SHOULD BE", 10000L, descs.get(0).getQuantity());
      index++;
    }
  }

  @Test
  public void listAssetTransacionsTest() throws OapException {
    String account = IAddressDataProvider.ACCOUNT_SENDER;



    List<OapTransactionDescriptor> descriptors = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset(account, 20, 0, true);
    assertEquals("Senders transaction list should have", provider.receivingTransactionCountOf(account) + provider.sendingTransactionCountOf(account), descriptors.size());
  }

  public boolean checkCointains(List<OapTransactionDescriptor>  array, OapTransactionDescriptor desc) {
    for(OapTransactionDescriptor e : array) {
      Hash txid = e.getTxid();
      int vout = (Integer)e.getVout();
      if (txid.equals(desc.getTxid())) {
        if (vout == (Integer)desc.getVout()) {
          return true;
        }
      }
    }
    return false;
  }

  @Test
  public void listAssetTransacionsWatchOnlyTest() throws OapException {
    String account = IAddressDataProvider.ACCOUNT_SENDER;

    List<OapTransactionDescriptor> descriptorsIncludeWatchOnlyTrue = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset(account, 20, 0, true);
    assertEquals(
      "Senders transaction list should have",
      provider.receivingTransactionCountOf(account) + provider.sendingTransactionCountOf(account),
      descriptorsIncludeWatchOnlyTrue.size()
    );

    List<OapTransactionDescriptor> descriptorsIncludeWatchOnlyFalse = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset(account, 20, 0, false);

    int wathOnlyCount = 0;
    for(OapTransactionDescriptor desc : descriptorsIncludeWatchOnlyTrue) {
      if (!desc.getInvolvesWatchonly()) {
        assertTrue(
          "listAssetTransactionResult(includeWatchOnly => false) sholuld contain all of watch-only transaction of (listAssetTransactionResult(includeWatchOnly => true)",
          checkCointains(descriptorsIncludeWatchOnlyFalse, desc)
        );
      } else {
        wathOnlyCount++;
      }
    }
    assertEquals("Descriptor count with involve_watch_only true should be",
      wathOnlyCount,
      descriptorsIncludeWatchOnlyTrue.size() - descriptorsIncludeWatchOnlyFalse.size()
    );
    for(OapTransactionDescriptor desc : descriptorsIncludeWatchOnlyFalse) {
      assertTrue(
        "listAssetTransactionResult(includeWatchOnly => true) sholuld contain all of watch-only transaction of (listAssetTransactionResult(includeWatchOnly => false)",
        checkCointains(descriptorsIncludeWatchOnlyTrue, desc)
      );
    }
  }

  @Test
  public void listAssetTransacionsNonExsitingAccountTest() throws OapException {
    String account = "NON_EXSISTING_ACCOUNT";

    List<OapTransactionDescriptor> descriptors = OpenAssetsProtocol.get().wallet().listTransactionsWithAsset( account, 20, 0, true);
    assertEquals("Senders transaction list should have", 0, descriptors.size());
  }

  @Test
  @Ignore
  public void listTransactionTest() {
    //
    // FIXME : It seems that there is a bug in Wallet.listTransaction() when skip != 0;
    //   CHECK Wallet.scala 365, 382, checking transactin index should be done after list has been built.
    //   if (transactionIndex >= skip && transactionIndex < skip + count) {

    String account = provider.accounts()[1];
    IWalletInterface walletInterface = OpenAssetsProtocol.get().wallet().walletInterface();
    List<WalletTransactionDescriptor> descriptors = walletInterface.listTransactions(
      account,
      1000,
      0,
      true
    );
    int skip = descriptors.size() / 2;
    int count = descriptors.size() - skip;
    List<WalletTransactionDescriptor> descriptorsWithSkip = walletInterface.listTransactions(
      account,
      count,
      skip,
      true
    );
    assertEquals("listUnspent should skip " + skip + " descriptors", count, descriptorsWithSkip.size());
  }
}


