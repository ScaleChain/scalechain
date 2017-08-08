package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.blockchain.transaction.PrivateKey;
import io.scalechain.blockchain.transaction.PublicKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shannon on 17. 1. 9.
 */
public class AddressDataProvider implements IAddressDataProvider {

  public static final String DEFAULT_MINING_ACCOUNT = "_FOR_TEST_ONLY";

  private String internalAccount;
  private AddressData internalAddressData;

  public AddressDataProvider(String internalAccount, AddressData internalAddressData) {
    if (internalAccount != null) {
      this.internalAccount = internalAccount;
      this.internalAddressData = internalAddressData;
    } else {
      try {
        this.internalAccount = DEFAULT_MINING_ACCOUNT;
        this.internalAddressData = createAccountAddresses(DEFAULT_MINING_ACCOUNT, 1)[0];
      }catch(OapException e) {
      }
    }

  }
  private String[] accounts = {
    ACCOUNT_IMPORTER,
    ACCOUNT_SENDER,
    ACCOUNT_RECEIVER
  };

  private Map<String, AddressData[]> accountAddressData = new HashMap<String, AddressData[]>();
  private HashMap<String, AddressData> addressAndAddressData = new HashMap<String, AddressData>();

  public String internalAccount() {
    return internalAccount;
  }
  public AddressData internalAddressData() {
    return internalAddressData;
  }
  public String[] accounts() {
    return accounts;
  }
  public AddressData[] addressesOf(String account) {
    if (account.equals(internalAccount)) return new AddressData[] { internalAddressData };
    return accountAddressData.get(account);
  }
  public AddressData addressDataOf(String coinaddressOrAssetAddressOrAssetId) {
    return addressAndAddressData.get(coinaddressOrAssetAddressOrAssetId);
  }

  public AddressData[] createAccountAddresses(String account, int size) throws OapException {
    AddressData[] result = new AddressData[size];
    for (int i = 0; i < size; i++) {
      result[i] = generateAddress();
      onAddressGeneration(account, result[i].address, (i == size - 1) ? result[i].privateKey : null);
    }
    return result;
  }
  public AddressData[] watchOnlyAddressesOf(String account) {
    AddressData[] addressData = accountAddressData.get(account);
    AddressData[] result = new AddressData[addressData.length - 1];
    System.arraycopy(addressData, 0, result, 0, result.length);
    return result;
  }

  public AddressData receivingAddressOf(String account) {
    AddressData[] addressData = accountAddressData.get(account);
    return addressData[addressData.length - 1];
  }

  @Override
  public PrivateKey[] privateKeysOf(String account) {
    AddressData[] addressData = accountAddressData.get(account);
    PrivateKey[] result = new PrivateKey[addressData.length];
    for(int i = 0;i < addressData.length;i++) {
      result[i] = addressData[i].privateKey;
    }
    return result;
  }

  public void generateAccountsAndAddresses() throws OapException {
    for (String account : accounts) {
      AddressData[] data = createAccountAddresses(account, 2);
      accountAddressData.put(account, data);
      for(AddressData d : data) {
        addressAndAddressData.put(d.address.base58(), d);
        addressAndAddressData.put(d.assetAddress.base58(), d);
        addressAndAddressData.put(d.assetId.base58(), d);
      }
    }
  }

  private AddressData generateAddress() throws OapException {
    PrivateKey privateKey = PrivateKey.generate();
    CoinAddress address = CoinAddress.from(privateKey);
    return new AddressData(privateKey, PublicKey.from(privateKey), ParsedPubKeyScript.from(privateKey), address, AssetAddress.fromCoinAddress(address), AssetId.from(address));
  }

  //
  // AddressGeneration Event
  //
  List<IAddressGenerationListener> listeners = new ArrayList<IAddressGenerationListener>();
  public void addListener(IAddressGenerationListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void removeListener(IAddressGenerationListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  private void onAddressGeneration(String account, CoinAddress address, PrivateKey privateKey) {
    synchronized (listeners) {
      for(IAddressGenerationListener listener : listeners) {
        listener.onAddressGeneration(account, address, privateKey);
      }
    }
  }
}
