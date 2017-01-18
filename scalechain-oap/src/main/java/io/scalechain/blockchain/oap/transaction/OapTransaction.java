package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionInput;
import io.scalechain.blockchain.proto.TransactionOutput;
import scala.collection.immutable.List;

/**
 * Created by shannon on 16. 11. 23.
 */
public class OapTransaction extends Transaction {
    OapMarkerOutput markerOutput;
    int markerOutputIndex;
    public OapTransaction(int version, List<TransactionInput> inputs, List<TransactionOutput> outputs, long lockTime) {
        super(version, inputs, outputs, lockTime);
        for(int i = 0;i < outputs.size();i++) {
            TransactionOutput output = outputs.apply(i);
            if (output.value() == 0) {
                markerOutput = (OapMarkerOutput) output;
                markerOutputIndex = i;
            }
        }
    }

    OapMarkerOutput getMarkerOutput() {
        return markerOutput;
    }
    int getMarkerOutputIndex() {
        return markerOutputIndex;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(OapTransaction.class.getSimpleName()).append('(');
        sb.append("super=").append(super.toString());
        return sb.append(')').toString();
    }

}
