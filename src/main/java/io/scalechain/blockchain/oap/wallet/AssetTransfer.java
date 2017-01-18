package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;

/**
 * Created by shannon on 16. 11. 21.
 */
public class AssetTransfer {
    AssetAddress toAddress;
    AssetId assetId;
    int quantity;

    public AssetTransfer(AssetAddress toAddress, AssetId assetId, int quantity) {
        this.toAddress = toAddress;
        this.assetId = assetId;
        this.quantity = quantity;
    }

    public AssetAddress getToAddress() {
        return toAddress;
    }

    public AssetId getAssetId() {
        return assetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public static AssetTransfer create(String toAddressBase58, String assetIdBase58, int quantity) throws OapException {
        AssetAddress toAddress = AssetAddress.from(toAddressBase58);
        AssetId assetId = AssetId.from(assetIdBase58);
        if (quantity < 0) throw new OapException(OapException.INVALID_QUANTITY, "Invalid quantity: " + quantity );
        return new AssetTransfer(toAddress, assetId, quantity);
    }

    public static AssetTransfer from(AssetTransferTo to) throws OapException {
        return create(to.to_address(), to.asset_id(), to.quantity());
    }
}
