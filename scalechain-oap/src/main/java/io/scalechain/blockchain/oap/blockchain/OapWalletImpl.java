package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.coloring.ColoringEngine;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.oap.wallet.OapTransactionDescriptor;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.WalletTransactionDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shannon on 16. 11. 29.
 */
public class OapWalletImpl extends OapWallet implements IOapConstants {
  IWalletInterface walletInterface = null;

  protected OapWalletImpl(IWalletInterface walletInterface) throws OapException {
    this.walletInterface = walletInterface;
  }

  @Override
  public IWalletInterface walletInterface() {
    return walletInterface;
  }

  @Override
  public List<UnspentAssetDescriptor> listUnspent(long minConfirmations, long maxConfirmations, List<CoinAddress> addresses, boolean includeAssets) throws OapException {
    List<UnspentAssetDescriptor> result = new ArrayList<UnspentAssetDescriptor>();
    List<UnspentCoinDescriptor> unspents = walletInterface().listUnspent(minConfirmations, maxConfirmations, addresses);

    for (UnspentCoinDescriptor unspent : unspents) {
      UnspentAssetDescriptor assetDesc = ColoringEngine.get().colorUnspentCoinDescriptor(unspent);

      if (assetDesc.isColored()) {
        if (includeAssets) {
          result.add(assetDesc);
        }
      } else {
        result.add(assetDesc);
      }
    }

    return result;
  }


  /**
   * returns OutPoint of TransactionOutput of this transction descriptor.
   * used to color "send" TransactionDescriptor
   *
   * @param desc
   * @return
   */
  protected OutPoint outPointOfSending(WalletTransactionDescriptor desc) {
    Transaction tx = OpenAssetsProtocol.get().chain().getRawTransaction(desc.getTxid());
    TransactionInput input = tx.getInputs().get( desc.getVout()) ;
    return input.getOutPoint();
  }

  /**
   * create OapTransactionDescriptor from WalletTransactionDescriptor
   *
   * @param ce
   * @param d
   * @return
   * @throws OapException
   */
  protected OapTransactionDescriptor oapTransactionDescriptor(ColoringEngine ce, WalletTransactionDescriptor d) throws OapException {
    if (!d.getAmount().equals(IOapConstants.DUST_IN_BITCOIN)) {
      return OapTransactionDescriptor.from(d);
    } else {
      OutPoint outPoint = null;
      if (d.getCategory().equalsIgnoreCase("send")) {
        outPoint = outPointOfSending(d);
      } else {
        outPoint = new OutPoint(d.getTxid(), d.getVout());
      }

      OapTransactionOutput o = ce.getOapOutput(outPoint);
      return OapTransactionDescriptor.from(d, o);
    }
  }

  /**
   * lists recent transaction of an Account.
   *
   * @param accountOption
   * @param count
   * @param skip
   * @param includeWatchOnly
   * @return
   * @throws OapException
   */
  public List<OapTransactionDescriptor> listTransactionsWithAsset(String accountOption, int count, long skip, boolean includeWatchOnly) throws OapException {
    List<WalletTransactionDescriptor> descs = walletInterface().listTransactions(accountOption, count, skip, includeWatchOnly);

    // Convert WalletTransactionDescriptors to OapTransactionDescriptors
    ColoringEngine ce = ColoringEngine.get();
    List<OapTransactionDescriptor> result = new ArrayList<OapTransactionDescriptor>();
    for (WalletTransactionDescriptor d : descs) {
      result.add(oapTransactionDescriptor(ce, d));
    }
    return result;
  }

  @Override
  public List<CoinAddress> getAddressesByAccount(String accountOption, boolean includeWatchOnly) throws OapException {
    return walletInterface().getAddressesByAccount(accountOption, includeWatchOnly);
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return walletInterface().getReceivingAddress(account);
  }
}
