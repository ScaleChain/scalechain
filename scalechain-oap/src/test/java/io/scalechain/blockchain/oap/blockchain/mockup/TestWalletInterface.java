package io.scalechain.blockchain.oap.blockchain.mockup;

import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.OutputOwnership;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import io.scalechain.wallet.WalletTransactionDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by shannon on 16. 11. 21.
 */
public class TestWalletInterface implements IWalletInterface {
  TestBlockChainView       chainView;
  Wallet                   wallet;
  public TestWalletInterface(TestBlockChainView chainView, Wallet wallet) {
    this.chainView = chainView;
    this.wallet    = wallet;
  }

  @Override
  public List<UnspentCoinDescriptor> listUnspent(long minConfirmations, long maxConfirmations, List<CoinAddress> addresses) {
    List<CoinAddress> coinAddressesOption = null;
    if (addresses != null && addresses.size() > 0) {
      coinAddressesOption = addresses;
    } else {
      coinAddressesOption = null;
    }
    List<UnspentCoinDescriptor> descriptors = wallet.listUnspent(
      chainView.db(),
      chainView,
      minConfirmations,
      maxConfirmations,
      coinAddressesOption
    );
    return descriptors;
  }

  @Override
  public List<WalletTransactionDescriptor> listTransactions(String accountOption, int count, long skip, boolean includeWatchOnly) {
    List<WalletTransactionDescriptor> transactions = new ArrayList<WalletTransactionDescriptor>();
    // LIST TRANSACTIOINS...
    transactions.addAll(
      wallet.listTransactions(chainView.db(), chainView, accountOption, count, skip, includeWatchOnly)
    );
    return transactions;
  }

  @Override
  public List<CoinAddress> getAddressesByAccount(String accountOption, boolean includeWatchOnly) throws OapException {
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    for (OutputOwnership o :
      wallet.getStore().getOutputOwnerships(
        chainView.db(),
        accountOption
      )
    ) {
      // PrivateKey does not exists, watch only address
      if (!includeWatchOnly && wallet.getStore().getPrivateKeys(chainView.db(), o).isEmpty()) {
        continue;
      }
      // BUGBUG : OAP : CoinAddress.from(o.stringKey()) does not work.
      addresses.add((o instanceof CoinAddress) ? (CoinAddress) o : CoinAddress.from(o.stringKey()));
    }
    return addresses;
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return wallet.getReceivingAddress(chainView.db(), account);
  }
}