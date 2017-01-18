package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.codec.TransactionCodec$;
import io.scalechain.util.HexUtil;

/**
 * Utility class for TransactionCodec.
 * TransactionCodec class is in api layer.
 *
 * Created by shannon on 16. 12. 16.
 */
public class TransactionCodec {
    /**
     * decodes given Tx Hex to a Transaction.
     * Wrapper for scalechain TransactionCodec
     *
     * @param txHex
     * @return
     */
    public static Transaction decode(String txHex) {
        if (txHex == null || txHex.length() == 0) return null;
        byte[] rawTx = HexUtil.bytes(txHex);
        return (Transaction) TransactionCodec$.MODULE$.parse(rawTx);
    }

//    /**
//     * encode given Tx to Hex String.
//     * Wrapper for scalechain TransactionCodec
//     *
//     * @param tx
//     * @return
//     */
//    public static String encode(Transaction tx) {
//        if (tx == null) return null;
//        return HexUtil.hex(TransactionCodec$.MODULE$.serialize(tx), HexUtil.hex$default$2());
//    }
}
