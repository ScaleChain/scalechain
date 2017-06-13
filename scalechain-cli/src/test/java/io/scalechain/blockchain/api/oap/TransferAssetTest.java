package io.scalechain.blockchain.api.oap;

import io.scalechain.blockchain.GeneralException;
import io.scalechain.blockchain.api.ApiTestWithSampleTransactions;
import io.scalechain.blockchain.net.RpcSubSystem;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.transaction.PrivateKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Most of the method level unit tests are done in oap module.
 * Do unit tests for RpcSubsystem.transferAsset method.
 *
 * Created by shannon on 17. 1. 3.
 */
public class TransferAssetTest extends ApiTestWithSampleTransactions {

  @Override
  @Before
  public void setUp(){
    super.setUp();
  }

  @Override
  @After
  public void tearDown() {
    super.tearDown();
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

//  @Test
//  public void transferAssetWithNegativeQuantity() {
//  }
//
//  @Test
//  void transferAssetWithInvalidAssetIdTest() {
//  }

  @Test
  public void transferAssetWithoutPrivateKeyTest() {
    thrown.expect(GeneralException.class);

    RpcSubSystem rpc = RpcSubSystem.get();
    AddressData senderAddressData = provider.watchOnlyAddressesOf(provider.ACCOUNT_SENDER)[0];
    String fromAddress = senderAddressData.assetAddress.base58();
    PrivateKey privateKey = senderAddressData.privateKey;
    String assetId = provider.receivingAddressOf(provider.ACCOUNT_SENDER).assetId.base58();


    AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String toAddress = receiverAddressData.assetAddress.base58();

    // Provide no private key.
    List<PrivateKey> list = new ArrayList<PrivateKey>();
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(new AssetTransferTo(toAddress, assetId,1000));

    rpc.transferAsset(
      fromAddress,
      tos,
      list,
      fromAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI
    );

    // TODO : OAP : ASK to Shannon : need to check for seeing if transfer completed?
  }


  @Test
  public void transferAssetNotEnoughAssetTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("Not enough asset");

    RpcSubSystem rpc = RpcSubSystem.get();
    AddressData senderAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String fromAddress = senderAddressData.assetAddress.base58();
    PrivateKey privateKey = senderAddressData.privateKey;
    String assetId = provider.receivingAddressOf(provider.ACCOUNT_SENDER).assetId.base58();

    long assetBalance = provider.assetBalanceOf(fromAddress).get(senderAddressData.assetId.base58());
    int quantity = (int)assetBalance * 2;

    AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String toAddress = receiverAddressData.assetAddress.base58();

    // Provide no private key.
    List<PrivateKey> list = new ArrayList<PrivateKey>();
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(new AssetTransferTo(toAddress, assetId, quantity));

    rpc.transferAsset(
      fromAddress,
      tos,
      list,
      fromAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI
    );
    assertTrue("Transfer should fail", false);

    // TODO : OAP : ASK to Shannon : How did this test pass?
  }

  @Test
  public void transferAssetNoAssetTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("has no asset");

    RpcSubSystem rpc = RpcSubSystem.get();
    AddressData senderAddressData = provider.watchOnlyAddressesOf(provider.ACCOUNT_SENDER)[0];
    String fromAddress = senderAddressData.assetAddress.base58();
    PrivateKey privateKey = senderAddressData.privateKey;

    AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String toAddress = receiverAddressData.assetAddress.base58();

    // Provide no private key.
    List<PrivateKey> list = new ArrayList<PrivateKey>();
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(new AssetTransferTo(toAddress, senderAddressData.assetId.base58(),1000));

    rpc.transferAsset(
      fromAddress,
      tos,
      list,
      fromAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI
    );

    // TODO : OAP : ASK to Shannon : Need to check the result of transfer?
  }

  @Test
  public void transferAssetWithInvalidToAddressTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid AssetAddress");

    RpcSubSystem rpc = RpcSubSystem.get();
    AddressData senderAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String fromAddress = senderAddressData.address.base58();
    PrivateKey privateKey = senderAddressData.privateKey;

    AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String toAddress = receiverAddressData.assetAddress.base58();

    List<PrivateKey> list = new ArrayList<PrivateKey>();
    list.add(privateKey);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(new AssetTransferTo("INVALID_ASSET_ADDRESS", senderAddressData.assetId.base58(),1000));

    rpc.transferAsset(
      fromAddress,
      tos,
      list,
      fromAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI
    );

    // TODO : OAP : ASK to Shannon : Need to check the result of transfer?
  }

  @Test
  public void transferAssetWithInvalidFromAddressTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid AssetAddress");

    RpcSubSystem rpc = RpcSubSystem.get();
    AddressData senderAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String fromAddress = "AA" + senderAddressData.address.base58();
    PrivateKey privateKey = senderAddressData.privateKey;

    AddressData receiverAddressData = provider.receivingAddressOf(provider.ACCOUNT_SENDER);
    String toAddress = receiverAddressData.assetAddress.base58();

    List<PrivateKey> list = new ArrayList<PrivateKey>();
    list.add(privateKey);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(new AssetTransferTo(toAddress, senderAddressData.assetId.base58(),1000));

    rpc.transferAsset(
      fromAddress,
      tos,
      list,
      fromAddress,
      IOapConstants.DEFAULT_FEES_IN_SATOSHI
    );

    // TODO : OAP : ASK to Shannon : Need to check the result of transfer?
  }

}
