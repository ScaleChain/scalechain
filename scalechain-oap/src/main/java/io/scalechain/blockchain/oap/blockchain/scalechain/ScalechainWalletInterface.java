package io.scalechain.blockchain.oap.blockchain.scalechain;

import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.OutputOwnership;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import io.scalechain.wallet.WalletTransactionDescriptor;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by shannon on 16. 11. 21.
 */
public class ScalechainWalletInterface implements IWalletInterface {
  Blockchain chain;
  Wallet wallet;
  public ScalechainWalletInterface(Blockchain chain, Wallet wallet) {
    this.chain = chain;
    this.wallet = wallet;
  }

  @Override
  public List<UnspentCoinDescriptor> listUnspent(long minConfirmations, long maxConfirmations, List<CoinAddress> addresses) {
    List<CoinAddress> coinAddressesOption =
      (addresses != null && addresses.size() > 0) ? addresses : null;

    List<UnspentCoinDescriptor> descriptors = wallet.listUnspent(
      chain.getDb(),
      chain,
      minConfirmations,
      maxConfirmations,
      coinAddressesOption
    );
    return descriptors;
  }

  @Override
  public List<WalletTransactionDescriptor> listTransactions(String accountOption, int count, long skip, boolean includeWatchOnly) {
    return wallet.listTransactions(chain.getDb(), chain, accountOption, count, skip, includeWatchOnly);
  }

  @Override
  public List<CoinAddress> getAddressesByAccount(String accountOption, boolean includeWatchOnly) throws OapException {
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    for (OutputOwnership o :
      wallet.getStore().getOutputOwnerships(
        OpenAssetsProtocol.get().chain().db(),
        accountOption
      )
    ) {
      // PrivateKey does not exists, watch only address
      if (!includeWatchOnly && wallet.getStore().getPrivateKeys(chain.getDb(), o).isEmpty()) {
        continue;
      }
      // BUGBUG : OAP : CoinAddress.from(o.stringKey()) does not work.
      addresses.add((o instanceof CoinAddress) ? (CoinAddress) o : CoinAddress.from(o.stringKey()));
    }
    return addresses;
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return wallet.getReceivingAddress(chain.getDb(), account);
  }
}