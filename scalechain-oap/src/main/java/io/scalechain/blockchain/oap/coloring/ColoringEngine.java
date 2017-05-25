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
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AddressUtil;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.wallet.UnspentCoinDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

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
        if (output.getValue() != 0) return false;
        // MarkerOuput LockingScript starts with OP_RETURN
        return ParsedPubKeyScript.from(output.getLockingScript()).getScriptOps().getOperations().get(0) instanceof OpReturn;
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
        List<TransactionOutput> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            TransactionOutput output = outputs.get(i);
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
                tx.getInputs().get(0).getOutPoint()
        );
        return AssetId.from(
                AddressUtil.coinAddressFromLockingScript(prevTxOut.getLockingScript())
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
    public List<OapTransactionOutput> colorUntilMarkerOutput(
            OapBlockchain chain,
            Transaction tx,
            int markerOutputIndex
    ) throws OapException {
        List<OapTransactionOutput> result = new java.util.ArrayList<OapTransactionOutput>();
        List<TransactionOutput> outputs = tx.getOutputs();
        if (markerOutputIndex > 0) {
            AssetId assetId = assetIdForIssuance(chain, tx);
            OapMarkerOutput markerOutput = new OapMarkerOutput(tx.getOutputs().get(markerOutputIndex),assetId);
            int[] quantities = markerOutput.getQuantities();
            int quntityIndex = 0;
            for (int i = 0; i < markerOutputIndex; i++) {
                TransactionOutput output = outputs.get(i);
                if (output.getValue() == DUST_IN_SATOSHI) {
                    OapTransactionOutput coloredOutput = new OapTransactionOutput(
                            assetId,
                            quantities.length == 0 ? 0 : quantities[quntityIndex++],
                            output.getValue(),
                            output.getLockingScript()
                    );
                    result.add(coloredOutput);
                } else {
                    result.add(new OapTransactionOutput(output));
                }
            }
            result.add(markerOutput);
        } else {
            result.add(new OapMarkerOutput(
                    tx.getOutputs().get(markerOutputIndex),
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
            OapTransactionOutput spendingTxOut = getOapOutput(inputs.get(i).getOutPoint());
            if (spendingTxOut.getTransactionOutput().getValue() == DUST_IN_SATOSHI) {
                // Asset Transfer or Issue
                OapTransactionOutput o = spendingTxOut;
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
            List<OapTransactionOutput> outputs,
            int markerOutputIndex
    ) throws OapException {
        // 1. Agggrgate Asset Quantities by Asset ID of all inputs.
        HashMap<AssetId, Integer> inputQuantitiesByAssetId = buildAssetQuantitiesMap(tx.getInputs());

        int quanitiyCountIndex = 0;
        for(OapTransactionOutput output : outputs) {
            if (output.getTransactionOutput().getValue() == DUST_IN_SATOSHI) {
                quanitiyCountIndex++;
            }
        }
        OapMarkerOutput marker = (OapMarkerOutput)outputs.get(markerOutputIndex);
        int[] assetQuantities =  marker.getQuantities();
        for(int i = markerOutputIndex + 1;i < tx.getOutputs().size();i++) {
            TransactionOutput rawOutput = tx.getOutputs().get(i);
            if (rawOutput.getValue() == DUST_IN_SATOSHI) {
                Pair<AssetId, Integer> pair = getNextAssetIdAndQuantity(inputQuantitiesByAssetId, assetQuantities[quanitiyCountIndex]);
                if (pair.getFirst() == null) {
                    throw new OapException(OapException.COLORING_ERROR, "Cannot assign asset id and asset quantity for index:" + quanitiyCountIndex);
                }
                quanitiyCountIndex++;
                outputs.add(
                        new OapTransactionOutput(pair.getFirst(), pair.getSecond(), rawOutput)
                );
            } else {
                outputs.add(new OapTransactionOutput(rawOutput) );
            }
        }
        if (hash != null) {
            // IF TX HASH IS GIVEN, PUT COLORED OUTPUTS TO CACHE
            // TODO : We can propaget newly colored transaction here.
            for(int i = 0;i < outputs.size();i++) {
                OapTransactionOutput output = outputs.get(i);
                if (output.getTransactionOutput().getValue() != DUST_IN_SATOSHI) continue;
                // PUT COLORED OUTPUT TO CACHE
                OutPoint key = new OutPoint(hash, i);
                OapStorage.get().putOutput(key, output);
            }
        }
        // CREATE OapTransaction.
        return new OapTransaction(
                tx.getVersion(),
                tx.getInputs(),
                outputs,
                tx.getLockTime()
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
    public OapTransaction color(Transaction tx, Hash hash) throws OapException {
        OapBlockchain chain = OpenAssetsProtocol.get().chain();
        // CHECK IF tx HAS A MARKER OUTPUT
        int markerOutputIndex = getMarkerOutputIndex(tx);
        // Not a OpenAssetsProtocolImpl Transaction, return tx.
        if (markerOutputIndex < 0) return new OapTransaction(tx);

        // GET COLORED OUTPUTS FROM CACHE
        if (hash != null) {
            List<Pair<OutPoint, OapTransactionOutput>> coloredOutputs = OapStorage.get().getOutputs(hash);
            if (coloredOutputs.size() > 0) {
                java.util.List<OapTransactionOutput> newOutputs = new java.util.ArrayList<OapTransactionOutput>();
                newOutputs.addAll( OapTransaction.toOapOutput( tx.getOutputs() ) );
                for (Pair<OutPoint, OapTransactionOutput> p : coloredOutputs) {
                    newOutputs.set(p.getFirst().getOutputIndex(), p.getSecond());
                }
                newOutputs.set(markerOutputIndex,
                        new OapMarkerOutput(
                                tx.getOutputs().get(markerOutputIndex),
                                markerOutputIndex > 0 ? (newOutputs.get(0)).getAssetId() : null
                        )
                );
                return new OapTransaction(
                        tx.getVersion(),
                        tx.getInputs(),
                        newOutputs,
                        tx.getLockTime()
                );
            }
        }
        List<OapTransactionOutput> outputs = colorUntilMarkerOutput(chain, tx, markerOutputIndex);
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
    public OapTransaction color(Hash hash) throws OapException {
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
    public OapTransaction color(Transaction tx) throws OapException {
        return color(tx, null);
    }

    public OapTransactionOutput getOapOutput(OutPoint outPoint) throws OapException {
        // If we have Tx Output already colored, return it.
        OapTransactionOutput item = OapStorage.get().getOutput(outPoint);//cache.get(outPoint);
        if (item != null)
            return item;

        // GET RAW TX FROM BLOCKCHAIN
        Transaction tx = OpenAssetsProtocol.get().chain().getRawTransaction(outPoint.getTransactionHash());
        if (tx == null) throw new OapException(OapException.COLORING_ERROR, "Transaction does not exsist having TX ID=" + outPoint.getTransactionHash().toHex());

        // NOW WE HAVE AN UNCOLORED TX, LET'S COLOR...
        OapTransaction oapTx = color(tx, outPoint.getTransactionHash());

        if (oapTx.isColored()) {
            return oapTx.getOapOutputs().get(outPoint.getOutputIndex());
        } else {
            return new OapTransactionOutput(oapTx.getTransaction().getOutputs().get(outPoint.getOutputIndex()));
        }
    }

    /**
     * Colors unspent coin descriptor.
     *
     * @param unspent
     * @return
     * @throws OapException
     */
    public UnspentAssetDescriptor colorUnspentCoinDescriptor(UnspentCoinDescriptor unspent) throws OapException {
        if (UnspentAssetDescriptor.amountToCoinUnit(unspent.getAmount()) != DUST_IN_SATOSHI) {
            return new UnspentAssetDescriptor(unspent);
        }
        // CACHED ITEM?
        //  BUILD COLOR THIS UNSPENT.
        OapTransactionOutput outputOption = OapStorage.get().getOutput(new OutPoint(unspent.getTxid(), unspent.getVout()));// cache.get(new OutPoint(unspent.txid(), unspent.vout()));
        if (outputOption != null) {
            return new UnspentAssetDescriptor(unspent, outputOption.getAssetId(), outputOption.getQuantity());
        }

        UnspentAssetDescriptor result = null;
        // COLOR TX containing unspent.
        OapTransaction tx = color(unspent.getTxid());
        // PUT ALL COLORED OUTPUT TO CACHE AND COLOR THIS UNSPENT.
        for(int i = 0;i < tx.getOapOutputs().size();i++) {
            OapTransactionOutput output = tx.getOapOutputs().get(i);
            if (output.getTransactionOutput().getValue() == DUST_IN_SATOSHI) {
                OapTransactionOutput o = (OapTransactionOutput)output;
//                cache.put(new OutPoint(unspent.txid(), i), new OapOutputCacheItem(o, o.getAssetId().base58(), o.getQuantity()));
                OapStorage.get().putOutput(new OutPoint(unspent.getTxid(), i), o);
                if (unspent.getVout() == i) {
                    result = new UnspentAssetDescriptor(unspent, o.getAssetId(), o.getQuantity());
                }
            }
        }
        return result;
    }
}
