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
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;
import scala.math.BigDecimal;

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
    ListBuffer<CoinAddress> buffer = new ListBuffer<CoinAddress>();
    buffer.$plus$eq(provider.receivingAddressOf(account).address);
    return buffer.toList();
  }

  private Tuple2<List<Tuple2<CoinAddress, CoinAmount>>, CoinAmount> makeOutputs(AddressData[] addressData, int size, CoinAmount baseAmount, CoinAmount fees) {
    BigDecimal outputSum = fees.value();
    ListBuffer<Tuple2<CoinAddress, CoinAmount>> buffer = new ListBuffer<Tuple2<CoinAddress, CoinAmount>>();
    size = size > addressData.length ? addressData.length : size;
    for (int i = 0; i < size; i++) {
      CoinAmount amount = CoinAmount.apply(baseAmount.value().$times(BigDecimal.decimal(i + 1))); // baseAmount * (i + 1)
      buffer.$plus$eq(new Tuple2<CoinAddress, CoinAmount>(addressData[i].address, amount));
      outputSum = outputSum.$plus(amount.value());
    }
    return new Tuple2<List<Tuple2<CoinAddress, CoinAmount>>, CoinAmount>(buffer.toList(), CoinAmount.apply(outputSum));
  }

  private List<Tuple2<String, BigDecimal>> makeOutputsForSendManyAPI(AddressData[] addressData, int size, CoinAmount baseAmount, boolean invalidAddress, boolean negativeAmount) {
    ListBuffer<Tuple2<String, BigDecimal>> buffer = new ListBuffer<Tuple2<String, BigDecimal>>();
    size = size > addressData.length ? addressData.length : size;
    for (int i = 0; i < size; i++) {
      BigDecimal amount = baseAmount.value().$times(BigDecimal.decimal((negativeAmount ? -(i + 1) : i + 1)));
      buffer.$plus$eq(new Tuple2<String, BigDecimal>(addressData[i].address.base58() + (invalidAddress ? "invalid" : ""), amount));
    }
    return buffer.toList();
  }

  @Test
  public void getInputsAndChangeTest() {
    Option<List<CoinAddress>> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);
    Tuple2<List<Tuple2<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      receiverAddressData.length,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );

    // DO TEST
    Tuple2<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees._1()
    );

    // SUM inputs.
    long inputSum = 0;
    for (int i = 0; i < inputAndChange._1().size(); i++) {
      UnspentCoinDescriptor unspent = inputAndChange._1().apply(i);
      inputSum = CoinAmount.apply(unspent.amount()).coinUnits();
    }
    CoinAmount actualChange = CoinAmount.from(inputSum - outputAndSumWithFees._2().coinUnits());

    System.err.println("Change=" + inputAndChange._2());
    System.err.println("inputSum=" + inputSum);
    System.err.println("outputAndSumWithFees=" + outputAndSumWithFees);

    assertEquals("change should be inputSum - outputSum", inputAndChange._2(), actualChange);
  }

  @Test
  public void getInputsAndChangeNoAddressesTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("No outputs");

    Option<List<CoinAddress>> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);

    Tuple2<List<Tuple2<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      0,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );

    // DO TEST
    Tuple2<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees._1()
    );
    //  CHECK
    BigDecimal inputSum = BigDecimal.decimal(0);
    for (int i = 0; i < inputAndChange._1().size(); i++) {
      inputSum = inputSum.$plus(inputAndChange._1().apply(i).amount());
    }
    assertEquals("Sum of inputs should be equal to Sum of outputs + fees", inputSum, outputAndSumWithFees._2().value().$plus(inputAndChange._2().value()));
  }

  @Test
  public void buildTransationTest() {
    // MAKE PRE TEST DATA
    //   ADDRESS LIST FOR senderAccount
    Option<List<CoinAddress>> addressesOption = rpcSubSystem.nonWatchOnlyAddressesOf(senderAccount);
    //   SAMPLE OUTPUTS DATA
    Tuple2<List<Tuple2<CoinAddress, CoinAmount>>, CoinAmount> outputAndSumWithFees = makeOutputs(
      receiverAddressData,
      receiverAddressData.length,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      CoinAmount.from(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
    );
    //   GET UNSPENT COINS AND CACULATE CHANGE
    Tuple2<List<UnspentCoinDescriptor>, CoinAmount> inputAndChange = rpcSubSystem.calculateInputsAndChange(
      addressesOption, outputAndSumWithFees._1()
    );

    // DO TEST
    Transaction tx = rpcSubSystem.buildSendManyTransaction(inputAndChange, outputAndSumWithFees._1(), senderAccount);

    // CHECK RESULT
    assertNotNull("Transaction should be returned", tx);
    //   COMPARE OUTPUTS
    for (int i = 0; i < outputAndSumWithFees._1().size(); i++) {
      outputAndSumWithFees._1().apply(i)._2();
      assertEquals("Each output should be equal",
        outputAndSumWithFees._1().apply(i)._2(), CoinAmount.from(tx.outputs().apply(i).value())
      );
    }
    //   COMPARE CHANGE
    if (inputAndChange._2().value().$greater(BigDecimal.decimal(0L))) {
      assertEquals("Change sould be equal",
        inputAndChange._2(),
        CoinAmount.from(tx.outputs().apply(tx.outputs().size() - 1).value())
      );
    }
    //   CHECK INPUTS
    for (int i = 0; i < tx.inputs().size(); i++) {
      TransactionOutput spedingOutput = Blockchain.get().getTransactionOutput(tx.inputs().apply(i).getOutPoint(), Blockchain.get().db());
      UnspentCoinDescriptor unspent = inputAndChange._1().apply(i);
      assertEquals(
        "Inpout amount(" + i + ") should be match",
        CoinAmount.from(spedingOutput.value()),
        CoinAmount.apply(unspent.amount())
      );
    }
  }


  private void checkTx(List<Tuple2<String, CoinAmount>> outputs, Transaction tx) {
    for (int i = 0; i < outputs.size(); i++) {
      assertEquals("Receving amount should be equal to output amount",
        outputs.apply(i)._2(),
        CoinAmount.from(tx.outputs().apply(i).value())
      );
      assertEquals("Receiving address should match to output address",
        CoinAddress.from(outputs.apply(i)._1()).lockingScript(),
        tx.outputs().apply(i).lockingScript()
      );
    }
    //    CHECK INPUT & FEE
  }

  private void checkTxForSendManyAPI(List<Tuple2<String, BigDecimal>> outputs, Transaction tx) {
    for (int i = 0; i < outputs.size(); i++) {
      assertEquals("Receving amount should be equal to output amount",
        CoinAmount.apply(outputs.apply(i)._2()),
        CoinAmount.from(tx.outputs().apply(i).value())
      );
      assertEquals("Receiving address should match to output address",
        CoinAddress.from(outputs.apply(i)._1()).lockingScript(),
        tx.outputs().apply(i).lockingScript()
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
    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 1,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    Hash hash = rpcSubSystem.sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("Hash should not be null", hash);
    // DO TEST
    Option<Transaction> tx = Blockchain.get().getTransaction(hash, Blockchain.get().db());
    //    CHECK TxHash returned.
    assertTrue("Transaction should be exists", tx.isDefined());
    //    CHECK EACH OUTPUT IS CORRECT
    checkTxForSendManyAPI(outputs, tx.get());
    waitForChain(1);
  }

  @Test
  public void sendManyMultipleAddressTest() {
    java.util.List<CoinAddress> addresses = new java.util.ArrayList<CoinAddress>();
    addresses.add(provider.receivingAddressOf(provider.accounts()[1]).address);
    scala.collection.immutable.List<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
      Blockchain.get(),
      0,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      Option.apply(JavaConverters.asScalaBuffer(addresses).toList()),
      Blockchain.get().db()
    );
    System.err.println("Unspents.size()" + unspents.size());
    for(int i = 0;i < unspents.size();i++) {
      System.out.println(unspents.apply(i).confirmations()+"==>" +unspents.apply(i).toString());
    }
    System.out.println("ACCOUNT==>" + provider.accounts()[1]);
    System.out.println("RECEVING ADDRESS=" + provider.receivingAddressOf(provider.accounts()[1]).address + " > " + provider.receivingAddressOf(provider.accounts()[1]).address.base58());
    System.out.println("WATCHONLY ADDRESS=" + provider.watchOnlyAddressesOf(provider.accounts()[1])[0].address + " > " + provider.watchOnlyAddressesOf(provider.accounts()[1])[0].address.base58());

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 2,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false);

    Hash hash = rpcSubSystem.sendMany(
      senderAccount,
      outputs,
      null,
      null
    );

    assertNotNull("TxHash should not be null", hash);

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
    thrown.expect(OapException.class);
    thrown.expectMessage("No outputs");

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData, 0,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false
    );
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyFromNonExistingAccountTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("No addresses exist for account");

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(
      receiverAddressData,
      2,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      false, false
    );

    Hash hash = rpcSubSystem.sendMany("ACCOUNT_NOBODY",outputs,null,null);
  }

  @Test
  public void sendManyNotEnoughCoinTest() {
    thrown.expect(OapException.class);
    thrown.expectMessage("not enought coins");

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      CoinAmount.apply(BigDecimal.valueOf(10000)), // 10000BTC
      false,
      false
    );
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyToInvalidAddressTest() {
     thrown.expect(OapException.class);
     thrown.expectMessage("Cannot convert address");

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 1BTC
      true,
      false);
    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }

  @Test
  public void sendManyNegativeAmountTest() {
    thrown.expect(Exception.class);
    thrown.expectMessage("Negative amount");

    List<Tuple2<String, BigDecimal>> outputs = makeOutputsForSendManyAPI(receiverAddressData,
      2,
      CoinAmount.apply(BigDecimal.valueOf(1)), // 10000BTC
      false,
      true
    );

    Hash hash = rpcSubSystem.sendMany(senderAccount, outputs, null, null);
  }
}

