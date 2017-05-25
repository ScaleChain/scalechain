package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.chain.TransactionBuilder;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.UnspentCoinDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles IssueAsset request.
 *
 * Created by shannon on 17. 1. 3.
 */
public class IssueAssetHandler implements IOapConstants {
  private static IssueAssetHandler instance = new IssueAssetHandler();
  public static IssueAssetHandler get() {
    return instance;
  }

  public Pair<List<UnspentCoinDescriptor>, List<CoinAmount>> inputAndOutputAmounts(
    CoinAddress issuerAddress, List<UnspentCoinDescriptor> unspentCoinDescriptors, CoinAmount fees
  ) throws OapException {
    List<UnspentCoinDescriptor> coinsToBeSpents = new ArrayList<UnspentCoinDescriptor>();
    List<CoinAmount> outputAmounts = new ArrayList<CoinAmount>();
    long amountRequired = fees.coinUnits() + DUST_IN_SATOSHI;   // MINIMUM AMOUNT NEEDED
    long sum = 0;
    for (UnspentCoinDescriptor unspent : unspentCoinDescriptors) {
      // amount() returns BigDecimal value in SATOSHI.
      sum += UnspentAssetDescriptor.amountToCoinUnit(unspent.getAmount());
      coinsToBeSpents.add(unspent);
      if (sum >= amountRequired) {
        break;
      }
    }
    if (sum < amountRequired) {
      throw new OapException(OapException.NOT_ENOUGH_COIN, "from_address(" + issuerAddress.base58() + ") has not enought coins to issue an asset");
    }
    outputAmounts.add(CoinAmount.from(DUST_IN_SATOSHI));  // For Issuance output
    outputAmounts.add(CoinAmount.from(0L));                      // For MarkerOutput
    if (sum - amountRequired > 0) {
      outputAmounts.add(CoinAmount.from(sum - amountRequired));  // For Coin Change
    }
    return new Pair<List<UnspentCoinDescriptor>, List<CoinAmount>>(coinsToBeSpents, outputAmounts);
  }

  /**
   * build the issue asset transaction.
   * issue transaction has 2 or 3 outputs
   *   an issuance output, the marker output and coin change output.
   *
   * unspents has inputs spending.
   * amouts has output amounts
   *
   * @param unspents
   * @param amounts
   * @param toAddress
   * @param assetId
   * @param quantity
   * @param pointer
   * @param changeAddress
   * @return
   * @throws OapException
   */
  protected Transaction buildTransaction(
    List<UnspentCoinDescriptor> unspents, List<CoinAmount> amounts, AssetAddress toAddress, AssetId assetId, int quantity, AssetDefinitionPointer pointer, CoinAddress changeAddress
  ) throws OapException {
    // BUILD transaction using TransactionBuilder
    TransactionBuilder builder = new TransactionBuilder();
    // ADD inputs
    for (UnspentCoinDescriptor unpent : unspents) {
      builder.addInput(
        OpenAssetsProtocol.get().chain().db(),
        OpenAssetsProtocol.get().chain(),
        new OutPoint(unpent.getTxid(), unpent.getVout())

      );
    }

    OapMarkerOutput markerOutput = new OapMarkerOutput(assetId, new int[]{ quantity }, pointer.getPointer());
    for (CoinAmount amount : amounts) {
      if (amount.coinUnits() == 0L) {
        // MarkerOutput :
        builder.addOutput(OapMarkerOutput.stripOpReturnFromLockScript(markerOutput.getTransactionOutput().getLockingScript()));
      } else if (amount.coinUnits() == DUST_IN_SATOSHI) {
        // Asset Issue/Transfer Output
        builder.addOutput(CoinAmount.from(DUST_IN_SATOSHI), new Hash(toAddress.coinAddress().getPublicKeyHash()));
      } else {
        // Coin Transfer Output
        builder.addOutput(amount, new Hash(changeAddress.getPublicKeyHash()));
      }
    }
    return builder.build();

  }

  /**
   * creates the issue asset transaction.
   *
   * the asset defintion processing is done in the net layer RpcSubsystem.
   * simple puts the Pointer value into metadata.
   *
   * @param issuerAddress
   * @param toAddress
   * @param assetId
   * @param quantity
   * @param pointer
   * @param changeAddress
   * @param feesInCoinUnit
   * @return
   * @throws OapException
   */
  public Transaction createIssuanceTransaction(
    CoinAddress issuerAddress,
    AssetAddress toAddress,
    AssetId assetId,
    int quantity,
    AssetDefinitionPointer pointer,
    CoinAddress changeAddress,
    long feesInCoinUnit
  ) throws OapException {
    if (feesInCoinUnit < IOapConstants.MIN_FEES_IN_SATOSHI) throw new OapException(OapException.FEES_TOO_SMALL, "Fees are too small");
    if (quantity < 0) throw new OapException(OapException.INVALID_QUANTITY, "Invalid quantity: " + quantity);

    CoinAmount fees = CoinAmount.from(feesInCoinUnit);
    List<CoinAddress> issuerAddresses = new ArrayList<CoinAddress>();
    issuerAddresses.add(issuerAddress);
    // GET Unspent Outputs of isserAddress
    List<UnspentAssetDescriptor> unspentCoinDescriptors = OpenAssetsProtocol.get().wallet().listUnspent(1, 999999, issuerAddresses, false);
    // Cacluate coin amounts and change
    Pair<List<UnspentCoinDescriptor>, List<CoinAmount>> inputsAndOutputAmounts = inputAndOutputAmounts(issuerAddress, UnspentAssetDescriptor.toOriginalDescriptor(unspentCoinDescriptors), fees);

    return buildTransaction(
      inputsAndOutputAmounts.getFirst(),
      inputsAndOutputAmounts.getSecond(),
      toAddress, assetId, quantity, pointer, changeAddress
    );
  }
}
