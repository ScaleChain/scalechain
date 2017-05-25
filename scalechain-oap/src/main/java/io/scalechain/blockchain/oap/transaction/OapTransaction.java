package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionInput;
import io.scalechain.blockchain.proto.TransactionOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shannon on 16. 11. 23.
 */
public class OapTransaction {
    OapMarkerOutput markerOutput = null;
    int markerOutputIndex = -1;
    Transaction transaction = null;
    List<OapTransactionOutput> oapOutputs = null;

    public boolean isColored() {
        return markerOutput != null && markerOutputIndex >= 0;
    }

    public OapTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    // TODO : Add a unit test
    public static List<TransactionOutput> toOriginalOutput(List<OapTransactionOutput> outputs) {
        ArrayList<TransactionOutput> originalOutputs = new ArrayList<TransactionOutput>();
        for ( OapTransactionOutput o : outputs) {
            originalOutputs.add( o.getTransactionOutput() );
        }
        return originalOutputs;
    }

    // TODO : Add a unit test
    public static List<OapTransactionOutput> toOapOutput(List<TransactionOutput> outputs) {
        ArrayList<OapTransactionOutput> originalOutputs = new ArrayList<OapTransactionOutput>();
        for ( TransactionOutput o : outputs) {
            originalOutputs.add( new OapTransactionOutput( o ) );
        }
        return originalOutputs;
    }

    public OapTransaction(int version, List<TransactionInput> inputs, List<OapTransactionOutput> outputs, long lockTime) {

        transaction = new Transaction(version, inputs, toOriginalOutput(outputs), lockTime);
        oapOutputs = outputs;
        for(int i = 0;i < outputs.size();i++) {
            OapTransactionOutput output = outputs.get(i);

            // TODO : get rid of isinstanceof - it is a bad sign that a Java code uses isinstanceof
            if (output instanceof OapMarkerOutput) {
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

    public List<OapTransactionOutput> getOapOutputs() {
        assert(oapOutputs != null);
        return oapOutputs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(OapTransaction.class.getSimpleName()).append('(');
        sb.append("transaction=").append(transaction.toString());
        return sb.append(')').toString();
    }

    public Transaction getTransaction() {
        return transaction;
    }

}
