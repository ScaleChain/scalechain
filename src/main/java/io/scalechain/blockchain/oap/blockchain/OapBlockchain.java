package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.blockchain.transaction.CoinsView;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;

/**
 * Created by shannon on 16. 12. 1.
 */
public abstract class OapBlockchain implements IOapConstants, CoinsView {
    public static OapBlockchain create(IBlockchainInterface blockChainView) throws OapException {
        return new OapBlockchainImpl(blockChainView);
    }

    public abstract IBlockchainInterface chainInterface();
    /**
     * query a transaction from blockchain for given Hash "hash".
     *
     * @param key
     * @return
     */
    abstract public Transaction       getRawTransaction(Hash key);

    /**
     * query a transaction output from blockchain for given OutPoin "key".
     *
     * @param key
     * @return
     */
    abstract public TransactionOutput getRawOutput(OutPoint key);

    abstract public KeyValueDatabase db();
}
