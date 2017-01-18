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
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.Option;
import scala.collection.JavaConverters;
import scala.math.BigDecimal;

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
      BigDecimal amount = baseAmount.value().$times(BigDecimal.decimal((negativeAmount ? -(i + 1) : i + 1)));

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
      BigDecimal amount = BigDecimal.javaBigDecimal2bigDecimal(e.getValue().getAsBigDecimal());
      assertEquals("Receving amount should be equal to output amount",
        CoinAmount.apply(amount),
        CoinAmount.from(tx.outputs().apply(i).value())
      );
      assertEquals("Receiving address should match to output address",
        address.lockingScript(),
        tx.outputs().apply(i).lockingScript()
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
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    String hashHex = sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("Hash should not be null", hashHex);
    Hash hash = Hash.apply(ByteArray.apply(HexUtil.bytes(hashHex)));

    // DO TEST
    Option<Transaction> tx = Blockchain.get().getTransaction(hash, Blockchain.get().db());
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx.isDefined());
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx.get());
    waitForChain(1);
  }

  @Test
  public void sendManyMultipleAddressTest() throws RpcInvoker.RpcCallException {
    java.util.List<CoinAddress> addresses = new java.util.ArrayList<CoinAddress>();
    addresses.add(provider.receivingAddressOf(provider.accounts()[1]).address);
    scala.collection.immutable.List<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
      Blockchain.get(),
      0,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      Option.apply(JavaConverters.asScalaBuffer(addresses).toList()),
      Blockchain.get().db()
    );
    for (int i = 0; i < unspents.size(); i++) {
      System.out.println(unspents.apply(i).confirmations() + "==>" + unspents.apply(i).toString());
    }

    JsonObject outputs = outputsForSendManyAPI(
      receiverAddressData, 2,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    String hashHex = sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("TxHash should not be null", hashHex);
    Hash hash = Hash.apply(ByteArray.apply(HexUtil.bytes(hashHex)));

    // DO TEST
    Option<Transaction> tx = Blockchain.get().getTransaction(hash, Blockchain.get().db());
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx.isDefined());
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx.get());

    waitForChain(1);
  }


  @Test
  public void sendManyWithNoOutputsTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(
        receiverAddressData, 0,
        CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
        false, false
      );
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"No outputs.\"", e.getData().contains("No outputs."));
    }

  }

  @Test
  public void sendManyFromNonExistingAccountTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(
        receiverAddressData,
        2,
        CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
        false, false
      );

      String hashHex = sendMany("ACCOUNT_NOBODY", outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should start with \"No addresses exist for account\"", e.getData().startsWith("No addresses exist for account"));
    }

  }

  @Test
  public void sendManyNotEnoughCoinTest() {
    try {

      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        CoinAmount.apply(BigDecimal.valueOf(10000)), // 10000BTC
        false,
        false
      );
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"not enought coins\"", e.getData().contains("not enought coins"));
    }

  }

  @Test
  public void sendManyToInvalidAddressTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
        true,
        false);
      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"Cannot convert address\"", e.getData().contains("Cannot convert address"));
    }
  }

  @Test
  public void sendManyNegativeAmountTest() {
    try {
      JsonObject outputs = outputsForSendManyAPI(receiverAddressData,
        2,
        CoinAmount.apply(BigDecimal.valueOf(1)), // 10000BTC
        false,
        true
      );

      String hashHex = sendMany(senderAccount, outputs, null, null);
    } catch (RpcInvoker.RpcCallException e) {
      assertTrue("RPC error data should contain \"Negative amount\"", e.getData().contains("Negative amount"));
    }

  }
}
