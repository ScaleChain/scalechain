package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.chain.NewOutput;
import io.scalechain.blockchain.chain.OutputWithOutPoint;
import io.scalechain.blockchain.chain.TransactionBuilder;
import io.scalechain.blockchain.chain.TransactionWithName;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetTransfer;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.script.HashSupported;
import io.scalechain.blockchain.storage.Storage;
import io.scalechain.blockchain.storage.index.CachedRocksDatabase;
import io.scalechain.blockchain.transaction.*;
import io.scalechain.wallet.Wallet;
import org.apache.commons.io.FileUtils;
import scala.Option;
import scala.collection.JavaConverters;

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
    super(testName, Option.apply(wallet), new AddressDataProvider(null, null), new CachedRocksDatabase(blockStoragePath));
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
      wallet.importOutputOwnership(chainView, account, address, false, db);
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
      wallet.store().putOutputOwnership(account, address, db);
      List<PrivateKey> keys = new ArrayList<>();
      keys.add(privateKey);
      wallet.store().putPrivateKeys(address, JavaConverters.asScalaBuffer(keys).toList(), db);
      wallet.store().putReceivingAddress(account, address, db);
    }
  }

  public Hash getTxHash(Transaction tx) {
    return HashSupported.toHashSupportedTransaction(tx).hash();
  }


  //  def signTransaction(transaction   : Transaction,
  //                      chainView     : BlockchainView,
  //                      dependencies  : List[UnspentTransactionOutput],
  //                      privateKeys   : Option[List[PrivateKey]],
  //                      sigHash       : SigHash
  //  )(implicit db : KeyValueDatabase) : signedTx = {
  public signedTx signTransaction(Transaction transaction, Option<List<PrivateKey>> privateKeysOption) {
    Option<scala.collection.immutable.List<PrivateKey>> keysOption = Option.empty();
    if (privateKeysOption.isDefined()) {
      keysOption = Option.apply(JavaConverters.asScalaBuffer(privateKeysOption.get()).toList());
    }
    List<UnspentTransactionOutput> empty = new ArrayList<UnspentTransactionOutput>();
    return wallet.signTransaction(transaction, blockChainView(), JavaConverters.asScalaBuffer(empty).toList(), keysOption, SigHash.ALL(), blockChainView().db());
  }

  public Hash sendTransaction(signedTx tx) {
    Hash hash = getTxHash(tx.transaction());
    // CREATE A TransactionWithName.
    TransactionWithName transactionWithName = TransactionWithName.apply("SignedTx:" + hash.toHex(), tx.transaction());
    addTransaction(availableOutputs(), transactionWithName);
    // CREATE AND ADD A NEW BLOCK
    newBlock(transactionWithName);
    return hash;
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, Option<List<PrivateKey>> privateKeysOption) {
    TransactionBuilder builder = TransactionBuilder.newBuilder();

    for (int i = 0; i < spendingOutputs.size(); i++) {
      builder.addInput(
        availableOutputs(),
        spendingOutputs.get(i).outPoint(),
        builder.addInput$default$3(),
        builder.addInput$default$4(),
        db
      );
    }
    for (int i = 0; i < newOutputs.size(); i++) {
      builder.addOutput(newOutputs.get(i).amount(), newOutputs.get(i).outputOwnership());
    }
    Transaction tx = builder.build(builder.build$default$1(), builder.build$default$2());
    signedTx signedTx = signTransaction(tx, privateKeysOption);
    TransactionWithName transactionWithName = TransactionWithName.apply(name,signedTx.transaction());
    addTransaction(availableOutputs(), transactionWithName);
    return transactionWithName;
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs, PrivateKey privateKey) {
    List<PrivateKey> keys = new ArrayList<PrivateKey>();
    keys.add(privateKey);
    return signedNormalTransaction(name, spendingOutputs, newOutputs, Option.apply(keys));
  }

  public TransactionWithName signedNormalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs) {
    List<PrivateKey> keys = new ArrayList<PrivateKey>();
    return signedNormalTransaction(name, spendingOutputs, newOutputs, Option.empty());
  }


  public void doMine(CoinAddress coinAddress) {
    coinAddress = (coinAddress != null) ? coinAddress : internalAddressData().address;
    TransactionWithName transactionWithName  = generationTransaction(
      "GENTX-" + coinAddress.base58() + "-" + (System.currentTimeMillis() / 1000),
      CoinAmount.apply(scala.math.BigDecimal.valueOf(50)),
      coinAddress
    );

    newBlock(transactionWithName);
  }

  private NewOutput markerOutput(int[] quantities, byte[] definitionPointer) throws OapException {
    NewOutput markerOutput = NewOutput.apply(
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
      result.add(OutputWithOutPoint.apply(pair.getSecond(), pair.getFirst()));
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
    addBlock(env.GenesisBlock());

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
        CoinAmount.apply(scala.math.BigDecimal.valueOf(50)),
        lastAddress.address
      );

      Hash hash = HashSupported.toHashSupportedTransaction(tx.transaction()).hash();
      // CHECK
      outputs[index] = tx.transaction().outputs().apply(0);
      outPoints[index] = OutPoint.apply(hash, 0);
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
    Option<List<PrivateKey>> privateKeyNone = Option.empty();
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long amount       = IOapConstants.ONE_BTC_IN_SATOSHI.longValue() * ((1 + index) * 5);
      long changeAmount = outputs[index].value() - amount - IOapConstants.DEFAULT_FEES_IN_SATOSHI;

      TransactionWithName tx = signedNormalTransaction(
        "S3_NORMALTX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(new Pair<OutPoint, TransactionOutput>(outPoints[index], outputs[index])),
        NewOutListConveter.toList(
          NewOutput.apply(
            CoinAmount.from(amount),
            firstAddress.address
          ),
          NewOutput.apply(
            CoinAmount.from(changeAmount),
            lastAddress.address
          )    //CHANGE
        )
      );
      // CHECK
      transactionWithNames.add(tx);

      Hash hash = HashSupported.toHashSupportedTransaction(tx.transaction()).hash();
      s3NormalTxHashes[index]   = hash;
      s3Outputs[index] = tx.transaction().outputs().apply(0);
      s3OOutPoints[index] = OutPoint.apply(hash, 0);
      changeOutputs[index] = tx.transaction().outputs().apply(1);
      changeOutPoints[index] = OutPoint.apply(hash, 1);
      // substract the tx fee
      substractBalance(account, lastAddress.address.base58(), IOapConstants.DEFAULT_FEES_IN_BITCOIN);
      substractBalance(account, lastAddress.address.base58(), CoinAmount.from(amount).value().bigDecimal());
      addBalance      (account, firstAddress.address.base58(), CoinAmount.from(amount).value().bigDecimal());

      transactionCounts[index][1] += 2;  // increase receiving transactions.
      transactionCounts[index][0]++;  // increase sending transactions.

      index++;
    }
    // CREATE NEW BLOCK WITH NEW TRANSACTION
    newBlock(JavaConverters.asScalaBuffer(transactionWithNames).toList());

    // STEP 4 : Issue Assets from the last address of each account
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];

      long changeAmount = changeOutputs[index].value() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      TransactionWithName tx = signedNormalTransaction(
        "S4_ISSUETX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          new Pair<OutPoint, TransactionOutput>(changeOutPoints[index], changeOutputs[index])
        ),
        NewOutListConveter.toList(
          NewOutput.apply( // INDEX : 0 ISSUE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          markerOutput( // INDEX : 1MARKER OUTPUT
            new int[] { assetQuntity }, lastAddress.assetDefinitionPointer.getPointer()
          ),
          NewOutput.apply( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            lastAddress.address
          )
        )
      );
      // CHECK
      Hash hash = HashSupported.toHashSupportedTransaction(tx.transaction()).hash();
      s4IssueTxHashes[index] = hash;
      // Store the issue output into outputs
      outputs[index] = tx.transaction().outputs().apply(0);
      outPoints[index] = OutPoint.apply(hash, 0);
      // Store the coin change output into change output
      changeOutputs[index] = tx.transaction().outputs().apply(2);
      changeOutPoints[index] = OutPoint.apply(hash, 2);

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
    newBlock(JavaConverters.asScalaBuffer(transactionWithNames).toList());

    // STEP 5 : Transfer Issue Assets
    //     from the last address of each account
    //     to the last address of next account.
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData lastAddress = data[data.length - 1];
      AddressData firstAddress = data[0];

      long changeAmount = (outputs[index].value() + changeOutputs[index].value()) - (IOapConstants.DUST_IN_SATOSHI * 2 + IOapConstants.DEFAULT_FEES_IN_SATOSHI);

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
          NewOutput.apply( // TRANSFER ASSET
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            firstAddress.address
          ),
          NewOutput.apply( // ASSET CHANGE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          NewOutput.apply( // COIN CHANGE
            CoinAmount.from(changeAmount),
            lastAddress.address
          )
        )
      );
      Hash hash = HashSupported.toHashSupportedTransaction(tx.transaction()).hash();
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
    newBlock(JavaConverters.asScalaBuffer(transactionWithNames).toList());

    // STEP 6 : Issue Assets from the first address of each account to last address of each account
    transactionWithNames = new ArrayList<TransactionWithName>();
    index = 0;
    for(String account : accounts()) {
      AddressData[] data = addressesOf(account);
      AddressData firstAddress = data[0];
      AddressData lastAddress  = data[data.length - 1];

      long changeAmount = s3Outputs[index].value() - (IOapConstants.DUST_IN_SATOSHI + IOapConstants.DEFAULT_FEES_IN_SATOSHI);
      // CREATE ISSUE TRANSACTION.
      TransactionWithName tx = signedNormalTransaction(
        "S6_ISSUETX-" + account + "-" + (System.currentTimeMillis() / 1000),
        toOuputWithOutPointList(
          new Pair<OutPoint, TransactionOutput>(s3OOutPoints[index], s3Outputs[index])
        ),
        NewOutListConveter.toList(
          NewOutput.apply( // INDEX : 0 ISSUE OUTPUT
            CoinAmount.from(IOapConstants.DUST_IN_SATOSHI),
            lastAddress.address
          ),
          markerOutput( // INDEX : 1MARKER OUTPUT
            new int[] { assetQuntity }, lastAddress.assetDefinitionPointer.getPointer()
          ),
          NewOutput.apply( // INDEX : 2 COIN CHANGE
            CoinAmount.from(changeAmount),
            firstAddress.address
          )
        ),
        firstAddress.privateKey
      );
      // CHECK
      Hash hash = HashSupported.toHashSupportedTransaction(tx.transaction()).hash();
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
    newBlock(JavaConverters.asScalaBuffer(transactionWithNames).toList());

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
    ChainEnvironment$.MODULE$.create(envName);

    if (path == null) {
      targetPath = new File("./target");
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
//    WalletSampleDataProvider.init("testnet", new File("./target"));
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
