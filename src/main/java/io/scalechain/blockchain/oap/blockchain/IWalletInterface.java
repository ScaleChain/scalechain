package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.WalletTransactionDescriptor;
import scala.Option;

import java.util.List;

/**
 * Interface class to wallet APIs.
 *
 * OAP uses listTransactions(), listUnspents()
 *
 * Newly implimented APIs GetAddressByAccount() uses getAddressByAccount().
 *
 * Created by shannon on 16. 11. 21.
 */
public interface IWalletInterface {
  /**
   * return unspent ouputs of given addresses
   * This method is interface for bitcoin rpc listunspent.
   *
   * @param minConfirmations
   * @param maxConfirmations
   * @param addresses
   * @return
   */
  List<UnspentCoinDescriptor> listUnspent(long minConfirmations, long maxConfirmations, List<CoinAddress> addresses);

  List<WalletTransactionDescriptor> listTransactions(Option<String> accountOption, int count, long skip, boolean includeWatchOly);

  List<CoinAddress> getAddressesByAccount(Option<String> accountOption, boolean includeWatchOnly) throws OapException;

  CoinAddress getReceivingAddress(String account) throws OapException;
}
