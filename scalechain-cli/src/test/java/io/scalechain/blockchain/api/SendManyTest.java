package io.scalechain.blockchain.api;

import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.net.RpcSubSystem;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import kotlin.Pair;

import static org.junit.Assert.*;

/**
 * SendMany API is implemented in RpcSubsystem.sendMany() method.
 * Do the method level unit tests of RpcSubsystem.sendMany() method and RPC level tests.
 *
 * Created by shannon on 16. 12. 27.
 */
public class SendManyTest extends ApiTestWithSampleTransactions {
  RpcSubSystem  rpcSubSystem;
  String        senderAccount;
  AddressData[] senderAddressData;
  String        receiverAccount;
  AddressData[] receiverAddressData;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    rpcSubSystem = RpcSubSystem.get();
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


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  //
  // Send Many Unit Tests
  //

  // Helper methods for SendManyTest

  private List<CoinAddress> nonWatchOnlyAddressesOf(String account) {
    ArrayList<CoinAddress> buffer = new ArrayList<CoinAddress>();
    buffer.add(provider.receivingAddressOf(account).address);
    return buffer;
  }

  private Pair<List<Pair<CoinAddress, CoinAmount>>, CoinAmount> makeOutputs(AddressData[] addressData, int size, CoinAmount baseAmount, CoinAmount fees) {
    BigDecimal outputSum = fees.getValue();
    ArrayList<Pair<CoinAddress, CoinAmount>> buffer = new ArrayList<Pair<CoinAddress, CoinAmount>>();
    size = size > addressData.length ? addressData.length : size;
    for (int i = 0; i < size; i++) {
      CoinAmount amount = new CoinAmount(baseAmount.getValue().multiply(BigDecimal.valueOf(i + 1))); // baseAmount * (i + 1)
      buffer.add(new Pair<CoinAddress, CoinAmount>(addressData[i].address, amount));
      outputSum = outputSum.add(amount.getValue());
    }
    return new Pair<List<Pair<CoinAddress, CoinAmount>>, CoinAmount>(buffer, new CoinAmount(outputSum));
  }

  private List<Pair<String, BigDecimal>> makeOutputsForSendManyAPI(AddressData[] addressData, int size, CoinAmount baseAmount, boolean invalidAddress, boolean negativeAmount) {
    ArrayList<Pair<String, BigDecimal>> buffer = new ArrayList<Pair<String, BigDecimal>>();
    size = size > addressData.length ? addressData.length : size;
    for (int i = 0; i < size; i++) {
      BigDecimal amount = baseAmount.getValue().multiply(BigDecimal.valueOf((negativeAmount ? -(i + 1) : i + 1)));
      buffer.add(new Pair<String, BigDecimal>(addressData[i].address.base58() + (invalidAddress ? "invalid" : ""), amount));
    }
    return buffer;
  }

  @Test
  public void getInputsAndChangeTest() {
    List<CoinAddress> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);
    Pair<List<Pair<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      receiverAddressData.length,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees.getFirst()
    );

    // SUM inputs.
    long inputSum = 0;
    for (int i = 0; i < inputAndChange.getFirst().size(); i++) {
      UnspentCoinDescriptor unspent = inputAndChange.getFirst().get(i);
      inputSum = new CoinAmount(unspent.getAmount()).coinUnits();
    }
    CoinAmount actualChange = CoinAmount.from(inputSum - outputAndSumWithFees.getSecond().coinUnits());

    System.err.println("Change=" + inputAndChange.getSecond());
    System.err.println("inputSum=" + inputSum);
    System.err.println("outputAndSumWithFees=" + outputAndSumWithFees);

    assertEquals("change should be inputSum - outputSum", inputAndChange.getSecond(), actualChange);
  }

  @Test
  public void getInputsAndChangeNoAddressesTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("No outputs");

    List<CoinAddress> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);

    Pair<List<Pair<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      0,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );

    // DO TEST
    Pair<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees.getFirst()
    );
    //  CHECK
    BigDecimal inputSum = BigDecimal.valueOf(0);
    for (int i = 0; i < inputAndChange.getFirst().size(); i++) {
      inputSum = inputSum.add(inputAndChange.getFirst().get(i).getAmount());
    }
    assertEquals("Sum of inputs should be equal to Sum of outputs + fees", inputSum, outputAndSumWithFees.getSecond().getValue().add(inputAndChange.getSecond().getValue()));
  }

  @Test
  public void buildTransationTest() {
    // MAKE PRE TEST DATA
    //   ADDRESS LIST FOR senderAccount
    List<CoinAddress> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);
    //   SAMPLE OUTPUTS DATA
    Pair<List<Pair<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      receiverAddressData.length,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );
    //   GET UNSPENT COINS AND CACULATE CHANGE
    Pair<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees.getFirst()
    );

    // DO TEST
    Transaction tx = rpcSubSystem.buildSendManyTransaction(inputAndChange, outputAndSumWithFees.getFirst(), senderAccount);

    // CHECK RESULT
    assertNotNull("Transaction should be returned", tx);
    //   COMPARE OUTPUTS
    for (int i = 0; i < outputAndSumWithFees.getFirst().size(); i++) {
      outputAndSumWithFees.getFirst().get(i).getSecond();
      assertEquals("Each output should be equal",
        outputAndSumWithFees.getFirst().get(i).getSecond(), CoinAmount.from(tx.getOutputs().get(i).getValue())
      );
    }
    //   COMPARE CHANGE
    if (inputAndChange.getSecond().getValue().compareTo(BigDecimal.valueOf(0L)) > 0 ) {
      assertEquals("Change sould be equal",
        inputAndChange.getSecond(),
        CoinAmount.from(tx.getOutputs().get(tx.getOutputs().size() - 1).getValue())
      );
    }
    //   CHECK INPUTS
    for (int i = 0; i < tx.getInputs().size(); i++) {
      TransactionOutput spedingOutput = Blockchain.get().getTransactionOutput(Blockchain.get().getDb(), tx.getInputs().get(i).getOutPoint());
      UnspentCoinDescriptor unspent = inputAndChange.getFirst().get(i);
      assertEquals(
        "Inpout amount(" + i + ") should be match",
        CoinAmount.from(spedingOutput.getValue()),
        new CoinAmount(unspent.getAmount())
      );
    }
  }


  private void checkTx(List<Pair<String, CoinAmount>> outputs, Transaction tx) {
    for (int i = 0; i < outputs.size(); i++) {
      assertEquals("Receving amount should be equal to output amount",
        outputs.get(i).getSecond(),
        CoinAmount.from(tx.getOutputs().get(i).getValue())
      );
      assertEquals("Receiving address should match to output address",
        CoinAddress.from(outputs.get(i).getFirst()).lockingScript(),
        tx.getOutputs().get(i).getLockingScript()
      );
    }
    //    CHECK INPUT & FEE
  }

  private void checkTxForSendManyAPI(List<Pair<String, BigDecimal>> outputs, Transaction tx) {
    for (int i = 0; i < outputs.size(); i++) {
      assertEquals("Receving amount should be equal to output amount",
        new CoinAmount(outputs.get(i).getSecond()),
        CoinAmount.from(tx.getOutputs().get(i).getValue())
      );
      assertEquals("Receiving address should match to output address",
        CoinAddress.from(outputs.get(i).getFirst()).lockingScript(),
        tx.getOutputs().get(i).getLockingScript()
      );
    }
    //    CHECK INPUT & FEE
  }


  //
  // SendMany Handler
  //
  @Test
  public void sendManySignleAddressTest() {
    // 1 OUTPUT TO RECEIVER ADDRESS 0, 1BTC
    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 1,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    Hash hash = rpcSubSystem.sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("Hash should not be null", hash);
    // DO TEST
    Transaction tx = Blockchain.get().getTransaction(Blockchain.get().getDb(), hash);
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx != null);
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx);
    waitForChain(1);
  }

  @Test
  public void sendManyMultipleAddressTest() {
    java.util.List<CoinAddress> addresses = new java.util.ArrayList<CoinAddress>();
    addresses.add(provider.receivingAddressOf(provider.accounts()[1]).address);
    List<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
      Blockchain.get().getDb(),
      Blockchain.get(),
      0,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      addresses
    );
    System.err.println("Unspents.size()" + unspents.size());
    for(int i = 0;i < unspents.size();i++) {
      System.out.println(unspents.get(i).getConfirmations()+"==>" +unspents.get(i).toString());
    }
    System.out.println("ACCOUNT==>" + provider.accounts()[1]);
    System.out.println("RECEIVING ADDRESS=" + provider.receivingAddressOf(provider.accounts()[1]).address + " > " + provider.receivingAddressOf(provider.accounts()[1]).address.base58());
    System.out.println("WATCHONLY ADDRESS=" + provider.watchOnlyAddressesOf(provider.accounts()[1])[0].address + " > " + provider.watchOnlyAddressesOf(provider.accounts()[1])[0].address.base58());

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 2,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    Hash hash = rpcSubSystem.sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("TxHash should not be null", hash);

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
    thrown.expect(OapException.class);
    thrown.expectMessage("No outputs");

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 0,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false
    );
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyFromNonExistingAccountTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("No addresses exist for account");

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData,
      2,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      false, false
    );

    Hash hash = rpcSubSystem.sendMany("ACCOUNT_NOBODY",outputs,null,null);
  }

  @Test
  public void sendManyNotEnoughCoinTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("not enough coins");

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      new CoinAmount(BigDecimal.valueOf(10000)), // 10000BTC
      false,
      false
    );
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyToInvalidAddressTest() {
     thrown.expect(OapException.class);
     thrown.expectMessage("Cannot convert address");

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      new CoinAmount(BigDecimal.valueOf(1)), // 1BTC
      true,
      false);
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyNegativeAmountTest() {
    thrown.expect(Exception.class);
    thrown.expectMessage("Negative amount");

    List<Pair<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      new CoinAmount(BigDecimal.valueOf(1)), // 10000BTC
      false,
      true
    );

    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }
}

