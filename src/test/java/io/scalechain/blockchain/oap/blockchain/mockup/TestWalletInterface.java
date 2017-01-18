package io.scalechain.blockchain.oap.blockchain.mockup;

import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.OutputOwnership;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import io.scalechain.wallet.WalletTransactionDescriptor;
import scala.Option;
import scala.Some;
import scala.collection.JavaConverters;

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
    Option<scala.collection.immutable.List<CoinAddress>> coinAddressesOption = null;
    if (addresses != null && addresses.size() > 0) {
      scala.collection.immutable.List<CoinAddress> list = JavaConverters.asScalaBufferConverter(addresses).asScala().toList();
      coinAddressesOption = new Some<scala.collection.immutable.List<CoinAddress>>(list);
    } else {
      coinAddressesOption = Option.empty();
    }
    scala.collection.immutable.List<UnspentCoinDescriptor> descriptors = wallet.listUnspent(
      chainView,
      minConfirmations,
      maxConfirmations,
      coinAddressesOption,
      chainView.db()
    );
    Collection result = JavaConverters.asJavaCollectionConverter(descriptors).asJavaCollection();
    return new ArrayList<UnspentCoinDescriptor>(result);
  }

  @Override
  public List<WalletTransactionDescriptor> listTransactions(Option<String> accountOption, int count, long skip, boolean includeWatchOnly) {
    List<WalletTransactionDescriptor> transactions = new ArrayList<WalletTransactionDescriptor>();
    // LIST TRANSACTIOINS...
    transactions.addAll(
      JavaConverters.asJavaCollection(
        wallet.listTransactions(chainView, accountOption, count, skip, includeWatchOnly, chainView.db())
      )
    );
    return transactions;
  }

  @Override
  public List<CoinAddress> getAddressesByAccount(Option<String> accountOption, boolean includeWatchOnly) throws OapException {
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    for (OutputOwnership o : JavaConverters.asJavaCollection(
      wallet.store().getOutputOwnerships(
        accountOption,
        chainView.db()
      )
    )) {
      // PrivateKey does not exists, watch only address
      if (!includeWatchOnly && wallet.store().getPrivateKeys(Option.apply(o), chainView.db()).isEmpty()) {
        continue;
      }
      addresses.add((o instanceof CoinAddress) ? (CoinAddress) o : CoinAddress.from(o.stringKey()));
    }
    return addresses;
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return wallet.getReceivingAddress(account, chainView.db());
  }
}