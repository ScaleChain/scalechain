package io.scalechain.blockchain.api.oap;

import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.blockchain.scalechain.ScalechainBlockchainInterface;
import io.scalechain.blockchain.oap.blockchain.scalechain.ScalechainWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.wallet.Wallet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shannon on 16. 12. 15.
 */
public class ScalechainIntefaceTest extends ApiTestWithSampleTransactions {

  @Test
  public void getChainTest() {
    ScalechainBlockchainInterface blockchainInterface = new ScalechainBlockchainInterface(Blockchain.get());
    assertNotNull("WALLET SHOULD BE CREATED", blockchainInterface);

    for(Hash hash : provider.s4IssueTxHashes) {
      Transaction tx = blockchainInterface.getTransation(hash);
      assertEquals("ISSUE TX should have marker output", 0L, tx.getOutputs().get(1).getValue());
    }
  }

  @Test
  public void getWalletTest() throws OapException {
    ScalechainWalletInterface walletInterface = new ScalechainWalletInterface(Blockchain.get(), Wallet.get());
    assertNotNull("WALLET SHOULD BE CREATED", walletInterface);
    for(String account : provider.accounts()) {
      CoinAddress coinAddress = walletInterface.getReceivingAddress(account);
      assertEquals("Receiving address of " + account + "should equals to", provider.receivingAddressOf(account).address, coinAddress);
    }
  }
}