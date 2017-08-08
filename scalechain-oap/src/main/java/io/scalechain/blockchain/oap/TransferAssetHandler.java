package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.chain.TransactionBuilder;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.AssetTransfer;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.UnspentCoinDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shannon on 17. 1. 3.
 */
public class TransferAssetHandler implements  IOapConstants {
  private static TransferAssetHandler instance = new TransferAssetHandler();
  public static TransferAssetHandler get() {
    return instance;
  }

  /**
   * Calcuate asset change from input, the list of UnspentAssetDescriptor and transfers, the list of AssetTransfer.
   *
   * If inputs has not enough asset to fullfill transfers OapException is thrown.
   *
   * The return value holds a list of UspentAssetDecriptor to be used as transaction input and asset change.
   *
   * @param assetId
   * @param inputs
   * @param transfers
   * @return
   * @throws OapException
   */
  protected Pair<List<UnspentAssetDescriptor>, Integer> caculateAssetChange(
    AssetId assetId,
    List<UnspentAssetDescriptor> inputs,
    List<AssetTransfer> transfers
  ) throws OapException {
    List<UnspentAssetDescriptor> spending = new ArrayList<UnspentAssetDescriptor>();
    int sum = 0;
    for (AssetTransfer transfer : transfers) {
      sum += transfer.getQuantity();
    }
    if (inputs != null && inputs.size() > 0) {
      for (UnspentAssetDescriptor input : inputs) {
        sum -= input.getQuantity();
        spending.add(input);
        if (sum <= 0) {
          break;
        }
      }
    } else {
      // FIXED
      throw new OapException(OapException.NO_ASSET, "Address has no asset " + assetId.base58());
    }
    if (sum > 0) throw new OapException(OapException.NOT_ENOUGH_ASSET, "Not enough asset " + assetId.base58() + " for transfer");
    int change = Math.abs(sum);

    return new Pair<List<UnspentAssetDescriptor>, Integer>(spending, change);
  }

  /*
   * Split the unspent list into Unspents Coin and Unsptent Assets by AssetId
   *
   */
  public Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> splitUnspent(
    List<UnspentAssetDescriptor> unspentCoinDescriptors
  ) {
    List<UnspentCoinDescriptor> coinDescriptors = new ArrayList<UnspentCoinDescriptor>();
    HashMap<AssetId, List<UnspentAssetDescriptor>> assetDescriptors = new HashMap<AssetId, List<UnspentAssetDescriptor>>();
    for (UnspentAssetDescriptor unspent : unspentCoinDescriptors) {
      if (unspent.isColored()) {
        List<UnspentAssetDescriptor> list = assetDescriptors.get(unspent.getAssetId());
        if (list == null) {
          list = new ArrayList<UnspentAssetDescriptor>();
          assetDescriptors.put(unspent.getAssetId(), list);
        }
        list.add(unspent);
      } else {
        coinDescriptors.add(unspent.getUnspentCoinDescriptor());
      }
    }
    return new Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>>(coinDescriptors, assetDescriptors);
  }

  /**
   * Group transfers, the list of AssetTransferTo by Asset Id
   *
   * @param transfers
   * @return
   * @throws OapException
   */
  protected  HashMap<AssetId, List<AssetTransfer>> groupAssetTransfersByAssetId(List<AssetTransferTo> transfers) throws OapException {
    HashMap<AssetId, List<AssetTransfer>> result = new HashMap<AssetId, List<AssetTransfer>>();
    for (AssetTransferTo transfer : transfers) {
      if (transfer.getQuantity() < 0) {
        throw new OapException(OapException.INVALID_QUANTITY, "Invalid quantity: " + transfer.getQuantity());
      }
      AssetTransfer to = AssetTransfer.from(transfer);
      List<AssetTransfer> list = result.get(to.getAssetId());
      if (list == null) {
        list = new ArrayList<AssetTransfer>();
        result.put(to.getAssetId(), list);
      }
      list.add(to);
    }
    return result;
  }

  /**
   * creates
   * a list of UnspentCoinDescriptor that can used as transaction inputs
   * and a list of AssetTransfer that containing actual asset transfers.
   *
   * @param unspentAssetDescrptors
   * @param transfersByAssetId
   * @param changeAddress
   * @return
   * @throws OapException
   */
  protected Pair<List<UnspentAssetDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer(
    HashMap<AssetId, List<UnspentAssetDescriptor>> unspentAssetDescrptors,
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId,
    AssetAddress changeAddress
  ) throws OapException {
    List<AssetTransfer> actualAssetTransfers = new ArrayList<AssetTransfer>();
    List<UnspentAssetDescriptor> txInputs = new ArrayList<UnspentAssetDescriptor>();
    for (Map.Entry<AssetId, List<AssetTransfer>> entry : transfersByAssetId.entrySet()) {
      AssetId assetId = entry.getKey();
      Pair<List<UnspentAssetDescriptor>, Integer> inuputsAndChage = caculateAssetChange(
        assetId, unspentAssetDescrptors.get(assetId), entry.getValue()
      );
      // WE HAVE UnspentAssetDescriptors at First, and Asset Changes at Second.
      //   ADD all UnspentAssetDescriptors to allInputs.
      //   AND Asset transfers to actualAssetTransfers.
      txInputs.addAll(inuputsAndChage.getFirst());
      for (AssetTransfer transfer : entry.getValue()) {
        actualAssetTransfers.add(transfer);
      }
      //   IF Asset Change exist, ADD it to actualAssetTransfers
      if (inuputsAndChage.getSecond() > 0) {
        actualAssetTransfers.add(new AssetTransfer(changeAddress, assetId, inuputsAndChage.getSecond()));
      }
    }
    return new Pair<List<UnspentAssetDescriptor>, List<AssetTransfer>>(txInputs, actualAssetTransfers);
  }

  /**
   * make
   * a list of UnspentCoindescriptos with all coin and asset descriptors
   * and coin change.
   *
   * This is almost final stage of the asset tranfer.
   *
   * @param coinDescriptors
   * @param assetInputs
   * @param actualTransfers
   * @param fees
   * @param fromAddress
   * @return
   * @throws OapException
   */
  protected Pair<List<UnspentAssetDescriptor>, CoinAmount> allInputsAndCoinChangeForTransfer(
    List<UnspentCoinDescriptor> coinDescriptors,
    List<UnspentAssetDescriptor> assetInputs,
    List<AssetTransfer> actualTransfers,
    CoinAmount fees,
    AssetAddress fromAddress
  ) throws OapException {
    List<UnspentAssetDescriptor> txInputs = new ArrayList<UnspentAssetDescriptor>();

    txInputs.addAll(assetInputs);
    // NOW WE HAVE THE MINIMUM SUM OF COINS NEEDED TO TRANSFER ASSET(S)
    long coinInputAmount = fees.coinUnits() + actualTransfers.size() * DUST_IN_SATOSHI;
    for (UnspentAssetDescriptor descriptor : assetInputs) {
      coinInputAmount -= descriptor.getUnspentCoinDescriptor().getAmount().multiply(ONE_BTC_IN_SATOSHI).longValue();
    }

    // Calculate coin change
    for (UnspentCoinDescriptor descriptor : coinDescriptors) {
      txInputs.add(new UnspentAssetDescriptor( descriptor ) );
      coinInputAmount -= UnspentAssetDescriptor.amountToCoinUnit(descriptor.getAmount());
      if (coinInputAmount <= 0) break;
    }
    if (coinInputAmount > 0) throw new OapException(OapException.NOT_ENOUGH_COIN, "from_address(" + fromAddress.base58() + ") does not have enough coins");
    CoinAmount coinChange = CoinAmount.from(Math.abs(coinInputAmount));
    return new Pair<List<UnspentAssetDescriptor>, CoinAmount>(txInputs, coinChange);
  }

  /**
   * make a asset quantity array from acutal asset transfers.
   * @param actualAssetTransfers
   * @return
   */
  protected int[] toAssetQuanties(List<AssetTransfer> actualAssetTransfers) {
    int[] result = new int[actualAssetTransfers.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = actualAssetTransfers.get(i).getQuantity();
    }
    return result;
  }

  /**
   * build the asset transfer transaction.
   *
   * This is the final stage of creating the transfer asset transaction.
   *
   * @param txInputs
   * @param realTransfers
   * @param assetQuantities
   * @param changeAddress
   * @param coinChange
   * @return
   * @throws OapException
   */
  protected Transaction buildTransferTransacation(
    List<UnspentAssetDescriptor> txInputs,
    List<AssetTransfer> realTransfers,
    int[] assetQuantities,
    AssetAddress changeAddress,
    CoinAmount coinChange
  ) throws OapException {
    TransactionBuilder builder = TransactionBuilder.newBuilder();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    for (UnspentAssetDescriptor descriptor : txInputs) {
      builder.addInput(
        chain.db(),
        chain, new OutPoint(descriptor.getUnspentCoinDescriptor().getTxid(), descriptor.getUnspentCoinDescriptor().getVout())
      );
    }
    // BUILD Marker Output : have empty metadata for transfer transaction.
    OapMarkerOutput markerOutput = new OapMarkerOutput(null, assetQuantities, new byte[0]);
    // MarkerOutput
    builder.addOutput(OapMarkerOutput.stripOpReturnFromLockScript(markerOutput.getTransactionOutput().getLockingScript()));
    // Asset Issue/Transfer Output
    for (AssetTransfer transfer : realTransfers) {
      builder.addOutput(CoinAmount.from(DUST_IN_SATOSHI), new Hash(transfer.getToAddress().coinAddress().getPublicKeyHash()));
    }
    // ADD coin change output
    if (coinChange.coinUnits() > 0) {
      builder.addOutput(coinChange, new Hash(changeAddress.coinAddress().getPublicKeyHash()));
    }
    return builder.build();
  }

  public Transaction createTransferTransaction(
    AssetAddress fromAddress,
    List<AssetTransferTo> transfers,
    AssetAddress changeAddress, long feesInCoinUnit
  ) throws OapException {
    if (feesInCoinUnit < IOapConstants.MIN_FEES_IN_SATOSHI) throw new OapException(OapException.FEES_TOO_SMALL, "Fees are too small");
    CoinAmount fees = CoinAmount.from(feesInCoinUnit);
    List<CoinAddress> fromAddresses = new ArrayList<CoinAddress>();
    fromAddresses.add(fromAddress.coinAddress());

    // GROUP asset transfers by AssetId
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId = groupAssetTransfersByAssetId(transfers);
    // GET Unspent Outputs of fromAddress
    //  and split into coin outputs and asset outputs
    Pair<List<UnspentCoinDescriptor>,HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets = splitUnspent(
      OpenAssetsProtocol.get().wallet().listUnspent(DEFAULT_MIN_CONFIRMATIONS, DEFAULT_MAX_CONFIRMATIONS, fromAddresses,true)
    );
    // GET Asset Inputs and Actual Asset Transfers
    Pair<List<UnspentAssetDescriptor>, List<AssetTransfer>> assetInputsAndActualAssetTransfers = assetInputsAndActualTransfer(
      unspentCoinsAndAssets.getSecond(),
      transfersByAssetId,
      changeAddress
    );

    // GET all inputs(Asset Inputs + Coin Inputs) and Coin Change.
    Pair<List<UnspentAssetDescriptor>, CoinAmount> txInputsAndCoinChange = allInputsAndCoinChangeForTransfer(
      unspentCoinsAndAssets.getFirst(),
      assetInputsAndActualAssetTransfers.getFirst(),
      assetInputsAndActualAssetTransfers.getSecond(),
      fees, fromAddress
    );

    return buildTransferTransacation(
      txInputsAndCoinChange.getFirst(),
      assetInputsAndActualAssetTransfers.getSecond(),
      toAssetQuanties(assetInputsAndActualAssetTransfers.getSecond()),
      changeAddress,
      txInputsAndCoinChange.getSecond()
    );
  }
}
