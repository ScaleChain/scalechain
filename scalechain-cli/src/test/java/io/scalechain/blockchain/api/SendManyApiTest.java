package io.scalechain.blockchain.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by shannon on 17. 1. 14.
 */
public class SendManyApiTest extends ApiTestWithSampleTransactions {

  String senderAccount;
  AddressData[] senderAddressData;
  String receiverAccount;
  AddressData[] receiverAddressData;


  @Override
  @Before
  public void setUp() {
    super.setUp();
    senderAccount = provider.accounts()[1];
    senderAddressData = provider.addressesOf(senderAccount);
    receiverAccount = provider.accounts()[2];
    receiverAddressData = provider.addressesOf(receiverAccount);
  }

  @Override
  @After
  public void tearDown() {
    super.tearDown();
  }

  private JsonObject outputsForSendManyAPI(AddressData[] addressData, int size, CoinAmount baseAmount, boolean invalidAddress, boolean negativeAmount) {
    JsonObject outputs = new JsonObject();
    size = size > addressData.length ? addressData.length : size;
    for (int i = 0; i < size; i++) {
      BigDecimal amount = baseAmount.getValue().multiply(BigDecimal.valueOf(negativeAmount ? -(i + 1) : i + 1));

      outputs.addProperty(
        addressData[i].address.base58() + (invalidAddress ? "invalid" : ""),
        amount
      );
    }
    return outputs;
  }

  private void checkTxForSendManyAPI(JsonObject outputs, Transaction tx) {
    int i = 0;
    for (Map.Entry<String, JsonElement> e : outputs.entrySet()) {
      CoinAddress address = CoinAddress.from(e.getKey());
      BigDecimal amount = e.getValue().getAsBigDecimal();
      assertEquals("Receving amount should be equal to output amount",
        new CoinAmount(amount),
        CoinAmount.from(tx.getOutputs().get(i).getValue())
      );
      assertEquals("Receiving address should match to output address",
        address.lockingScript(),
        tx.getOutputs().get(i).getLockingScript()
      );
      i++;
    }
    //    CHECK INPUT & FEE
  }

  @Test
  public void sendManySignleAddressTest() throws RpcInvoker.RpcCallException {
    // 1 OUTPUT TO RECEIVER ADDRESS 0, 1BTC
    JsonObject outputs = outputsForSendManyAPI(
      receiverAddressData, 1,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    String hashHex = sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("Hash should not be null", hashHex);
    Hash hash = new Hash( new Bytes(HexUtil.bytes(hashHex)));

    // DO TEST
    Transaction tx = Blockchain.get().getTransaction(Blockchain.get().getDb(), hash);
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx != null);
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx);
    waitForChain(1);
  }

  @Test
  public void sendManyMultipleAddressTest() throws RpcInvoker.RpcCallException {
    java.util.List<CoinAddress> addresses = new java.util.ArrayList<CoinAddress>();
    addresses.add(provider.receivingAddressOf(provider.accounts()[1]).address);
    List<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
      Blockchain.get().getDb(),
      Blockchain.get(),
      0,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses
    );
    for (int i = 0; i < unspents.size(); i++) {
      System.out.println(unspents.get(i).getConfirmations() + "==>" + unspents.get(i).toString());
    }

    JsonObject outputs = outputsForSendManyAPI(
      receiverAddressData, 2,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    String hashHex = sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("TxHash should not be null", hashHex);
    Hash hash = new Hash(new Bytes(HexUtil.bytes(hashHex)));

    // DO TEST
    Transaction tx = Blockchain.get().getTransaction(Blockchain.get().getDb(), hash);
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx != null);
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx);

    waitForChain(1);
  }


  @Test
  public void sendManyWithNoOutputsTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(
        receiverAddressData, 0,
        new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
        false, false
      );
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"No outputs.\"", e.getData().contains("No outputs."));
    }
    // TODO : OAP : ASK to Shannon : If the exception is not thrown, the test passes. Need to make sure the exception is thrown.
  }

  @Test
  public void sendManyFromNonExistingAccountTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(
        receiverAddressData,
        2,
        new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
        false, false
      );

      String hashHex = sendMany("ACCOUNT_NOBODY", outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should start with \"No addresses exist for account\"", e.getData().startsWith("No addresses exist for account"));
    }
    // TODO : OAP : ASK to Shannon : If the exception is not thrown, the test passes. Need to make sure the exception is thrown.
  }

  @Test
  public void sendManyNotEnoughCoinTest() {
    try {

      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        new CoinAmount(BigDecimal.valueOf(10000)), // 10000BTC
        false,
        false
      );
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"not enought coins\"", e.getData().contains("not enought coins"));
    }
    // TODO : OAP : ASK to Shannon : If the exception is not thrown, the test passes. Need to make sure the exception is thrown.
  }

  @Test
  public void sendManyToInvalidAddressTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
        true,
        false);
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"Cannot convert address\"", e.getData().contains("Cannot convert address"));
    }
    // TODO : OAP : ASK to Shannon : If the exception is not thrown, the test passes. Need to make sure the exception is thrown.
  }

  @Test
  public void sendManyNegativeAmountTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        new CoinAmount(BigDecimal.valueOf(1)), // 10000BTC
        false,
        true
      );

      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"Negative amount\"", e.getData().contains("Negative amount"));
    }
    // TODO : OAP : ASK to Shannon : If the exception is not thrown, the test passes. Need to make sure the exception is thrown.
  }
}
