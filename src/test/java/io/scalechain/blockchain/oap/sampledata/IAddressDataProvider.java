package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.transaction.PrivateKey;

/**
 * Created by shannon on 17. 1. 9.
 */
public interface IAddressDataProvider {

  public static final String ACCOUNT_IMPORTER = "IMPORTER";
  public static final String ACCOUNT_SENDER   = "SENDER";
  public static final String ACCOUNT_RECEIVER = "RECEIVER";

  public String internalAccount();
  public AddressData internalAddressData();
  public String[] accounts();
  public AddressData[] addressesOf(String account);
  public AddressData[] watchOnlyAddressesOf(String account);
  public AddressData receivingAddressOf(String account);
  public PrivateKey[] privateKeysOf(String account);
  public AddressData addressDataOf(String coinaddressOrAssetAddressOrAssetId);
}
