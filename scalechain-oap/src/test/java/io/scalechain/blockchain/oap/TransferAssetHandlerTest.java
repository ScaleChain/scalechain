package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.blockchain.OapWallet;
import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.AssetTransfer;
import io.scalechain.blockchain.oap.wallet.UnspentAssetDescriptor;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.script.ops.OpPush;
import io.scalechain.blockchain.script.ops.ScriptOp;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.wallet.UnspentCoinDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shannon on 17. 1. 3.
 */
public class TransferAssetHandlerTest extends TestWithWalletSampleData {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  OapWallet wallet;
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    wallet = OpenAssetsProtocol.get().wallet();
  }
  @Test
  public void splitUnspentTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;
    AssetId[] assetIds = new AssetId[senderAddresses.length];
    for (int i = 0; i < assetIds.length; i++) {
      assetIds[i] = senderAddresses[i].assetId;
    }
    AssetId assetIdToTransfer = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

    List<CoinAddress> fromAddresses = new ArrayList<CoinAddress>();
    fromAddresses.add(fromAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      fromAddresses, true
    );

    // DO TEST.
    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets
      = transferHandler.splitUnspent(unspents);
    assertNotNull("unspentCoinsAssets should be not null", unspentCoinsAndAssets);
    assertTrue("Unspent Coins should exists", unspentCoinsAndAssets.getFirst().size() > 0);
    assertTrue("At least 2 assets should exists", unspentCoinsAndAssets.getSecond().size() > 0);

    int unspentCount = unspentCoinsAndAssets.getFirst().size();
    // EACH UnspentAssetDescriptors SHOULD HAVE 2 UnspentAssetDescriptor
    HashMap<AssetId, List<UnspentAssetDescriptor>> assets = unspentCoinsAndAssets.getSecond();
    for (AssetId assetId : assetIds) {
      List<UnspentAssetDescriptor> unspentAssets = assets.get(AssetId.from(assetId.base58()));
      unspentCount += unspentAssets.size();
      for (UnspentAssetDescriptor d : unspentAssets) {
        assertTrue("The Asset Id of each UnspentAssetDescriptor should equal to key", d.getAssetId().equals(assetId));
      }
    }
    assertEquals("The count of unspent ouput should be math", unspentCount, unspents.size());
  }

  @Test
  public void assetInputsAndActualTransferTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;
    AssetId[] assetIds = new AssetId[senderAddresses.length];
    for (int i = 0; i < assetIds.length; i++) {
      assetIds[i] = senderAddresses[i].assetId;
    }
    AssetId assetIdToTransfer = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

    List<CoinAddress> fromAddresses = new ArrayList<CoinAddress>();
    fromAddresses.add(fromAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      fromAddresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND 1/2 OF FIRST ASSET
    // THIS WILL PRODUCE 4 TRANSFER OUTPUTS.
    AssetId assetId = null;
    int quantity = 0;
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    for (UnspentCoinDescriptor desc : unspents) {
      if (CoinAmount.apply(desc.amount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity() / 2));
        assetId = ad.getAssetId();
        quantity = ad.getQuantity() / 2;
        break;
      }
    }
    assertNotNull("UnspentCoinDescriptor should have an UnspentAssetDescriptor", assetId);
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId
      = transferHandler.groupAssetTransfersByAssetId(transfers);


    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets
      = transferHandler.splitUnspent(unspents);

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer = transferHandler.assetInputsAndActualTransfer(
      unspentCoinsAndAssets.getSecond(),
      transfersByAssetId,
      AssetAddress.fromCoinAddress(fromAddress)
    );

    // CHECK OUTPUT QUANTITIES.
    List<UnspentCoinDescriptor> spending = assetInputsAndActualTransfer.getFirst();
    List<AssetTransfer> actualTransfers = assetInputsAndActualTransfer.getSecond();
    assertEquals("The size of actualTransfers should be", 2, actualTransfers.size());


    long spendingSum = 0;
    for (UnspentCoinDescriptor d : spending) {
      UnspentAssetDescriptor input = (UnspentAssetDescriptor) d;
      if (input.getAssetId().equals(assetId)) {
        spendingSum += input.getQuantity();
      }
    }
    long transferSum = 0;
    for (AssetTransfer transfer : actualTransfers) {
      transferSum += transfer.getQuantity();
    }
    assertEquals("The transfer quantity should be equal to ", quantity, actualTransfers.get(0).getQuantity());
    // THIS CAN BE TRUE, BECAUSE WE HAVE ONLY 1 UNSPENT ASSET DESCRIPTOR FOR AN ASSET.
    assertEquals("The quantiy of spending quantity equal to", spendingSum, transferSum);
  }

  @Test
  public void assetInputsAndActualTransferNoChangeTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;
    AssetId[] assetIds = new AssetId[senderAddresses.length];
    for (int i = 0; i < assetIds.length; i++) {
      assetIds[i] = senderAddresses[i].assetId;
    }
    AssetId assetIdToTransfer = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

    List<CoinAddress> fromAddresses = new ArrayList<CoinAddress>();
    fromAddresses.add(fromAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      fromAddresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND ALL FOR EACH ASSET.
    // THIS WILL PRODUCE 2 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    for (UnspentCoinDescriptor desc : unspents) {
      if (CoinAmount.apply(desc.amount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity()));
      }
    }
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId
      = transferHandler.groupAssetTransfersByAssetId(transfers);


    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets
      = transferHandler.splitUnspent(unspents);

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer = transferHandler.assetInputsAndActualTransfer(
      unspentCoinsAndAssets.getSecond(),
      transfersByAssetId,
      AssetAddress.fromCoinAddress(fromAddress)
    );

    // CHECK OUTPUT QUANTITIES.
    List<UnspentCoinDescriptor> spending = assetInputsAndActualTransfer.getFirst();
    List<AssetTransfer> actualTransfers = assetInputsAndActualTransfer.getSecond();
    assertEquals("The size of actualTransfers should be", 2, actualTransfers.size());
    for (AssetTransfer t : actualTransfers) {
      long sum = 0;
      for (UnspentCoinDescriptor d : spending) {
        UnspentAssetDescriptor input = (UnspentAssetDescriptor) d;
        if (input.getAssetId().equals(t.getAssetId())) {
          sum += input.getQuantity();
        }
        ;
      }
      assertEquals("The quantiy of tranfer output should be equal to", sum, t.getQuantity());
    }
  }

  @Test
  public void allInputsAndCoinChangeForTransferTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    AssetId[] assetIds = new AssetId[senderAddresses.length];

    CoinAddress fromCoinAddress = senderRecvadddress.address;
    AssetAddress fromAddress = senderRecvadddress.assetAddress;
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(fromCoinAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND 1/2 FOR EACH ASSET.
    // THIS WILL PRODUCE 4 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    for (UnspentCoinDescriptor desc : unspents) {
      if (CoinAmount.apply(desc.amount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity() / 2));
      }
    }
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId
      = transferHandler.groupAssetTransfersByAssetId(transfers);

    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets
      = TransferAssetHandler.get().splitUnspent(unspents);

    Pair<List<UnspentCoinDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer = TransferAssetHandler.get().assetInputsAndActualTransfer(
      unspentCoinsAndAssets.getSecond(),
      transfersByAssetId,
      changeAddress
    );
    CoinAmount fees = CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI);

    // DO TEST.
    Pair<List<UnspentCoinDescriptor>, CoinAmount> allInputsAndCoinChange = TransferAssetHandler.get().allInputsAndCoinChangeForTransfer(
      unspentCoinsAndAssets.getFirst(),
      assetInputsAndActualTransfer.getFirst(),
      assetInputsAndActualTransfer.getSecond(),
      fees,
      fromAddress
    );

    // CHECK COIN INPUT AND CHANGE
    long inputSum = 0;
    for (UnspentCoinDescriptor d : allInputsAndCoinChange.getFirst()) {
      inputSum += CoinAmount.apply(d.amount()).coinUnits();
    }
    long outputSum = fees.coinUnits() +allInputsAndCoinChange.getSecond().coinUnits() + assetInputsAndActualTransfer.getSecond().size() * IOapConstants.DUST_IN_SATOSHI;
    assertEquals(
      "The sum of input amount should be equal to sum of output amount + fee",
      outputSum,
      inputSum
    );
  }


  @Test
  public void allInputsAndCoinChangeForTransferNotEnoughCoinTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("has not enought coins");

    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    AssetId[] assetIds = new AssetId[senderAddresses.length];

    CoinAddress fromCoinAddress = senderRecvadddress.address;
    AssetAddress fromAddress = senderRecvadddress.assetAddress;
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(fromCoinAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND 1/2 FOR EACH ASSET.
    // THIS WILL PRODUCE 4 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    for (UnspentCoinDescriptor desc : unspents) {
      if (CoinAmount.apply(desc.amount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity() / 2));
      }
    }
    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId
      = transferHandler.groupAssetTransfersByAssetId(transfers);

    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescriptor>>> unspentCoinsAndAssets
      = TransferAssetHandler.get().splitUnspent(unspents);

    Pair<List<UnspentCoinDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer = TransferAssetHandler.get().assetInputsAndActualTransfer(
      unspentCoinsAndAssets.getSecond(),
      transfersByAssetId,
      changeAddress
    );
    CoinAmount fees = CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI);

    // REMOVE ALL UNSPENT COINS.THIS WILL PRODUCE "not enough coin" EXCEPTION
    List<UnspentCoinDescriptor> inputs = unspentCoinsAndAssets.getFirst();
    for (int i = inputs.size() - 1; i >= 0; i--) {
      if (!(inputs.get(i) instanceof UnspentAssetDescriptor)) {
        inputs.remove(i);
        i--;
      }
    }

    // DO TEST.
    Pair<List<UnspentCoinDescriptor>, CoinAmount> allInputsAndCoinChange = TransferAssetHandler.get().allInputsAndCoinChangeForTransfer(
      unspentCoinsAndAssets.getFirst(),
      assetInputsAndActualTransfer.getFirst(),
      assetInputsAndActualTransfer.getSecond(),
      fees,
      fromAddress
    );
  }


  @Test
  public void buildTransferTransacationTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;
    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    AssetAddress changeAddress = senderRecvadddress.assetAddress;

//    // 1200 assets for each asset
//    // THIS WILL PRODUCE 4 AssetTranfer OUTPUTS
//    // 1 FOR TRANSFER AND 1 FOR CHANGE FOR EACH.
//    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
//    for (int i = 0; i < assetIds.length; i++) {
//      transfers.add(AssetTransferTo.apply(assetAddresses[1], assetIds[i], 1200));
//    }
//    HashMap<AssetId, List<AssetTransfer>> transfersByAssetId
//      = TransferAssetHandler.get().groupAssetTransfersByAssetId(transfers);
//
//    List<UnspentCoinDescriptor> unspents = listUnspent(fromAddress);
//    Pair<List<UnspentCoinDescriptor>, HashMap<AssetId, List<UnspentAssetDescrptor>>> unspentCoinsAndAssets
//      = TransferAssetHandler.get().splitUnspent(unspents);
//
//    Pair<List<UnspentCoinDescriptor>, List<AssetTransfer>> assetInputsAndActualTransfer = TransferAssetHandler.get().assetInputsAndActualTransfer(
//      unspentCoinsAndAssets.getSecond(),
//      transfersByAssetId,
//      assetAddress
//    );
//    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
//
//    // DO TEST.
//
//
//    Pair<List<UnspentCoinDescriptor>, Long> allInputsAndCoinChange = TransferAssetHandler.get().allInputsAndCoinChangeForTransfer(
//      unspentCoinsAndAssets.getFirst(),
//      assetInputsAndActualTransfer.getFirst(),
//      assetInputsAndActualTransfer.getSecond(),
//      fees,
//      assetAddress
//    );
//
//    // DO TEST
//    Transaction tx = wallethandler.buildTransferTransacation(
//      allInputsAndCoinChange.getFirst(),
//      assetInputsAndActualTransfer.getSecond(),
//      TransferAssetHandler.get().toAssetQuanties(assetInputsAndActualTransfer.getSecond()),
//      assetAddress,
//      allInputsAndCoinChange.getSecond()
//    );
//
//    assertNotNull("Transaction should be created", tx);
//    assertEquals("The size of Tx outputs should be",
//      assetInputsAndActualTransfer.getSecond().size() + 1 + (allInputsAndCoinChange.getSecond() > 0 ? 1 : 0),
//      tx.outputs().size()
//    );
//    // CHECK OutPoint of each Tx input.
//    long inputSum = 0;
//    List<UnspentCoinDescriptor> u = allInputsAndCoinChange.getFirst();
//    for (int i = 0; i < allInputsAndCoinChange.getFirst().size(); i++) {
//      inputSum += CoinAmount.apply(u.get(i).amount()).coinUnits();
//      assertEquals(
//        "The output of Tx inputshould be euqual to that of spending coin",
//        OutPoint.apply(u.get(i).txid(), u.get(i).vout()),
//        tx.inputs().apply(i).getOutPoint()
//      );
//    }
//    // CHECK Marker Output
//    assertTrue("First Tx output should have value of 0", tx.outputs().apply(0).value() == 0);
//
//    // CHECK receiving adress of each outputs
//    List<AssetTransfer> actualTransfers = assetInputsAndActualTransfer.getSecond();
//    for (int i = 0; i < actualTransfers.size(); i++) {
//      assertEquals(
//        "Tx output locking script should be equal to that of receving address",
//        tx.outputs().apply(i + 1).lockingScript(),
//        actualTransfers.get(i).getToAddress().lockingScript()
//      );
//    }
//
//    // CHECK amounts
//    long outputSum = fees;
//    for (int i = 0; i < tx.outputs().size(); i++) {
//      outputSum += tx.outputs().apply(i).value();
//    }
//    assertEquals("The sum of output amounts + fees shoul be euqal to sum of input amounts", outputSum, inputSum);
  }


  //  IMPORTER : { WATCH-ONLY : 2000, RECEIVING ADDRESS : 8000 }
  //  SENDER   : { WATCH-ONLY : 4000, RECEIVING ADDRESS : 6000 }
  //  RECEIVER : { WATCH-ONLY : 6000, RECEIVING ADDRESS : 4000 }
  @Test
  public void createTransferTransactionSingleTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;

    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    // SPEND ALL ASSETS AND MAKE ASSET CHANGE 100
    tos.add(AssetTransferTo.apply(
      toAddress.base58(), assetId.base58(), sendRecvAssetBalance - assetChange
    ));

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      tos,
      receiverRecvAddress.assetAddress, fees
    );

    assertNotNull("TRANSFER TX", tx);
    // SPEND ALL ASSET INPUTS, INPUT SIZE SHOULD BE 2
    assertEquals("INPUT SIZE", 2, tx.inputs().size());
    // OUTPUT SIZE SOULD BE 4. (MAKER OUTPUT, ASSET TRANSFER, ASSET TRANSFER(CHANGE), COIN TRANSFER(CHAGNE))
    assertEquals("OUTPUT SIZE", 4, tx.outputs().size());
    // THE FIRST OUTPUT SHOULD BE MARKER OUTPUT.
    assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
    OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
    // ASSSET QUANTITY COUNT SHOULD BE 2. (ASSET TRANSFER, ASSET TRANSFER(CHANGE))
    assertEquals("Marker Output Quantity", 2, marker.getQuantities().length);
    assertEquals("Asset Quantity", sendRecvAssetBalance - assetChange, marker.getQuantities()[0]);
    assertEquals("Asset Change Quantity", assetChange, marker.getQuantities()[1]);
    long coinSum = fees;
    for (int j = 0; j < tx.outputs().size(); j++) {
      coinSum += tx.outputs().apply(j).value();
    }
    assert checkInputs(tx, fromAddress, coinSum);
  }

//  @Test
//  public void transferSingleAssetSpendingOneInputTest() throws OapException {
//    for (int i = 0; i < coinAddresses.length; i++) {
//      int quantity = 1000;
//      long fees = i == 0 ? IOapConstants.DEFAULT_FEES_IN_SATOSHI : 1000;
//      CoinAddress fromAddress = CoinAddress.from(coinAddresses[i]);
//      AssetAddress toAddress = AssetAddress.from(assetAddresses[i]);
//      List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
//      tos.add(AssetTransferTo.apply(assetAddresses[(i + 1) % assetAddresses.length], assetIds[i], quantity));
//      Transaction tx = TransferAssetHandler.get().createTransferTransaction(
//        AssetAddress.from(assetAddresses[i]),
//        tos,
//        AssetAddress.from(assetAddresses[i]), fees
//      );
//      assertNotNull("TRANSFER TX", tx);
//      assertEquals("INPUT COUNT SHOULD BE 2", 2, tx.inputs().size());
//      assertEquals("OUTPUT SIZE", 4, tx.outputs().size());
//      // THE FIRST OUTPUT IS MARKER OUTPUT.
//      assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
//      OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
//      assertEquals("Marker Output Quantity", 2, marker.getQuantities().length);
//      assertEquals("Asset Quantity", quantity, marker.getQuantities()[0]);
//      assertEquals("Asset Change Quantity", assetQuantities[i][i] - quantity, marker.getQuantities()[1]);
//
//      long coinSum = fees;
//      for (int j = 0; j < tx.outputs().size(); j++) {
//        coinSum += tx.outputs().apply(j).value();
//      }
//      assertTrue(checkInputs(tx, fromAddress, coinSum));
//    }
//  }

  @Test
  public void transferAssetChangeTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;

    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 0;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    // SPEND ALL ASSETS AND MAKE ASSET CHANGE 100
    tos.add(AssetTransferTo.apply(
      toAddress.base58(), assetId.base58(), sendRecvAssetBalance - assetChange
    ));

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      tos,
      receiverRecvAddress.assetAddress, fees
    );

    assertNotNull("TRANSFER TX", tx);
    // SPEND ALL ASSET INPUTS, INPUT SIZE SHOULD BE 2
    assertEquals("INPUT SIZE", 2, tx.inputs().size());
    // OUTPUT SIZE SOULD BE 4. (MAKER OUTPUT, ASSET TRANSFER, COIN TRANSFER(CHAGNE))
    assertEquals("OUTPUT SIZE", 3, tx.outputs().size());
    // THE FIRST OUTPUT SHOULD BE MARKER OUTPUT.
    assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
    OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
    // ASSSET QUANTITY COUNT SHOULD BE 2. (ASSET TRANSFER, ASSET TRANSFER(CHANGE))
    assertEquals("Marker Output Quantity Count", 1, marker.getQuantities().length);
    assertEquals("Asset Quantity", sendRecvAssetBalance - assetChange, marker.getQuantities()[0]);
    long coinSum = fees;
    for (int j = 0; j < tx.outputs().size(); j++) {
      coinSum += tx.outputs().apply(j).value();
    }
    assert checkInputs(tx, fromAddress, coinSum);
  }

  @Test
  public void createTransferTransactionNoAssetChangeNoChangeTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromCoinAddress = senderRecvadddress.address;
    AssetAddress fromAddress = senderRecvadddress.assetAddress;
    AssetId assetId = senderRecvadddress.assetId;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 0;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(fromCoinAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND ALL FOR EACH ASSET.
    // THIS WILL PRODUCE 4 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    long inputAmount = 0;
    for (UnspentCoinDescriptor desc : unspents) {
      long value = CoinAmount.apply(desc.amount()).coinUnits();
      inputAmount += value;
      if (value == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity()));
      }
    }
    fees = inputAmount - transfers.size() * IOapConstants.DUST_IN_SATOSHI;

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      transfers,
      receiverRecvAddress.assetAddress, fees
    );

    assertNotNull("TRANSFER TX", tx);
    assertEquals("INPUT SIZE", 3, tx.inputs().size());
    assertEquals("OUTPUT SIZE", 3, tx.outputs().size());
    assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
    OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
    assertEquals("Marker Output Quantity", 2, marker.getQuantities().length);
    int[] qts = marker.getQuantities();
    int qindex = 0;
    for (int i = 0; i < tx.inputs().size(); i++) {
      TransactionOutput o = OpenAssetsProtocol.get().coloringEngine().getOutput(tx.inputs().apply(i).getOutPoint());
      assertNotNull("Spending output should not be null", o);
      if (o.value() != 600) continue;

      OapTransactionOutput output = (OapTransactionOutput) o;
      AssetId id = output.getAssetId();
      boolean f = false;
      for (int j = 0; j < transfers.size(); j++) {
        if (transfers.get(j).asset_id().equals(id.base58()) && transfers.get(j).quantity() == qts[qindex]) {
          f = true;
        }
      }
      assertTrue("Asset Id and quantity should match with transfers.", f);
      qindex++;
    }

    long coinSum = fees;
    for (int j = 0; j < tx.outputs().size(); j++) {
      coinSum += tx.outputs().apply(j).value();
    }
    assertTrue(checkInputs(tx, fromCoinAddress, coinSum));
  }


  @Test
  public void transferNotEnoughCoinTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("has not enought coins");

    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;

    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * 50; // SET FEES 50BTC, THIS CAUSE not ehough coin Excpetion.

    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    // SPEND ALL ASSETS AND MAKE ASSET CHANGE 100
    tos.add(AssetTransferTo.apply(
      toAddress.base58(), assetId.base58(), sendRecvAssetBalance - assetChange
    ));

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      tos,
      receiverRecvAddress.assetAddress, fees
    );
  }

  @Test
  public void transferMulipleAssetsTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromCoinAddress = senderRecvadddress.address;
    AssetAddress fromAddress = senderRecvadddress.assetAddress;
    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 1000;
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(fromCoinAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses, true
    );
    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND 1/2 FOR EACH ASSET.
    // THIS WILL PRODUCE 4 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    for (UnspentCoinDescriptor desc : unspents) {
      if (CoinAmount.apply(desc.amount()).coinUnits() == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity() / 2));
      }
    }

    Transaction tx = transferHandler.createTransferTransaction(
      senderRecvadddress.assetAddress,
      transfers,
      receiverRecvAddress.assetAddress, fees
    );

    assertNotNull("TRANSFER TX", tx);
    assertEquals("INPUT SIZE", 3, tx.inputs().size());
    assertEquals("OUTPUT SIZE", 6, tx.outputs().size());
    assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
    OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
    assertEquals("Marker Output Quantity", 4, marker.getQuantities().length);
    int[] qts = marker.getQuantities();
    int qindex = 0;
    for (int i = 0; i < tx.inputs().size(); i++) {
      TransactionOutput o = OpenAssetsProtocol.get().coloringEngine().getOutput(tx.inputs().apply(i).getOutPoint());
      assertNotNull("Spending output should not be null", o);
      if (o.value() != 600) continue;

      OapTransactionOutput output = (OapTransactionOutput) o;
      AssetId id = output.getAssetId();
      boolean f = false;
      for (int j = 0; j < transfers.size(); j++) {
        if (
          transfers.get(j).asset_id().equals(id.base58()) &&
            transfers.get(j).quantity() == qts[qindex]
            && qts[qindex] == qts[qindex + 1] // TRANSFERED 1/2. SO CHANGE QUANTITY IS EQUAL TO TRANSFER QUANTITY
          ) {
          f = true;
        }
      }
      assertTrue("Asset Id and quantity should match with transfers.", f);
      qindex += 2;
    }
    long coinSum = fees;
    for (int j = 0; j < tx.outputs().size(); j++) {
      coinSum += tx.outputs().apply(j).value();
    }
    assert checkInputs(tx, fromCoinAddress, coinSum);
  }

  @Test
  public void transferMulipleAssetNoAssetChangeTest() throws OapException {
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromCoinAddress = senderRecvadddress.address;
    AssetAddress fromAddress = senderRecvadddress.assetAddress;
    AssetId assetId = senderRecvadddress.assetId;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 0;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<CoinAddress> addresses = new ArrayList<CoinAddress>();
    addresses.add(fromCoinAddress);
    // GET UNPENTS.
    List<UnspentCoinDescriptor> unspents = wallet.listUnspent(
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses, true
    );

    // WE HAVE 2 ASSETS FOR SENDER RECEIVING ADDERSS
    // SPEND ALL FOR EACH ASSET.
    // THIS WILL PRODUCE 2 TRANSFER OUTPUTS.
    List<AssetTransferTo> transfers = new ArrayList<AssetTransferTo>();
    long inputAmount = 0;
    for (UnspentCoinDescriptor desc : unspents) {
      long value = CoinAmount.apply(desc.amount()).coinUnits();
      inputAmount += value;
      if (value == IOapConstants.DUST_IN_SATOSHI) {
        UnspentAssetDescriptor ad = (UnspentAssetDescriptor) desc;
        transfers.add(AssetTransferTo.apply(toAddress.base58(), ad.getAssetId().base58(), ad.getQuantity()));
      }
    }

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      transfers,
      receiverRecvAddress.assetAddress, fees
    );

    assertNotNull("TRANSFER TX", tx);
    assertEquals("INPUT SIZE", 3, tx.inputs().size());
    assertEquals("OUTPUT SIZE", 4, tx.outputs().size());
    assertEquals("Marker Output value", 0, tx.outputs().apply(0).value());
    OapMarkerOutput marker = new OapMarkerOutput(tx.outputs().apply(0), null);
    assertEquals("Marker Output Quantity", 2, marker.getQuantities().length);
    int[] qts = marker.getQuantities();
    int qindex = 0;
    for (int i = 0; i < tx.inputs().size(); i++) {
      TransactionOutput o = coloringEngine.getOutput(tx.inputs().apply(i).getOutPoint());
      assertNotNull("Spending output should not be null", o);
      if (o.value() != 600) continue;

      OapTransactionOutput output = (OapTransactionOutput) o;
      AssetId id = output.getAssetId();
      boolean f = false;
      for (int j = 0; j < transfers.size(); j++) {
        if (transfers.get(j).asset_id().equals(id.base58()) && transfers.get(j).quantity() == qts[qindex]) {
          f = true;
        }
      }
      assertTrue("Asset Id and quantity should match with transfers.", f);
      qindex++;
    }

    long coinSum = fees;
    for (int j = 0; j < tx.outputs().size(); j++) {
      coinSum += tx.outputs().apply(j).value();
    }
    assertTrue(checkInputs(tx, fromCoinAddress, coinSum));
  }

  @Test
  public void noAssetTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Address has no asset");

    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;

    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    // SPEND ALL ASSETS AND MAKE ASSET CHANGE 100
    String notExistingAssetId = "odpxybEFcg9zs2eG3bijDMzhi4nCdeuRUj";
    tos.add(AssetTransferTo.apply(
      toAddress.base58(), notExistingAssetId, 1000
    ));

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      tos,
      receiverRecvAddress.assetAddress, fees
    );
  }

  @Test
  public void notEnoughAssetTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Not enoough asset");

    TransferAssetHandler handler = TransferAssetHandler.get();
    WalletSampleDataProvider provider = getDataProvider();
    // GET "SENDER" ACCOUNT
    int senderIndex = 1;
    int receiverIndex = 2;

    AddressData[] senderAddresses = provider.addressesOf(provider.accounts()[senderIndex]);
    AddressData[] receiverAddresses = provider.addressesOf(provider.accounts()[receiverIndex]);
    AddressData senderRecvadddress = senderAddresses[senderAddresses.length - 1];
    AddressData receiverRecvAddress = receiverAddresses[receiverAddresses.length - 1];
    CoinAddress fromAddress = senderRecvadddress.address;

    AssetId assetId = senderRecvadddress.assetId;
    int sendRecvAssetBalance = 10000 - (senderIndex + 1) * 2000;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    int assetChange = 1000;
    // GET UNSPENT OUTPUTS WITHOUT ASSETS
    AssetAddress toAddress = receiverRecvAddress.assetAddress;
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    // SPEND ALL ASSETS AND MAKE ASSET CHANGE 100
    tos.add(AssetTransferTo.apply(
      toAddress.base58(), assetId.base58(), sendRecvAssetBalance + 1
    ));

    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      senderRecvadddress.assetAddress,
      tos,
      receiverRecvAddress.assetAddress, fees
    );
  }


  private boolean checkInputs(Transaction tx, CoinAddress coinAddresse, long coinSum) throws OapException {
    long sum = 0;
    for (int i = 0; i < tx.inputs().size(); i++) {
      OutPoint point = tx.inputs().apply(i).getOutPoint();
      TransactionOutput prevOTX = OpenAssetsProtocol.get().chain().getRawOutput(point);
      if (!checkPublicKeyHash(prevOTX, coinAddresse))
        throw new OapException(OapException.INTERNAL_ERROR, "Invalid inpuit: " + point);
      sum += prevOTX.value();
    }
    return sum == coinSum;
  }

  private boolean checkPublicKeyHash(TransactionOutput output, CoinAddress coinAddress) {
    Collection<ScriptOp> c = JavaConverters.asJavaCollectionConverter(
      ParsedPubKeyScript.from(output.lockingScript()).scriptOps().operations()
    ).asJavaCollection();
    for (ScriptOp op : c) {
      if (op instanceof OpPush) {
        byte[] bytes = ((OpPush) op).inputValue().value();
        if (comprareBytes(bytes, coinAddress.publicKeyHash().array())) return true;
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
