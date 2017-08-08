package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.chain.NewOutput;
import io.scalechain.blockchain.chain.OutputWithOutPoint;
import io.scalechain.blockchain.chain.TransactionBuilder;
import io.scalechain.blockchain.chain.TransactionWithName;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import kotlin.Pair;
import io.scalechain.blockchain.oap.wallet.AssetTransfer;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.script.HashCalculator;
import io.scalechain.blockchain.storage.Storage;
import io.scalechain.blockchain.storage.index.RocksDatabase;
import io.scalechain.blockchain.transaction.*;
import io.scalechain.wallet.Wallet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shannon on 17. 1. 5.
 */
public class WalletSampleDataProvider extends ChainSampleDataProvider {
  private Wallet wallet;
  public Wallet wallet() {
    return wallet;
  }
  public WalletSampleDataProvider(String testName, Wallet wallet, File blockStoragePath) {
    super(testName, wallet, new AddressDataProvider(null, null), new RocksDatabase(blockStoragePath));
    this.wallet = wallet;
    this.dbPath = blockStoragePath;
    onAddressGeneration(super.internalAccount(), super.internalAddressData().address, super.internalAddressData().privateKey);
  }

  //WalletSampleData.scala
  //  override def onAddressGeneration(account: String, address: CoinAddress): Unit = {
  //    wallet.importOutputOwnership(TestBlockchainView, account, address, false)(db)
  //  }
  @Override
  public void onAddressGeneration(String account, CoinAddress address, PrivateKey privateKey) {
    super.onAddressGeneration(account, address, privateKey);
    if (privateKey == null) {
      wallet.importOutputOwnership(db, chainView, account, address, false);
    } else {
      // From: Wallet.newAddress()
      //      // Step 1 : Generate a random number and a private key.
      //      val privateKey : PrivateKey = PrivateKey.generate
      //      // Step 2 : Create an address.
      //      val address = CoinAddress.from(privateKey)
      //      // Step 3 : Wallet Store : Put an address into an account.
      //      store.putOutputOwnership(account, address)
      //      // Step 4 : Wallet Store : Put the private key into an the address.
      //      store.putPrivateKeys(address, List(privateKey))
      //      // Step 5 : Put the address as the receiving address of the account.
      //      store.putReceivingAddress(account, address)
      wallet.getStore().putOutputOwnership(db, account, address);
      List<PrivateKey> keys = new ArrayList<PrivateKey>();
      keys.add(privateKey);
      wallet.getStore().putPrivateKeys(db, address, keys);
      wallet.getStore().putReceivingAddress(db, account, address);
    }
  }

  public Hash getTxHash(Transaction tx) {
    return HashCalculator.transactionHash(tx);
  }


  //  def signTransaction(transaction   : Transaction,
  //                      chainView     : BlockchainView,
  //                      dependencies  : List[UnspentTransactionOutput],
  //                      privateKeys   : Option[List[PrivateKey]],
  //                      sigHash       : SigHash
  //  )(implicit db : KeyValueDatabase) : signedTx = {
  public SignedTransaction signTransaction(Transaction transaction, List<PrivateKey> privateKeysOption) {
    List<UnspentTransactionOutput> empty = new ArrayList<UnspentTransactionOutput>();
    return wallet.signTransaction(blockChainView().db(), transaction, blockChainView(), new ArrayList(), privateKeysOption, SigHash.ALL);
  }

  public Hash sendTransaction(SignedTransaction tx) {
    Hash hash = getTxHash(tx.getTransaction());
    // CREATE A TransactionWithName.
    TransactionWithName transactionWithName = new TransactionWithName("SignedTx:" + hash.toHex(), tx.getTransaction());
    addTransaction(availableOutputs(), transactionWithName);
    // CREATE AND ADD A NEW BLOCK
    newBlock(transactionWithName);
    return hash;
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, List<PrivateKey> privateKeysOption) {
    TransactionBuilder builder = TransactionBuilder.newBuilder();

    for (int i = 0; i < spendingOutputs.size(); i++) {
      builder.addInput(
        db,
        availableOutputs(),
        spendingOutputs.get(i).getOutPoint()
      );
    }
    for (int i = 0; i < newOutputs.size(); i++) {
      builder.addOutput(newOutputs.get(i).getAmount(), newOutputs.get(i).getOutputOwnership());
    }
    Transaction tx = builder.build();
    SignedTransaction signedTx = signTransaction(tx, privateKeysOption);
    TransactionWithName transactionWithName = new TransactionWithName(name,signedTx.getTransaction());
    addTransaction(availableOutputs(), transactionWithName);
    return transactionWithName;
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, PrivateKey privateKey) {
    List<PrivateKey> keys = new ArrayList<PrivateKey>();
    keys.add(privateKey);
    return signedNormalTransaction(name, spendingOutputs, newOutputs, keys);
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs) {
    List<PrivateKey> keys = null;
    return signedNormalTransaction(name, spendingOutputs, newOutputs, keys);
  }


  public void doMine(CoinAddress coinAddress) {
    coinAddress = (coinAddress != null) ? coinAddress : internalAddressData().address;
    TransactionWithName transactionWithName  = generationTransaction(
      "GENTX-" + coinAddress.base58() + "-" + (System.currentTimeMillis() / 1000),
      new CoinAmount(java.math.BigDecimal.valueOf(50)),
      coinAddress
    );

    newBlock(transactionWithName);
  }

  private NewOutput markerOutput(int[] quantities, byte[] definitionPointer) throws OapException {
    NewOutput markerOutput = new NewOutput(
      CoinAmount.from(0L),
      ParsedPubKeyScript.from(OapMarkerOutput.lockingScriptFrom(quantities, definitionPointer))
    );
    return markerOutput;
  }

  private static class ListConverter<T> {
    public List<T> toList(T... elements) {
      List<T> result = new ArrayList<T>();
      for(T e : elements) {
        result.add(e);
      }
      return result;
    }
  }

  private List<OutputWithOutPoint> toOuputWithOutPointList(Pair<OutPoint, TransactionOutput>... outPointsAndOutpus) {
    List<OutputWithOutPoint> result = new ArrayList<OutputWithOutPoint>();

    for(Pair<OutPoint, TransactionOutput> pair : outPointsAndOutpus) {
      result.add(new OutputWithOutPoint(pair.getSecond(), pair.getFirst()));
    }

    return result;
  }

  //
  //  FOR USE IN TEST CASES.
  //

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

  public void populate() throws Exception {
    s3NormalTxHashes   = new Hash[accounts().length];
    s4IssueTxHashes    = new Hash[s3NormalTxHashes.length];
    s5TransferTxHashes = new Hash[s3NormalTxHashes.length];;
    s6IssueTxHashes    = new Hash[s3NormalTxHashes.length];;
    int[][] transactionCounts = new int[s3NormalTxHashes.length][];
    for(int i = 0;i < transactionCounts.length;i++) {
      transactionCounts[i] = new int[2];
    }


    ListConverter<NewOutput> NewOutListConveter = new ListConverter<NewOutput>();
    ListConverter<AssetTransfer> AssetTransferListConverter = new ListConverter<AssetTransfer>();

    // Put genesis block.
    addBlock(env.getGenesisBlock());

    // CREATE ACCOUNT AND ADRESSES
    addressDataProvider.generateAccountsAndAddresses();

    // STEP 1 : Mine coins and give them to internalAddress
    doMine(null);
    addBalance(internalAccount(), internalAddressData().address.base58(), BigDecimal.valueOf(50));

    int assetQuntity = 10000;
    TransactionOutput output;
    // STEP 2 : Mine coins and give them to each account
    //   The last address of an account is not watch-only adddress.
    List<TransactionWithName> transactionWithNames = new ArrayList<TransactionWithName>();
    TransactionOutput[] outputs   = new TransactionOutput[accounts().length];
    OutPoint[]          outPoints = new OutPoint[accounts().length];
    TransactionOutput[] changeOutputs  = new TransactionOutput[accounts().length];
    OutPoint[]          changeOutPoints = new OutPoint[accounts().length];
    int index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      TransactionWithName tx  = generationTransaction(
        "S2_GENTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        new CoinAmount(java.math.BigDecimal.valueOf(50)),
        lastAddress.address
      );

      Hash hash = HashCalculator.transactionHash(tx.getTransaction());
      // CHECK
      outputs[index] = tx.getTransaction().getOutputs().get(0);
      outPoints[index] = new OutPoint(hash, 0);
      // ADD balance to account
      addBalance(account, lastAddress.address.base58(), BigDecimal.valueOf(50));
      transactionCounts[index][1]++;  // increase receiving transactions.

      index++;
      newBlock(tx);
    }

    TransactionOutput[] s3Outputs   = new TransactionOutput[accounts().length];
    OutPoint[]          s3OOutPoints = new OutPoint[accounts().length];

    // STEP 3 : Transfer coins from last non watch-only address to first watch-only address
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long amount       = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * ((1 + index) * 5);
      long changeAmount = outputs[index].getValue() - amount - IOapConstants.DEFAULT_FEES_IN_SATOSHI;

      TransactionWithName tx = signedNormalTransaction(
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
      // CHECK
      transactionWithNames.add(tx);

      Hash hash = HashCalculator.transactionHash(tx.getTransaction());
      s3NormalTxHashes[index]   = hash;
      s3Outputs[index] = tx.getTransaction().getOutputs().get(0);
      s3OOutPoints[index] = new OutPoint(hash, 0);
      changeOutputs[index] = tx.getTransaction().getOutputs().get(1);
      changeOutPoints[index] = new OutPoint(hash, 1);
      // substract the tx fee
      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, lastAddress.address.base58(), CoinAmount.from(amount).getValue());
      addBalance      (account, firstAddress.address.base58(), CoinAmount.from(amount).getValue());

      transactionCounts[index][1] += 2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
    }
    // CREATE NEW BLOCK WITH NEW TRANSACTION
    newBlock(transactionWithNames);

    // STEP 4 : Issue Assets from the last address of each account
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];

      long changeAmount = changeOutputs[index].getValue() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      TransactionWithName tx = signedNormalTransaction(
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
            new int[] { assetQuntity }, lastAddress.assetDefinitionPointer.getPointer()
          ),
          new NewOutput( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            lastAddress.address
          )
        )
      );
      // CHECK
      Hash hash = HashCalculator.transactionHash(tx.getTransaction());
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
      addAssetBalance (account, lastAddress.assetAddress.base58(), lastAddress.assetId.base58(), assetQuntity);

      transactionCounts[index][1]+=2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
      transactionWithNames.add(tx);
    }

    // CREATE NEW BLOCK WITH NEW TRANSACTION
    newBlock(transactionWithNames);

    // STEP 5 : Transfer Issue Assets
    //     from the last address of each account
    //     to the last address of next account.
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long changeAmount = (outputs[index].getValue() + changeOutputs[index].getValue()) - (IOapConstants.DUST_IN_SATOSHI * 2 + IOapConstants.DEFAULT_FEES_IN_SATOSHI);

      int transferQuantity = 2000 * (index + 1);
      int changeQuantity = assetQuntity - transferQuantity;

      // CREATE TRANSFER
      TransactionWithName tx = signedNormalTransaction(
        "S5_TRANSFERTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          // outPoints/outputs : ISSUE OUTPUT, changeoutPoints/changeOutputs : COIN CHANGE OUTPUT
          new Pair<OutPoint, TransactionOutput>(outPoints[index], outputs[index]),
          new Pair<OutPoint, TransactionOutput>(changeOutPoints[index], changeOutputs[index])
        ),
        NewOutListConveter.toList(
          markerOutput( // MARKER OUTPUT
            new int[] { transferQuantity, changeQuantity }, new byte[0]
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
      Hash hash = HashCalculator.transactionHash(tx.getTransaction());
      s5TransferTxHashes[index] = hash;

      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
//      substractBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
//      addBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      substractAssetBalance(account, lastAddress.assetAddress.base58(), lastAddress.assetId.base58(), transferQuantity);

      addBalance(account, firstAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      addAssetBalance(account, firstAddress.assetAddress.base58(), lastAddress.assetId.base58(), transferQuantity);

      transactionCounts[index][1]+=3;  // increase receiving transactions.
      transactionCounts[index][0]+=2;  // increase sending transactions.

      index++;
      transactionWithNames.add(tx);
    }
    // CREATE NEW BLOCK WITH NEW TRANSACTION
    newBlock(transactionWithNames);

    // STEP 6 : Issue Assets from the first address of each account to last address of each account
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData firstAddress = data[0];
      AddressData lastAddress  = data[data.length - 1];

      long changeAmount = s3Outputs[index].getValue() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      TransactionWithName tx = signedNormalTransaction(
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
            new int[] { assetQuntity }, lastAddress.assetDefinitionPointer.getPointer()
          ),
          new NewOutput( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            firstAddress.address
          )
        ),
        firstAddress.privateKey
      );
      // CHECK
      Hash hash = HashCalculator.transactionHash(tx.getTransaction());
      s6IssueTxHashes[index]    = hash;

      substractBalance(account, firstAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, firstAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);

      addBalance(account, lastAddress.address.base58(), IOapConstants.DUST_IN_BITCOIN);
      addAssetBalance (account, lastAddress.assetAddress.base58(), firstAddress.assetId.base58(), assetQuntity);

      transactionCounts[index][1]+=2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
      transactionWithNames.add(tx);
    }
    // CREATE NEW BLOCK WITH NEW TRANSACTION
    newBlock(transactionWithNames);

    for(int i= 0;i < transactionCounts.length;i++) {
      receivingTransactionCounts.put(accounts()[i], transactionCounts[i][0]);
      sendingTransactionCounts.put(accounts()[i], transactionCounts[i][1]);
    }

  }

  protected void dispose() {
    this.db.close();

    try {
      FileUtils.deleteDirectory(dbPath);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  static File targetPath = null;
  File dbPath = null;
  public static void init(String envName, File path) {
    if (envName == null) envName = "testnet";
    ChainEnvironment.create(envName);

    if (path == null) {
      targetPath = new File("./build");
    } else {
      targetPath = path;
    }
    if (!targetPath.exists()) {
      targetPath.mkdir();
    }

    Storage.initialize();
  }

  public static synchronized WalletSampleDataProvider create(String testName) throws Exception {
    File blockStoragePath = new File(targetPath, "blockstorage-" + testName);
    if (blockStoragePath.exists()) {
      try { FileUtils.deleteDirectory(blockStoragePath); } catch (IOException e) { e.printStackTrace(); }
    }
    Wallet wallet = Wallet.create();
    WalletSampleDataProvider provider = new WalletSampleDataProvider(testName, wallet, blockStoragePath);
    provider.populate();
    return provider;
  }

  public static void destroy(WalletSampleDataProvider provider) {
    provider.dispose();
  }

//  // TEST RUN...
//  public static void main(String[] args) throws Exception {
//    WalletSampleDataProvider.init("testnet", new File("./build"));
//
//    WalletSampleDataProvider provider = WalletSampleDataProvider.create("mock");
//    String account = "IMPORTER";
//    CoinAddress importAddress = Wallet.get().getReceivingAddress("IMPORTER", provider.db);
//    for( OutputOwnership o : JavaConverters.asJavaCollection(Wallet.get().store().getOutputOwnerships(Option.apply("IMPORTER"), provider.blockChainView().db()))) {
//      System.out.println(o);
//    };
//    System.out.println("Receiving address of IMPORTER=" + importAddress.stringKey());
//    System.out.println("Transactions of " + account);;
//    scala.collection.immutable.List<WalletTransactionDescriptor>  transactionDescriptors= Wallet.get().listTransactions(provider.blockChainView(), Option.apply(account), 10, 0, true, provider.blockChainView().db());
//    for(int i = 0;i < transactionDescriptors.size();i++) {
//      System.out.println(transactionDescriptors.apply(i));
//    }
//    System.out.println("UnspentCoins of "+account);
//    List<CoinAddress> list = new ArrayList<CoinAddress>();
//    list.add(provider.addressesOf(account)[1].address);
//    scala.collection.immutable.List<CoinAddress> coinAddresses = JavaConverters.asScalaBuffer(list).toList();
//    scala.collection.immutable.List<UnspentCoinDescriptor> unspents = Wallet.get().listUnspent(
//      provider.blockChainView(),
//      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
//      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
//      Option.apply(coinAddresses),
//      provider.blockChainView().db()
//    );
//    for(int i = 0;i < unspents.size();i++) {
//      UnspentCoinDescriptor desc = unspents.apply(i);
//      System.out.println("TransactionDescriptor==>" + desc);
//    }
//    WalletSampleDataProvider.destroy(provider);
//
//    // RUN SAME THING AGAIN:
//
//    provider = WalletSampleDataProvider.create("mock-test2");
//    account = "IMPORTER";
//    importAddress = Wallet.get().getReceivingAddress("IMPORTER", provider.db);
//    System.out.println("Receiving address of IMPORTER=" + importAddress.stringKey());
//    System.out.println("Transactions of " + account);;
//    transactionDescriptors= Wallet.get().listTransactions(provider.blockChainView(), Option.apply(account), 10, 0, true, provider.blockChainView().db());
//    for(int i = 0;i < transactionDescriptors.size();i++) {
//      System.out.println(transactionDescriptors.apply(i));
//    }
//    System.out.println("UnspentCoins of the receiving address of "+ account);
//    list = new ArrayList<CoinAddress>();
//    list.add(provider.addressesOf(account)[1].address);
//    coinAddresses = JavaConverters.asScalaBuffer(list).toList();
//    unspents = Wallet.get().listUnspent(
//      provider.blockChainView(),
//      IOapConstants.DEFAULT_MIN_CONFIRMATIONS,
//      IOapConstants.DEFAULT_MAX_CONFIRMATIONS,
//      Option.apply(coinAddresses),
//      provider.blockChainView().db()
//    );
//    for(int i = 0;i < unspents.size();i++) {
//      UnspentCoinDescriptor desc = unspents.apply(i);
//      System.out.println("UnspentCoinDescriptor==>" + desc);
//    }
//    WalletSampleDataProvider.destroy(provider);
// }
}
