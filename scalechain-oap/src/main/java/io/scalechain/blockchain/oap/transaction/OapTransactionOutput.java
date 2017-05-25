package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.proto.TransactionOutput;

/**
 * OapTransactionOutput extends scala case class TransacionOutput.
 *
 * FIXME: Remove "bad extends"
 *
 * Created by shannon on 16. 11. 23.
 */
public class OapTransactionOutput {
    AssetId assetId = null;
    int quantity = -1;
    TransactionOutput output;

    public boolean isColored() {
        return assetId != null && quantity >= 0;
    }

    public OapTransactionOutput(TransactionOutput output) {
        this.output = output;
    }

    public OapTransactionOutput(AssetId assetId, int quantity, long value, LockingScript lockingScript) {
        output = new TransactionOutput(value, lockingScript);
        this.assetId = assetId;
        this.quantity = quantity;
    }

    public OapTransactionOutput(AssetId assetId, int quantity, TransactionOutput output) {
        this(assetId, quantity, output.getValue(), output.getLockingScript());
    }

    public AssetId getAssetId() {
        return assetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public TransactionOutput getTransactionOutput() {
        return output;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(OapTransactionOutput.class.getSimpleName()).append('(');
        if (assetId != null) {
            sb.append("assetId=").append(assetId.base58()).append(", ");
            sb.append("quantity=").append(quantity).append(", ");
        }
        sb.append("output=(").append(output.toString()).append(')');
        sb.append(')');
        return sb.toString();
    }
}