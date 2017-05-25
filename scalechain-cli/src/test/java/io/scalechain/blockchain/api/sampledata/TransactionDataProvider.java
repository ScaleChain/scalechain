package io.scalechain.blockchain.api.sampledata;

import io.scalechain.blockchain.chain.*;
import io.scalechain.blockchain.net.RpcSubSystem;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.AddressDataProvider;
import io.scalechain.blockchain.oap.sampledata.IAddressDataProvider;
import io.scalechain.blockchain.oap.sampledata.IAddressGenerationListener;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import kotlin.Pair;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.script.HashCalculator;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.blockchain.transaction.*;
import io.scalechain.util.Bytes;
import io.scalechain.util.Config;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.Wallet;
import scala.Option;
import scala.collection.JavaConverters;

import java.math.BigDecimal;
import java.util.*;

/**
 * Utility class for providing test data.
 *
 * Create 3 accounts, "IMPORTER", "SENDER", "RECEIVER"
 * and 2 addresses,
 *  one for watch-only address and
 *  one for receiving address for each account.
 *
 * Transfers 49BTC to the receiving address for each account.
 * Issues an asset from the receving address to receiving address for each account.
 * Trans the asset issued above to watch-only address.
 * Issues an asset from watch-only address to receving address for each account
 *
 * Calculate balances for each account.
 *
 * Created by shannon on 17. 1. 9.
 */
public class TransactionDataProvider implements IAddressGenerationListener, IAddressDataProvider {
  static ListConverter<NewOutput> NewOutListConveter = new ListConverter<NewOutput>();
  Random random = new Random();

  private static class ListConverter<T> {
    public List<T> toList(T... elements) {
      List<T> result = new ArrayList<T>();
      for (T e : elements) {
        result.add(e);
      }
      return result;
    }
  }

  Blockchain chain;
  Wallet wallet;

  public BlockchainView blockChainView() {
    return chain;
  }

  public KeyValueDatabase db() {
    return chain.getDb();
  }

  AddressDataProvider addressDataProvider;

  //
  // Delegate AddressDataProvider interface
  //
  @Override
  public String internalAccount() {
    return addressDataProvider.internalAccount();
  }

  @Override
  public AddressData internalAddressData() {
    return addressDataProvider.internalAddressData();
  }

  @Override
  public String[] accounts() {
    return addressDataProvider.accounts();
  }

  @Override
  public AddressData[] addressesOf(String account) {
    return addressDataProvider.addressesOf(account);
  }

  @Override
  public AddressData[] watchOnlyAddressesOf(String account) {
    return addressDataProvider.watchOnlyAddressesOf(account);
  }

  @Override
  public AddressData receivingAddressOf(String account) {
    return addressDataProvider.receivingAddressOf(account);
  }

  @Override
  public PrivateKey[] privateKeysOf(String account) {
    return addressDataProvider.privateKeysOf(account);
  }

  @Override
  public AddressData addressDataOf(String coinaddressOrAssetAddressOrAssetId) {
    return addressDataProvider.addressDataOf(coinaddressOrAssetAddressOrAssetId);
  }

  //
  //  Process Address Generation Event
  //
  //  Regiter genrated address to Wallet.
  //
  @Override
  public void onAddressGeneration(String account, CoinAddress address, PrivateKey privateKey) {
    System.out.println("onAddressGeneration(" + account + ", " + address + ", " + privateKey + ")");
    if (privateKey == null) {
      wallet.importOutputOwnership( db(), blockChainView(), account, address, false);
    } else {
      wallet.getStore().putOutputOwnership(db(), account, address);
      List<PrivateKey> keys = new ArrayList<PrivateKey>();
      keys.add(privateKey);
      wallet.getStore().putPrivateKeys(db(), address, keys);
      wallet.getStore().putReceivingAddress(db(), account, address);
    }
  }

  public TransactionDataProvider(Blockchain chain, Wallet wallet) {
    this.chain = chain;
    this.wallet = wallet;

    String miningAccount = miningAccount();
    AddressData miningAddress = miningAddress(Wallet.get(), Blockchain.get().getDb(), miningAccount);
    this.addressDataProvider = new AddressDataProvider(miningAccount, miningAddress);
    this.addressDataProvider.addListener(this);
  }

  public void dispose() {
    this.db().close();
  }

  private static String miningAccount() {
    String miningAccount = Config.get().getString("scalechain.mining.account");
    return miningAccount == null ? AddressDataProvider.DEFAULT_MINING_ACCOUNT : miningAccount;
  }

  private static AddressData miningAddress(Wallet wallet, KeyValueDatabase db, String miningAccount) {
    CoinAddress address = wallet.getReceivingAddress(db, miningAccount);
    return AddressData.from(address);
  }

  //
  // Initial Balances
  //
  public HashMap<String, BigDecimal> balances = new HashMap<String, BigDecimal>();
  public HashMap<String, HashMap<String, Long>> assetBalances = new HashMap<String, HashMap<String, Long>>();

  public BigDecimal balanceOf(String accountOrAddress) {
    return balances.get(accountOrAddress);
  }

  public HashMap<String, Long> assetBalanceOf(String accountOrAssetAddress) {
    return assetBalances.get(accountOrAssetAddress);
  }

  private void addBalance(String account, String address, BigDecimal amount) {
    BigDecimal balance = balances.get(account);
    balance = (balance == null ? BigDecimal.ZERO : balance).add(amount);
    balances.put(account, balance);

    balance = balances.get(address);
    balance = (balance == null ? BigDecimal.ZERO : balance).add(amount);
    balances.put(address, balance);
  }

  private void substractBalance(String account, String address, BigDecimal amount) {
    BigDecimal balance = balances.get(account);
    balance = (balance == null ? BigDecimal.ZERO : balance).subtract(amount);
    balances.put(account, balance);

    balance = balances.get(address);
    balance = (balance == null ? BigDecimal.ZERO : balance).subtract(amount);
    balances.put(address, balance);
  }


  private void addAssetBalance(String account, String assetAddress, String assetId, int quantity) {
    HashMap<String, Long> map = assetBalances.get(account);
    if (map == null) map = new HashMap<String, Long>();
    Long balance = map.get(assetId);
    balance = (balance == null ? 0 : balance) + quantity;
    map.put(assetId, balance);
    assetBalances.put(account, map);

    map = assetBalances.get(assetAddress);
    if (map == null) map = new HashMap<String, Long>();
    balance = map.get(assetId);
    balance = (balance == null ? 0 : balance) + quantity;
    map.put(assetId, balance);
    assetBalances.put(assetAddress, map);
  }

  private void substractAssetBalance(String account, String assetAddress, String assetId, int quantity) {
    HashMap<String, Long> map = assetBalances.get(account);
    if (map == null) map = new HashMap<String, Long>();
    Long balance = map.get(assetId);
    balance = (balance == null ? 0 : balance) - quantity;
    map.put(assetId, balance);
    assetBalances.put(account, map);

    map = assetBalances.get(assetAddress);
    if (map == null) map = new HashMap<String, Long>();
    balance = map.get(assetId);
    balance = (balance == null ? 0 : balance) - quantity;
    map.put(assetId, balance);
    assetBalances.put(assetAddress, map);
  }

  //
  //  Tx Hash of each step
  //
//  public Hash s1GenTxHash;
  public Hash[] s3NormalTxHashes;
  public Hash[] s4IssueTxHashes;
  public Hash[] s5TransferTxHashes;
  public Hash[] s6IssueTxHashes;

  HashMap<String, Integer> receivingTransactionCounts = new HashMap<String, Integer>();
  HashMap<String, Integer> sendingTransactionCounts = new HashMap<String, Integer>();
  public int receivingTransactionCountOf(String account) {
    return receivingTransactionCounts.get(account);
  }
  public int sendingTransactionCountOf(String account) {
    return sendingTransactionCounts.get(account);
  }


  private NewOutput markerOutput(int[] quantities, byte[] definitionPointer) throws OapException {
    NewOutput markerOutput = new NewOutput(
      CoinAmount.from(0L),
      ParsedPubKeyScript.from(OapMarkerOutput.lockingScriptFrom(quantities, definitionPointer))
    );
    return markerOutput;
  }

  private List<OutputWithOutPoint> toOuputWithOutPointList(Pair<OutPoint, TransactionOutput>... outPointsAndOutpus) {
    List<OutputWithOutPoint> result = new ArrayList<OutputWithOutPoint>();

    for (Pair<OutPoint, TransactionOutput> pair : outPointsAndOutpus) {
      result.add(new OutputWithOutPoint(pair.getSecond(), pair.getFirst()));
    }

    return result;
  }

  private Hash getTxHash(Transaction tx) {
    return HashCalculator.transactionHash(tx);
  }

  //
  // Send signed transaction to blockchain
  //
  public Hash sendTransaction(Transaction tx) {
    Hash hash = getTxHash(tx);
    RpcSubSystem.get().sendRawTransaction(tx, true);
    return hash;
  }

  public SignedTransaction signTransaction(Transaction transaction, List<PrivateKey> privateKeysOption) {
    return wallet.signTransaction(db(), transaction, blockChainView(), new ArrayList(), privateKeysOption, SigHash.ALL);
  }

  public TransactionWithName generationTransaction(String name, CoinAmount amount, OutputOwnership generatedBy) {
    String data = "Random:" + (random.nextLong()) + ".The scalable crypto-currency, ScaleChain by Kwanho, Chanwoo, Kangmo.";
    TransactionBuilder builder = TransactionBuilder.newBuilder();
    Transaction transaction = builder
      .addGenerationInput(new CoinbaseData(new Bytes(data.getBytes())), 0)
      .addOutput(new CoinAmount(BigDecimal.valueOf(50)), generatedBy)
      .build();
    TransactionWithName transactionWithName = new TransactionWithName(name, transaction);
    return transactionWithName;
  }


  public SignedTransaction signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, List<PrivateKey> privateKeysOption) throws Exception {
    TransactionBuilder builder = TransactionBuilder.newBuilder();

    for (int i = 0; i < spendingOutputs.size(); i++) {
      builder.addInput(
        db(),
        blockChainView(),
        spendingOutputs.get(i).getOutPoint()
      );
    }
    for (int i = 0; i < newOutputs.size(); i++) {
      builder.addOutput(newOutputs.get(i).getAmount(), newOutputs.get(i).getOutputOwnership());
    }
    SignedTransaction tx = signTransaction(builder.build(), privateKeysOption);
    if (!tx.getComplete()) {
      throw new Exception("Cannot sign transaction");
    }
    return tx;
  }

  public SignedTransaction signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, PrivateKey privateKey) throws Exception {
    List<PrivateKey> keys = new ArrayList<PrivateKey>();
    keys.add(privateKey);
    return signedNormalTransaction(name, spendingOutputs, newOutputs, keys);
  }

  public SignedTransaction signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs) throws Exception {
    List<PrivateKey> keys = null;
    return signedNormalTransaction(name, spendingOutputs, newOutputs, keys);
  }


  private void waitForChain(int h) {
    long blockHeight = Blockchain.get().getBestBlockHeight();
    while (true) {
      if (Blockchain.get().getBestBlockHeight() - blockHeight > h) break;
      try {
        Thread.sleep(500);
      } catch (InterruptedException ie) {
      }
    }
  }

  private UnspentCoinDescriptor[] getInternalUnspents() throws Exception {
    UnspentCoinDescriptor[] result = new UnspentCoinDescriptor[accounts().length];
    List<CoinAddress> internalAddresses = new ArrayList<CoinAddress>();
    internalAddresses.add(internalAddressData().address);

    Collection<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
      Blockchain.get().getDb(),
      Blockchain.get(),
      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
      internalAddresses
    );

    int index = 0;
    for (UnspentCoinDescriptor unspent : unspents) {
      if (index == result.length) break;
      if (unspent.getAmount().longValue() == 50) {
        result[index] = unspent;
        index++;
      }
    }
    if (index < result.length) throw new Exception("Internal Address doesn't have enough coins");

    return result;
  }

  public void populate() throws Exception {
    s3NormalTxHashes = new Hash[accounts().length];
    s4IssueTxHashes = new Hash[s3NormalTxHashes.length];
    s5TransferTxHashes = new Hash[s3NormalTxHashes.length];
    s6IssueTxHashes = new Hash[s3NormalTxHashes.length];
    int[][] transactionCounts = new int[s3NormalTxHashes.length][];
    for(int i = 0;i < transactionCounts.length;i++) {
      transactionCounts[i] = new int[2];
    }

    int assetQuntity = 10000;

    // CREATE ACCOUNT AND ADRESSES
    addressDataProvider.generateAccountsAndAddresses();

    TransactionOutput[] outputs = new TransactionOutput[accounts().length];
    OutPoint[] outPoints = new OutPoint[accounts().length];
    TransactionOutput[] changeOutputs = new TransactionOutput[accounts().length];
    OutPoint[] changeOutPoints = new OutPoint[accounts().length];

    // SEND 49BTC TO RECEVING ADDRESS OF EACH ACOUNT
    UnspentCoinDescriptor[] internalUnspents = getInternalUnspents();
    int index = 0;
    for (String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      SignedTransaction tx = null;
      tx = signedNormalTransaction(
        "S2_NORMALTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(new Pair<OutPoint, TransactionOutput>(
          new OutPoint(internalUnspents[index].getTxid(), internalUnspents[index].getVout()),
          new TransactionOutput(
            new CoinAmount(internalUnspents[index].getAmount()).coinUnits(),
            CoinAddress.from(internalUnspents[index].getAddress()).lockingScript()
          )
        )),
        NewOutListConveter.toList(
          new NewOutput(
            new CoinAmount(BigDecimal.valueOf(49)),  // SEND 49BTC, Change will be 1BTC!
            lastAddress.address
          )
        )
      );
      if (tx == null) throw new Exception(internalAccount() + "does not have enough coins.");
      Hash hash = sendTransaction(tx.getTransaction());
      // Save the tx hash
      outputs[index] = tx.getTransaction().getOutputs().get(0);
      outPoints[index] = new OutPoint(hash, 0);
      // ADD balance to account
      addBalance(account, lastAddress.address.base58(), BigDecimal.valueOf(49));
      transactionCounts[index][1]++;  // increase receiving transactions.

      index++;
    }

    // Wait for transaction included in chain.
    waitForChain(1);

    TransactionOutput[] s3Outputs = new TransactionOutput[accounts().length];
    OutPoint[] s3OOutPoints = new OutPoint[accounts().length];

    // STEP 3 : Transfer coins from last receving address to first watch-only address
    index = 0;
    Option<List<PrivateKey>> privateKeyNone = Option.empty();
    for (String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long amount = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * ((1 + index) * 5);
      long changeAmount = outputs[index].getValue() - amount - IOapConstants.DEFAULT_FEES_IN_SATOSHI;

      SignedTransaction tx = signedNormalTransaction(
        "S3_NORMALTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(new Pair<OutPoint, TransactionOutput>(outPoints[index], outputs[index])),
        NewOutListConveter.toList(
          new NewOutput(
            CoinAmount.from(amount),
            firstAddress.address
          ),
          new NewOutput(
            CoinAmount.from(changeAmount),
            lastAddress.address
          )    //CHANGE
        )
      );
      // Send signed transaction
      Hash hash = sendTransaction(tx.getTransaction());
      s3NormalTxHashes[index] = hash;
      s3Outputs[index] = tx.getTransaction().getOutputs().get(0);
      s3OOutPoints[index] = new OutPoint(hash, 0);
      changeOutputs[index] = tx.getTransaction().getOutputs().get(1);
      changeOutPoints[index] = new OutPoint(hash, 1);

      // substract the tx fee
      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, lastAddress.address.base58(), CoinAmount.from(amount).getValue());
      addBalance(account, firstAddress.address.base58(), CoinAmount.from(amount).getValue());

      transactionCounts[index][1] += 2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
    }

    // Wait for transaction included in chain.
    waitForChain(1);

    // STEP 4 : Issue Assets from the last address of each account
    index = 0;
    for (String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];

      long changeAmount = changeOutputs[index].getValue() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      SignedTransaction tx = signedNormalTransaction(
        "S4_ISSUETX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          new Pair<OutPoint, TransactionOutput>(changeOutPoints[index], changeOutputs[index])
        ),
        NewOutListConveter.toList(
          new NewOutput( // INDEX : 0 ISSUE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          markerOutput( // INDEX : 1MARKER OUTPUT
            new int[]{assetQuntity}, lastAddress.assetDefinitionPointer.getPointer()
          ),
          new NewOutput( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            lastAddress.address
          )
        )
      );
      // Send signed Tx
      Hash hash = sendTransaction(tx.getTransaction());
      s4IssueTxHashes[index] = hash;
      // Store the issue output into outputs
      outputs[index] = tx.getTransaction().getOutputs().get(0);
      outPoints[index] = new OutPoint(hash, 0);
      // Store the coin change output into change output
      changeOutputs[index] = tx.getTransaction().getOutputs().get(2);
      changeOutPoints[index] = new OutPoint(hash, 2);

      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
//      substractBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
//      addBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      addAssetBalance(account, lastAddress.assetAddress.base58(), lastAddress.assetId.base58(), assetQuntity);

      transactionCounts[index][1]+=2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
    }

    // Wait for transaction included in chain.
    waitForChain(1);

    // STEP 5 : Transfer Issue Assets
    //     from the last address of each account
    //     to the last address of next account.
    index = 0;
    for (String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long changeAmount = (outputs[index].getValue() + changeOutputs[index].getValue()) - (IOapConstants.DUST_IN_SATOSHI * 2 + IOapConstants.DEFAULT_FEES_IN_SATOSHI);

      int transferQuantity = 2000 * (index + 1);
      int changeQuantity = assetQuntity - transferQuantity;

      // CREATE TRANSFER
      SignedTransaction tx = signedNormalTransaction(
        "S5_TRANSFERTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          // outPoints/outputs : ISSUE OUTPUT, changeoutPoints/changeOutputs : COIN CHANGE OUTPUT
          new Pair<OutPoint, TransactionOutput>(outPoints[index], outputs[index]),
          new Pair<OutPoint, TransactionOutput>(changeOutPoints[index], changeOutputs[index])
        ),
        NewOutListConveter.toList(
          markerOutput( // MARKER OUTPUT
            new int[]{transferQuantity, changeQuantity}, new byte[0]
          ),
          new NewOutput( // TRANSFER ASSET
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            firstAddress.address
          ),
          new NewOutput( // ASSET CHANGE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          new NewOutput( // COIN CHANGE
            CoinAmount.from(changeAmount),
            lastAddress.address
          )
        )
      );
      // Send signed Tx
      Hash hash = sendTransaction(tx.getTransaction());
      s5TransferTxHashes[index] = hash;

      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      substractAssetBalance(account, lastAddress.assetAddress.base58(), lastAddress.assetId.base58(), transferQuantity);

      addBalance(account, firstAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      addAssetBalance(account, firstAddress.assetAddress.base58(), lastAddress.assetId.base58(), transferQuantity);

      transactionCounts[index][1]+=3;  // increase receiving transactions.
      transactionCounts[index][0]+=2;  // increase sending transactions.

      index++;
    }

    // Wait for transaction included in chain.
    waitForChain(1);

    // STEP 6 : Issue Assets from the first address of each account to last address of each account
    index = 0;
    for (String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData firstAddress = data[0];
      AddressData lastAddress = data[data.length - 1];

      long changeAmount = s3Outputs[index].getValue() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      SignedTransaction tx = signedNormalTransaction(
        "S6_ISSUETX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          new Pair<OutPoint, TransactionOutput>(s3OOutPoints[index], s3Outputs[index])
        ),
        NewOutListConveter.toList(
          new NewOutput( // INDEX : 0 ISSUE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          markerOutput( // INDEX : 1MARKER OUTPUT
            new int[]{assetQuntity}, lastAddress.assetDefinitionPointer.getPointer()
          ),
          new NewOutput( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            firstAddress.address
          )
        ),
        firstAddress.privateKey
      );

      // Send signed Tx
      Hash hash = sendTransaction(tx.getTransaction());
      s6IssueTxHashes[index] = hash;

      substractBalance(account, firstAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, firstAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);

      addBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      addAssetBalance(account, lastAddress.assetAddress.base58(), firstAddress.assetId.base58(), assetQuntity);

      transactionCounts[index][1]+=2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
    }

    for(int i= 0;i < transactionCounts.length;i++) {
      receivingTransactionCounts.put(accounts()[i], transactionCounts[i][0]);
      sendingTransactionCounts.put(accounts()[i], transactionCounts[i][1]);
    }

    // Wait for transaction included in chain.
    waitForChain(1);
  }
}
