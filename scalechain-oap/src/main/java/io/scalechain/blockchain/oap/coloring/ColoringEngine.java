package io.scalechain.blockchain.oap.coloring;

import io.scalechain.blockchain.oap.OapStorage;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.transaction.OapTransaction;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.script.ops.OpReturn;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AddressUtil;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.wallet.UnspentCoinDescriptor;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Takes care of colring of Open Assets Protocol transactions and transaction outpus
 *
 * Created by shannon on 16. 11. 17.
 */
public class ColoringEngine implements  IOapConstants {
    private static ColoringEngine instance;

    public static ColoringEngine get() {
        return instance;
    }

    public static ColoringEngine create() {
        instance = new ColoringEngine();
        return instance;
    }

    private ColoringEngine(){    }

    /**
     * Check if given TX OUTPUT is MARKER OUPUT
     *
     * @param output
     * @return
     */
    public boolean isMarkerOutput(TransactionOutput output) {
        if (output.value() != 0) return false;
        // MarkerOuput LockingScript starts with OP_RETURN
        return ParsedPubKeyScript.from(output.lockingScript()).scriptOps().operations().apply(0) instanceof OpReturn;
    }

    /**
     * Search MARKER OUTPUT and return its index
     * <p>
     * If MARKER OUTPUT is not in given tx, returns -1
     *
     * @param tx
     * @return
     */
    public int getMarkerOutputIndex(Transaction tx) {
        List<TransactionOutput> outputs = tx.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            TransactionOutput output = outputs.apply(i);
            if (isMarkerOutput(output)) {
                return i;
            }
        }
        return -1;
    }

    public AssetId assetIdForIssuance(OapBlockchain chain, Transaction tx) throws OapException {
        if (getMarkerOutputIndex(tx) < 1) throw new OapException(OapException.COLORING_ERROR, "Not OAP Issuance Tx.");
        // AssetId is calculated from address from first input
        TransactionOutput prevTxOut = chain.getRawOutput(
                tx.inputs().apply(0).getOutPoint()
        );
        return AssetId.from(
                AddressUtil.coinAddressFromLockingScript(prevTxOut.lockingScript())
        );
    }

    /**
     * Color issuance output in the given TX and returns all TX outputs.
     * This method also creates MARKER OUTPUT
     *
     * @param chain
     * @param tx
     * @param markerOutputIndex
     * @return
     * @throws OapException
     */
    public java.util.List<TransactionOutput> colorUntilMarkerOutput(
            OapBlockchain chain,
            Transaction tx,
            int markerOutputIndex
    ) throws OapException {
        java.util.List<TransactionOutput> result = new java.util.ArrayList<TransactionOutput>();
        List<TransactionOutput> outputs = tx.outputs();
        if (markerOutputIndex > 0) {
            AssetId assetId = assetIdForIssuance(chain, tx);
            OapMarkerOutput markerOutput = new OapMarkerOutput(tx.outputs().apply(markerOutputIndex),assetId);
            int[] quantities = markerOutput.getQuantities();
            int quntityIndex = 0;
            for (int i = 0; i < markerOutputIndex; i++) {
                TransactionOutput output = outputs.apply(i);
                if (output.value() == DUST_IN_SATOSHI) {
                    OapTransactionOutput coloredOutput = new OapTransactionOutput(
                            assetId,
                            quantities.length == 0 ? 0 : quantities[quntityIndex++],
                            output.value(),
                            output.lockingScript()
                    );
                    result.add(coloredOutput);
                } else {
                    result.add(output);
                }
            }
            result.add(markerOutput);
        } else {
            result.add(new OapMarkerOutput(
                    tx.outputs().apply(markerOutputIndex),
                    null
            ));
        }
        return result;
    }

    /**
     * Assigns Asset Id and Asset Quantity from assetInputs.
     * <p>
     * The first element of return value is Asset Id.
     * The second elemet of return value is Asset Quantity.
     *
     * @param assetInputs
     * @param quantity
     * @return
     */
    public Pair<AssetId, Integer> getNextAssetIdAndQuantity(HashMap<AssetId, Integer> assetInputs, int quantity) {
        for (Map.Entry<AssetId, Integer> entry : assetInputs.entrySet().toArray(new Map.Entry[0])) {
            if (entry.getValue() >= quantity) {
                int remaining = entry.getValue() - quantity;
                assetInputs.put(entry.getKey(), remaining);
                if (remaining == 0) {
                    assetInputs.remove(entry.getKey());
                }
                return new Pair<AssetId, Integer>(entry.getKey(), quantity);
            }
            // DISCARD THE REMNANT
            assetInputs.remove(entry.getKey());
        }
        return new Pair<AssetId, Integer>(null, 0);
    }

    public HashMap<AssetId, Integer> buildAssetQuantitiesMap(List<TransactionInput> inputs) throws OapException {
        HashMap<AssetId, Integer> result = new LinkedHashMap<AssetId, Integer>();
        for (int i = 0; i < inputs.size(); i++) {
            TransactionOutput spendingTxOut = getOutput(inputs.apply(i).getOutPoint());
            if (spendingTxOut.value() == DUST_IN_SATOSHI) {
                // Asset Transfer or Issue
                OapTransactionOutput o = (OapTransactionOutput) spendingTxOut;
                Integer sum = result.get(o.getAssetId());
                if (sum == null) {
                    result.put(o.getAssetId(), o.getQuantity());
                } else {
                    result.put(o.getAssetId(), o.getQuantity() + sum);
                }
            }
        }
        return result;
    }

    /**
     * colors trafer output of given tx.
     * returns OapTransaction if given tx is an Open Asset Protocol transaction,
     *         Transaction if given tx is a normal transaction
     *
     * @param chain
     * @param hash
     * @param tx
     * @param outputs
     * @param markerOutputIndex
     * @return
     * @throws OapException
     */
    private OapTransaction colorTransfer(
            OapBlockchain chain,
            Hash hash,
            Transaction tx,
            java.util.List<TransactionOutput> outputs,
            int markerOutputIndex
    ) throws OapException {
        // 1. Agggrgate Asset Quantities by Asset ID of all inputs.
        HashMap<AssetId, Integer> inputQuantitiesByAssetId = buildAssetQuantitiesMap(tx.inputs());

        int quanitiyCountIndex = 0;
        for(TransactionOutput output : outputs) {
            if (output.value() == DUST_IN_SATOSHI) {
                quanitiyCountIndex++;
            }
        }
        OapMarkerOutput marker = (OapMarkerOutput)outputs.get(markerOutputIndex);
        int[] assetQuantities =  marker.getQuantities();
        for(int i = markerOutputIndex + 1;i < tx.outputs().size();i++) {
            TransactionOutput rawOutput = tx.outputs().apply(i);
            if (rawOutput.value() == DUST_IN_SATOSHI) {
                Pair<AssetId, Integer> pair = getNextAssetIdAndQuantity(inputQuantitiesByAssetId, assetQuantities[quanitiyCountIndex]);
                if (pair.getFirst() == null) {
                    throw new OapException(OapException.COLORING_ERROR, "Cannot assign asset id and asset quantity for index:" + quanitiyCountIndex);
                }
                quanitiyCountIndex++;
                outputs.add(
                        new OapTransactionOutput(pair.getFirst(), pair.getSecond(), rawOutput)
                );
            } else {
                outputs.add(rawOutput);
            }
        }
        if (hash != null) {
            // IF TX HASH IS GIVEN, PUT COLORED OUTPUTS TO CACHE
            // TODO : We can propaget newly colored transaction here.
            for(int i = 0;i < outputs.size();i++) {
                TransactionOutput output = outputs.get(i);
                if (output.value() != DUST_IN_SATOSHI) continue;
                // PUT COLORED OUTPUT TO CACHE
                OapTransactionOutput o = (OapTransactionOutput) output;
                OutPoint key = new OutPoint(hash, i);
                OapStorage.get().putOutput(key, o);
            }
        }
        // CREATE OapTransaction.
        return new OapTransaction(
                tx.version(),
                tx.inputs(),
                JavaConverters.asScalaBuffer(outputs).toList(),
                tx.lockTime()
        );
    }

    /**
     * Colors given tx.
     * This method also put all colored outputs to cache if transaction hash is not null.
     *
     * @param tx
     * @param hash
     * @return
     * @throws OapException
     */
    public Transaction color(Transaction tx, Hash hash) throws OapException {
        OapBlockchain chain = OpenAssetsProtocol.get().chain();
        // CHECK IF tx HAS A MARKER OUTPUT
        int markerOutputIndex = getMarkerOutputIndex(tx);
        // Not a OpenAssetsProtocolImpl Transaction, return tx.
        if (markerOutputIndex < 0) return tx;

        // GET COLORED OUTPUTS FROM CACHE
        if (hash != null) {
            java.util.List<Pair<OutPoint, OapTransactionOutput>> coloredOutputs = OapStorage.get().getOutputs(hash);
            if (coloredOutputs.size() > 0) {
                java.util.List<TransactionOutput> newOutputs = new java.util.ArrayList<TransactionOutput>();
                newOutputs.addAll(JavaConverters.asJavaCollection(tx.outputs()));
                for (Pair<OutPoint, OapTransactionOutput> p : coloredOutputs) {
                    newOutputs.set(p.getFirst().outputIndex(), p.getSecond());
                }
                newOutputs.set(markerOutputIndex,
                        new OapMarkerOutput(
                                tx.outputs().apply(markerOutputIndex),
                                markerOutputIndex > 0 ? ((OapTransactionOutput) newOutputs.get(0)).getAssetId() : null
                        )
                );
                return new OapTransaction(
                        tx.version(), tx.inputs(),
                        JavaConverters.asScalaBuffer(newOutputs).toList(),
                        tx.lockTime()
                );
            }
        }
        java.util.List<TransactionOutput> outputs = colorUntilMarkerOutput(chain, tx, markerOutputIndex);
        return colorTransfer(chain, hash, tx, outputs, markerOutputIndex);
    }

    // GET THE TX WITH GIVEN TX HASH FROM BLOCKCHAIN
    // AND COLOR IT.

    /**
     * gets the transaction of given transaction hash and colors it.
     * If no transaction exists for given transaction hash, returns null.
     *
     * @param hash
     * @return
     * @throws OapException
     */
    public Transaction color(Hash hash) throws OapException {
        OapBlockchain chain = OpenAssetsProtocol.get().chain();
        Transaction tx = chain.getRawTransaction(hash);
        if (tx == null) return null;
        return color(tx, hash);
    }

    /**
     * Colors given transaction.
     *
     * @param tx
     * @return
     * @throws OapException
     */
    // JUST COLORING.
    // WITHOUT TX HASH, WE CANNOT PUT OUTPUTS OF THIS TX TO CACHE
    public Transaction color(Transaction tx) throws OapException {
        return color(tx, null);
    }

    public TransactionOutput getOutput(OutPoint outPoint) throws OapException {
        // If we have Tx Output already colored, return it.
        Option<OapTransactionOutput> item = OapStorage.get().getOutput(outPoint);//cache.get(outPoint);
        if (item.isDefined())
            return item.get();

        // GET RAW TX FROM BLOCKCHAIN
        Transaction tx = OpenAssetsProtocol.get().chain().getRawTransaction(outPoint.transactionHash());
        if (tx == null) throw new OapException(OapException.COLORING_ERROR, "Transaction does not exsist having TX ID=" + outPoint.transactionHash().toHex());

        // NOW WE HAVE AN UNCOLORED TX, LET'S COLOR...
        Transaction coloredTx = color(tx, outPoint.transactionHash());

        return coloredTx.outputs().apply(outPoint.outputIndex());
    }

    /**
     * Colors unspent coin descriptor.
     *
     * @param unspent
     * @return
     * @throws OapException
     */
    public UnspentCoinDescriptor colorUnspentCoinDescriptor(UnspentCoinDescriptor unspent) throws OapException {
        if (UnspentAssetDescriptor.amountToCoinUnit(unspent.amount()) != DUST_IN_SATOSHI) {
            return unspent;
        }
        // CACHED ITEM?
        //  BUILD COLOR THIS UNSPENT.
        Option<OapTransactionOutput> outputOption = OapStorage.get().getOutput(new OutPoint(unspent.txid(), unspent.vout()));// cache.get(new OutPoint(unspent.txid(), unspent.vout()));
        if (outputOption.isDefined()) {
            return new UnspentAssetDescriptor(unspent, outputOption.get().getAssetId(), outputOption.get().getQuantity());
        }

        UnspentAssetDescriptor result = null;
        // COLOR TX containing unspent.
        Transaction tx = color(unspent.txid());
        // PUT ALL COLORED OUTPUT TO CACHE AND COLOR THIS UNSPENT.
        for(int i = 0;i < tx.outputs().size();i++) {
            TransactionOutput output = tx.outputs().apply(i);
            if (output.value() == DUST_IN_SATOSHI) {
                OapTransactionOutput o = (OapTransactionOutput)output;
//                cache.put(new OutPoint(unspent.txid(), i), new OapOutputCacheItem(o, o.getAssetId().base58(), o.getQuantity()));
                OapStorage.get().putOutput(new OutPoint(unspent.txid(), i), o);
                if (unspent.vout() == i) {
                    result = new UnspentAssetDescriptor(unspent, o.getAssetId(), o.getQuantity());
                }
            }
        }
        return result;
    }
}
