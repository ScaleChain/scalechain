package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;

/**
 * Created by shannon on 16. 11. 24.
 */
public class OapBlockchainImpl extends OapBlockchain {
    private IBlockchainInterface chainInterface;

    /**
     * Creates Blockchain interface.
     *
     * @throws OapException
     */
    protected OapBlockchainImpl(IBlockchainInterface blockchain) throws OapException {
        this.chainInterface = blockchain;
    }

    @Override
    public TransactionOutput getRawOutput(OutPoint key) {
        return getTransactionOutput(db(), key);
    }

    @Override
    public IBlockchainInterface chainInterface() {
        return chainInterface;
    }

    @Override
    public Transaction getRawTransaction(Hash key) {
        return chainInterface().getTransation(key);
    }

    //
    @Override
    public KeyValueDatabase db() {
        return chainInterface().db();
    }


    // FOR trait CoinsView
    @Override
    public TransactionOutput getTransactionOutput(KeyValueDatabase db, OutPoint outPoint) {
        return chainInterface().getTransactionOutput(outPoint);
    }
}
