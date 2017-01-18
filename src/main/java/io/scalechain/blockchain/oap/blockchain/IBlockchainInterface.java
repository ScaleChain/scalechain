package io.scalechain.blockchain.oap.blockchain;

import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;

/**
 * Inteface to block chain api.
 * OAP uses only getTranstaction() and getTransactionOutput() API.
 *
 * Created by shannon on 16. 11. 29.
 */
public interface IBlockchainInterface
//  extends BlockchainView
{
    /**
     * returns a raw transaction for given hash.
     *
     * @param hash
     * @return
     */
    public Transaction getTransation(Hash hash);

    /**
     * returns a transaction output for given outPoint.
     *
     * @param outPoint
     * @return
     */
    public TransactionOutput getTransactionOutput(OutPoint outPoint);

    /**
     *
     * @return
     */
    public KeyValueDatabase db();
}
