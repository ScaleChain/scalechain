package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.wallet.UnspentCoinDescriptor;
import scala.Option;
import scala.math.BigDecimal;

/**
 * Class for hold unspent asset description.
 *   extends scala case class UnspentCoinDescriptor
 *
 * FIXME: Remove "bad extends".
 *
 * Created by shannon on 16. 11. 25.
 */
public class UnspentAssetDescriptor extends UnspentCoinDescriptor {
  AssetId assetId;
  int quantity;

  public UnspentAssetDescriptor(Hash txid,
                                int vout,
                                Option<String> address,
                                Option<String> account,
                                String scriptPubKey,
                                Option<String> redeemScript,
                                BigDecimal amount,
                                long confirmations,
                                boolean spendable,
                                AssetId assetId,
                                int quantity
  ) {
    super(txid, vout, address, account, scriptPubKey, redeemScript, amount, confirmations, spendable);
    this.assetId = assetId;
    this.quantity = quantity;
  }

  public UnspentAssetDescriptor(UnspentCoinDescriptor ucd, AssetId assetId, int quantity) {
    this(ucd.txid(),
      ucd.vout(),
      ucd.address(),
      ucd.account(),
      ucd.scriptPubKey(),
      ucd.redeemScript(),
      ucd.amount(),
      ucd.confirmations(),
      ucd.spendable(),
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
    return amount.bigDecimal().multiply(IOapConstants.ONE_BTC_IN_SATOSHI).longValue();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(UnspentAssetDescriptor.class.getSimpleName()).append('(');
    sb.append("assetId=").append(assetId.base58());
    sb.append(", quantity=").append(quantity);
    sb.append(", super=(").append(super.toString()).append(')');
    return sb.append(')').toString();
  }
}
