package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.wallet.UnspentCoinDescriptor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for hold unspent asset description.
 *
 * Created by shannon on 16. 11. 25.
 */
public class UnspentAssetDescriptor {
  AssetId assetId = null;
  int quantity = -1;
  UnspentCoinDescriptor unspentCoinDescriptor;

  public boolean isColored() {
    return assetId != null && quantity >= 0;
  }

  // TODO : Add a unit test
  public static List<UnspentCoinDescriptor> toOriginalDescriptor(List<UnspentAssetDescriptor> outputs) {
    ArrayList<UnspentCoinDescriptor> originalOutputs = new ArrayList<UnspentCoinDescriptor>();
    for ( UnspentAssetDescriptor o : outputs) {
      originalOutputs.add( o.getUnspentCoinDescriptor() );
    }
    return originalOutputs;
  }

  public UnspentAssetDescriptor(UnspentCoinDescriptor unspentCoinDescriptor) {
    this.unspentCoinDescriptor = unspentCoinDescriptor;
  }

  public UnspentAssetDescriptor(Hash txid,
                                int vout,
                                String addressOption,
                                String accountOption,
                                String scriptPubKey,
                                String redeemScriptOption,
                                BigDecimal amount,
                                long confirmations,
                                boolean spendable,
                                AssetId assetId,
                                int quantity
  ) {
    unspentCoinDescriptor = new UnspentCoinDescriptor(txid, vout, addressOption, accountOption, scriptPubKey, redeemScriptOption, amount, confirmations, spendable);
    this.assetId = assetId;
    this.quantity = quantity;
  }

  public UnspentAssetDescriptor(UnspentCoinDescriptor ucd, AssetId assetId, int quantity) {
    this(ucd.getTxid(),
      ucd.getVout(),
      ucd.getAddress(),
      ucd.getAccount(),
      ucd.getScriptPubKey(),
      ucd.getRedeemScript(),
      ucd.getAmount(),
      ucd.getConfirmations(),
      ucd.getSpendable(),
      assetId,
      quantity
    );
  }

  public AssetId getAssetId() {
    return assetId;
  }

  public int getQuantity() {
    return quantity;
  }

  public static long amountToCoinUnit(BigDecimal amount) {
    return amount.multiply(IOapConstants.ONE_BTC_IN_SATOSHI).longValue();
  }

  public UnspentCoinDescriptor getUnspentCoinDescriptor() {
    return unspentCoinDescriptor;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(UnspentAssetDescriptor.class.getSimpleName());

    sb.append('(');
    sb.append("assetId=").append(assetId == null ? "null" : assetId.base58());
    sb.append(", quantity=").append(quantity);
    sb.append(", super=(").append(unspentCoinDescriptor.toString()).append(')');
    sb.append(')');
    return sb.toString();
  }
}
