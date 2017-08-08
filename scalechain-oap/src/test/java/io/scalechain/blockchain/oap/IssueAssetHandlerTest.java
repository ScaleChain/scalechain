package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.transaction.OapTransaction;
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionInput;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.script.ops.OpPush;
import io.scalechain.blockchain.script.ops.ScriptOp;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.UnspentCoinDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shannon on 17. 1. 3.
 */
public class IssueAssetHandlerTest extends TestWithWalletSampleData {
  @Test
  public void inputAndOutputAmountTest() throws OapException {
    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    CoinAddress issuerAddress = addressData[addressData.length - 1].address;
    CoinAmount fees = CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI);
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(issuerAddress);
    List<UnspentAssetDescriptor> unspent = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses,
      false
    );

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, List<CoinAmount>> inputAndOutputAmounts = issueHandler.inputAndOutputAmounts(
      issuerAddress,
      UnspentAssetDescriptor.toOriginalDescriptor(unspent),
      fees
    );
    List<CoinAmount> outputAmounts = inputAndOutputAmounts.getSecond();

    assertTrue("The size of output amounts should be geater than or equal to ", outputAmounts.size() > 2);
    // CHECK output amounts
    long inputSum = 0;
    for(UnspentCoinDescriptor d : inputAndOutputAmounts.getFirst()) {
      inputSum += new CoinAmount(d.getAmount()).coinUnits();
    }
    long outputSum = fees.coinUnits();
    for(CoinAmount amount : outputAmounts) {
      outputSum += amount.coinUnits();
    }
    assertEquals("The sum of  output amounts should be equal to", inputSum, outputSum);

    assertEquals("The amount of first output should be equal to", 600, outputAmounts.get(0).coinUnits());
    assertEquals("The amount of second output(Marker Output) should be equal to", 0, outputAmounts.get(1).coinUnits());
  }

  @Test
  public void inputAndOutputAmountNoEnoughCoinTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("does not have enough coins");

    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    CoinAddress issuerAddress = addressData[addressData.length - 1].address;
    CoinAmount fees = CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI);
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(issuerAddress);
    // GIVE NO UNSPENT COIN. THIS WILL THROW "not enough coin" Exception
    List<UnspentCoinDescriptor> unspent = new ArrayList<UnspentCoinDescriptor>();

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, List<CoinAmount>> inputAndOutputAmounts = issueHandler.inputAndOutputAmounts(
      issuerAddress,
      unspent,
      fees
    );
  }

  @Test
  public void buildTransactionTest() throws OapException {
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    AddressData issuerAddressData = addressData[addressData.length - 1];

    CoinAddress issuerAddress = issuerAddressData.address;
    AssetAddress toAddress    = issuerAddressData.assetAddress;
    AssetId assetId           = issuerAddressData.assetId;
    CoinAmount fees = CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI);

    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(issuerAddress);
    List<UnspentAssetDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses,
      false
    );

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, List<CoinAmount>> inputAndOutputAmounts = issueHandler.inputAndOutputAmounts(
      issuerAddress,
      UnspentAssetDescriptor.toOriginalDescriptor(unspents),
      fees
    );

    // USE Hash160 of metadata[0] as Asset Definition Pointer
    AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;
    Transaction tx = issueHandler.buildTransaction(
      inputAndOutputAmounts.getFirst(), inputAndOutputAmounts.getSecond(),
      toAddress, assetId, 1000, pointer, issuerAddress
    );

    // CHECK OUTPUT AMOUNTS
    List<CoinAmount> outputAmounts = inputAndOutputAmounts.getSecond();
    for(int i = 0;i < outputAmounts.size();i++) {
      assertEquals("Output amounts shoul be euqal to", outputAmounts.get(i).coinUnits(), tx.getOutputs().get(i).getValue());
    }
    // CHEK Marker Output
    assertEquals("Market Output should have value 0", 0, tx.getOutputs().get(1).getValue());

    // CHECK OutPoint OF INPUTS
    long inputSum = 0;
    List<UnspentCoinDescriptor> inputs = inputAndOutputAmounts.getFirst();
    for(int i =0;i < inputs.size();i++) {
      assertEquals("The OutPoint of each Tx input should be equal to spening output",
        new OutPoint(inputs.get(i).getTxid(), inputs.get(i).getVout()),
        tx.getInputs().get(i).getOutPoint()
      );
      inputSum += new CoinAmount(inputs.get(i).getAmount()).coinUnits();
    }
    long outputSum = fees.coinUnits();
    for(int i = 0;i < tx.getOutputs().size();i++) {
      outputSum += tx.getOutputs().get(i).getValue();
    }
    assertEquals("The sum of output amount and fess should be equal to the sum of input amounts", outputSum, inputSum);
  }

  @Test
  public void issueTest() throws Exception {
    int quantity = 10000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    for(String account : provider.accounts()) {
      AddressData[] addressData = provider.addressesOf(account);
      AddressData issuerAddressData = addressData[addressData.length - 1];
      CoinAddress  issuerAddress     = issuerAddressData.address;
      AssetAddress toAddress         = issuerAddressData.assetAddress;
      AssetId      assetId           = issuerAddressData.assetId;
      AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;

      Transaction tx = issueHandler.createIssuanceTransaction(
        issuerAddress,
        toAddress,
        assetId,
        quantity,
        pointer,
        issuerAddress,
        fees
      );
      assertEquals("Size of Tx Ouputs should be equal to 3", 3, tx.getOutputs().size());
      TransactionOutput issueOutput      = tx.getOutputs().get(0);
      TransactionOutput markerOutput     = tx.getOutputs().get(1);
      TransactionOutput coinChangeOutput = tx.getOutputs().get(2);

      OapMarkerOutput marker = new OapMarkerOutput(tx.getOutputs().get(1), assetId);
      assertEquals("Market Output should have value of 0", 0, markerOutput.getValue());
      assertEquals("Asset Quantity Count should be 1", 1, marker.getQuantities().length);
      assertEquals("Asset Quantity should be " + quantity, quantity, marker.getQuantities()[0]);
      assertArrayEquals("Asset Definition pointer should be equal to given pointer", pointer.getPointer(), marker.getMetadata());

      assertEquals("Issue output should have value of DUST", IOapConstants.DUST_IN_SATOSHI, issueOutput.getValue());
      assertTrue("Public key hash of issue output should be that of issuer addresss", checkPublicKeyHash(issueOutput, issuerAddress));
      assertTrue("Public key hash of change output should be that of issuer addresss", checkPublicKeyHash(coinChangeOutput, issuerAddress));

      long outputCoinSum = IOapConstants.DUST_IN_SATOSHI + coinChangeOutput.getValue() + fees;
      assertTrue("sum of input amounts should be equal to Sum of output amount",  sumInputAmount(tx, issuerAddress) == outputCoinSum);

      TransactionOutput spendingOutput = OpenAssetsProtocol.get().chain().getRawOutput(tx.getInputs().get(0).getOutPoint());
      assertTrue("Public key hash of first input should be that of issuer addresss", checkPublicKeyHash(spendingOutput, issuerAddress));
    }
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void issueNotEnoughCoinTest() throws Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage(" does not have enough coins");

    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    AddressData issuerAddressData = addressData[addressData.length - 1];
    CoinAddress  issuerAddress     = issuerAddressData.address;
    AssetAddress toAddress         = issuerAddressData.assetAddress;
    AssetId      assetId           = issuerAddressData.assetId;
    AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;
    // SET FEES 50 BTC, THIS WILL MAKE "not enough coins" EXCEPTION THROWN
    long fees = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 50; // SET FEES 50 BTC

    int quantity = 1000;
    Transaction tx = issueHandler.createIssuanceTransaction(
      issuerAddress, toAddress,
      assetId,
      quantity,
      pointer,
      issuerAddress, fees
    );
//    int quantity = 10000;
//    CoinAddress issuerAddress = CoinAddress.from(coinAddresses[1]);
//    AssetAddress toAddress = AssetAddress.from(assetAddresses[1]);
//
//    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
//    Transaction tx = IssueAssetHandler.get().createIssuanceTransaction(
//      CoinAddress.from(coinAddresses[1]), AssetAddress.from(assetAddresses[1]),
//      AssetId.fromCoinAddress(coinAddresses[1]),
//      quantity,
//      AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, HexUtil.bytes("2345678901234567890123456789012345678901")),
//      CoinAddress.from(coinAddresses[1]),
//      fees
//    );
  }

  @Test
  public void invalidQuantityTest() throws Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid quantity");


    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    AddressData issuerAddressData = addressData[addressData.length - 1];
    CoinAddress  issuerAddress     = issuerAddressData.address;
    AssetAddress toAddress         = issuerAddressData.assetAddress;
    AssetId      assetId           = issuerAddressData.assetId;
    AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int quantity = -1;
    Transaction tx = issueHandler.createIssuanceTransaction(
      issuerAddress, toAddress,
      assetId,
      quantity,
      pointer,
      issuerAddress, fees
    );
  }

  @Test
  public void createIssuanceTransactionfeesToSmallTest() throws Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage("Fees are too small");

    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    AddressData issuerAddressData = addressData[addressData.length - 1];
    CoinAddress  issuerAddress     = issuerAddressData.address;
    AssetAddress toAddress         = issuerAddressData.assetAddress;
    AssetId      assetId           = issuerAddressData.assetId;
    AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;
    long fees = IOapConstants.MIN_FEES_IN_SATOSHI - 1;

    int quantity = 1000;
    Transaction tx = issueHandler.createIssuanceTransaction(
      issuerAddress, toAddress,
      assetId,
      quantity,
      pointer,
      issuerAddress, fees
    );
  }

  @Test
  public void invalidMetadataTest() throws Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid hash value");

    // GET "SENDER" ACCOUNT
    String account = provider.accounts()[1];
    AddressData[] addressData = provider.addressesOf(account);
    AddressData issuerAddressData = addressData[addressData.length - 1];
    CoinAddress  issuerAddress     = issuerAddressData.address;
    AssetAddress toAddress         = issuerAddressData.assetAddress;
    AssetId      assetId           = issuerAddressData.assetId;
    AssetDefinitionPointer pointer = issuerAddressData.assetDefinitionPointer;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int quantity = 1000;

    // DO TEST
    Transaction tx = issueHandler.createIssuanceTransaction(
      issuerAddress, toAddress,
      assetId, quantity,
      // GIVE INVALID HASDH POINTER WHICH SHOULD BE 20 bytes long.
      AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, HexUtil.bytes("12")),
      issuerAddress, fees
    );
  }

  private long sumInputAmount(Transaction tx, CoinAddress fromAddress) throws OapException {
    long sum = 0;
    for (int i = 0; i < tx.getInputs().size(); i++) {
      TransactionInput input = tx.getInputs().get(i);
      TransactionOutput prevTx = OpenAssetsProtocol.get().chain().getRawOutput(input.getOutPoint());
      if (!checkPublicKeyHash(prevTx, fromAddress)) throw new OapException(OapException.INTERNAL_ERROR, "Invalid input: " + input.getOutPoint());
      sum += prevTx.getValue();
    }
    return sum;
  }

  private boolean checkPublicKeyHash(TransactionOutput output, CoinAddress coinAddress) {
    Collection<ScriptOp> c =
      ParsedPubKeyScript.from(output.getLockingScript()).getScriptOps().getOperations();
    for (ScriptOp op : c) {
      if (op instanceof OpPush) {
        byte[] bytes = ((OpPush) op).getInputValue().getValue();
        if (comprareBytes(bytes, coinAddress.getPublicKeyHash().getArray())) return true;
      }
    }
    return false;
  }

  private static boolean comprareBytes(byte[] a1, byte[] a2) {
    if (a1.length != a2.length) return false;
    for (int i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i])
        return false;
    }
    return true;
  }
}
