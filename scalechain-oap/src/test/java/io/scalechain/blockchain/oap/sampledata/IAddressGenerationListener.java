package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.PrivateKey;

/**
 * Created by shannon on 17. 1. 9.
 */
public interface IAddressGenerationListener {
  public void onAddressGeneration(String account, CoinAddress address, PrivateKey privateKey);
}
