package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.oap.wallet.OapTransactionDescriptor;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.wallet.UnspentCoinDescriptor;
import scala.Option;

import java.util.List;

/**
 * Created by shannon on 16. 12. 1.
 */
public abstract class OapWallet implements IOapConstants {

    public abstract IWalletInterface walletInterface();
    public  static OapWallet create(IWalletInterface wallet) throws OapException {
        return new OapWalletImpl(wallet);
    }

    /**
     * lists unpent outputs of addresses given in "addresses".
     * Unspent Assets are colored with Asseet Id and Asset Quantity and returned as instance of UnSpentAssetDescriptor.
     * "includeAssets" flag controlls whether Unspent Asset Ouput should be included.
     *
     * @param minConfirmations
     * @param maxConfirmations
     * @param addresses
     * @param includeAssets
     * @return
     * @throws OapException
     */
    public abstract List<UnspentCoinDescriptor> listUnspent(
            long minConfirmations,
            long maxConfirmations,
            List<CoinAddress> addresses,
            boolean includeAssets
    ) throws OapException;

    /**
     * lists recent transaction with colored informations of an Account.
     *
     * @param accountOption
     * @param count
     * @param skip
     * @param includeWatchOnly
     * @return
     * @throws OapException
     */
    public abstract List<OapTransactionDescriptor> listTransactionsWithAsset(Option<String> accountOption, int count, long skip, boolean includeWatchOnly) throws OapException;

    /**
     * return addresses of the given account.
     *
     * @param accountOption
     * @param includeWatchOnly
     * @return
     * @throws OapException
     */
    public abstract List<CoinAddress> getAddressesByAccount(Option<String> accountOption, boolean includeWatchOnly) throws OapException;

    /**
     * returns receving address of the given accouint.
     *
     * @param account
     * @return
     * @throws OapException
     */
    public abstract CoinAddress getReceivingAddress(String account) throws OapException;
}
