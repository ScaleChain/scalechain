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
import scala.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shannon on 16. 11. 29.
 */
public class

OapWalletImpl extends OapWallet implements IOapConstants {
  IWalletInterface walletInterface = null;

  protected OapWalletImpl(IWalletInterface walletInterface) throws OapException {
    this.walletInterface = walletInterface;
  }

  @Override
  public IWalletInterface walletInterface() {
    return walletInterface;
  }

  @Override
  public List<UnspentCoinDescriptor> listUnspent(long minConfirmations, long maxConfirmations, List<CoinAddress> addresses, boolean includeAssets) throws OapException {
    List<UnspentCoinDescriptor> result = new ArrayList<UnspentCoinDescriptor>();
    List<UnspentCoinDescriptor> unspents = walletInterface().listUnspent(minConfirmations, maxConfirmations, addresses);
    // CONVERT a Scala List to a Java List
    if (includeAssets) {
      for (UnspentCoinDescriptor unspent : unspents) {
        if (UnspentAssetDescriptor.amountToCoinUnit(unspent.amount()) == DUST_IN_SATOSHI) {
          result.add(ColoringEngine.get().colorUnspentCoinDescriptor(unspent));
        } else {
          result.add(unspent);
        }
      }
    } else {
      // EXCLUDE assets
      for (UnspentCoinDescriptor unspent : unspents) {
        long amount = unspent.amount().bigDecimal().multiply(ONE_BTC_IN_SATOSHI).longValue();
        if (amount != DUST_IN_SATOSHI && amount != 0) {
          result.add(unspent);
        }
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
    Transaction tx = OpenAssetsProtocol.get().chain().getRawTransaction(desc.txid().get());
    TransactionInput input = tx.inputs().apply( (Integer)(desc.vout().get()) );
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
    if (!d.amount().bigDecimal().equals(IOapConstants.DUST_IN_BITCOIN)) {
      return OapTransactionDescriptor.from(d);
    } else {
      OutPoint outPoint = null;
      if (d.category().equalsIgnoreCase("send")) {
        outPoint = outPointOfSending(d);
      } else {
        outPoint = OutPoint.apply(d.txid().get(), (Integer) (d.vout().get()));
      }

      TransactionOutput o = ce.getOutput(outPoint);
      if (o instanceof OapTransactionOutput) {
        return OapTransactionDescriptor.from(d, (OapTransactionOutput) o);
      } else {
        throw new OapException(OapException.INTERNAL_ERROR, "Not an OpenAssetsProtocol output:" + outPoint);
      }
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
  public List<OapTransactionDescriptor> listTransactionsWithAsset(Option<String> accountOption, int count, long skip, boolean includeWatchOnly) throws OapException {
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
  public List<CoinAddress> getAddressesByAccount(Option<String> accountOption, boolean includeWatchOnly) throws OapException {
    return walletInterface().getAddressesByAccount(accountOption, includeWatchOnly);
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return walletInterface().getReceivingAddress(account);
  }
}
