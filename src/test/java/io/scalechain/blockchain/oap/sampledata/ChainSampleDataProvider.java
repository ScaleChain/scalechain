package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.chain.*;
import io.scalechain.blockchain.oap.blockchain.mockup.TestBlockChainView;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.script.HashSupported;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.blockchain.transaction.*;
import io.scalechain.util.ByteArray;
import scala.Option;
import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;

import java.util.Random;

/**
 * Created by shannon on 17. 1. 5.
 */
public class ChainSampleDataProvider implements IAddressGenerationListener, IAddressDataProvider {
  Random random = new Random();
  ChainEnvironment env;
  TestBlockChainView chainView;
  public TestBlockChainView blockChainView() {
    return chainView;
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

  KeyValueDatabase db;
  Option<ChainEventListener> chainEventListener;
  String testName;

//  public TransactionDataProvider(Blockchain chain, Wallet wallet, AddressDataProvider addressDataProvider) {

  protected ChainSampleDataProvider(String testName, Option<ChainEventListener> chainEventListener, AddressDataProvider addressDataProvider, KeyValueDatabase db) {
    env = ChainEnvironment$.MODULE$.get();
    this.db = db;
    this.chainView = TestBlockChainView.create(db);
    this.chainEventListener = chainEventListener;
    this.addressDataProvider = addressDataProvider;
    this.addressDataProvider.addListener(this);
    this.testName = testName;
  }

  // BlockBuildingTestTrait.sala
  // val availableOutputs = new TransactionOutputSet()
  TransactionOutputSet availableOutputs() {
    return chainView.availableOutputs;
  }

  // BlockBuildingTestTrait.sala
  //  /** Add all outputs in a transaction into an output set.
  //   *
  //   * @param outputSet The output set where each output of the given transaction is added.
  //   * @param transactionWithName The transaction that has outputs to be added to the set.
  //   */
  //  def addTransaction(outputSet : TransactionOutputSet, transactionWithName : TransactionWithName ) : Unit = {
  //    val transactionHash = getTxHash(transactionWithName)
  //    var outputIndex = -1
  //
  //    transactionWithName.transaction.outputs foreach { output =>
  //      outputIndex += 1
  //      outputSet.addTransactionOutput( OutPoint(transactionHash, outputIndex), output )
  //    }
  //  }
  // ChainSampleData.scala
  //  /** Add all outputs in a transaction into an output set.
  //   *
  //   * @param outputSet The output set where each output of the given transaction is added.
  //   * @param transactionWithName The transaction that has outputs to be added to the set.
  //   */
  //  override def addTransaction(outputSet : TransactionOutputSet, transactionWithName : TransactionWithName ) : Unit = {
  //    super.addTransaction(outputSet, transactionWithName)
  //
  //    val transactionHash = getTxHash(transactionWithName)
  //    blockIndex.addTransaction( transactionHash, transactionWithName.transaction)
  //    chainEventListener.map(_.onNewTransaction(transactionHash, transactionWithName.transaction, None, None))
  //    //println(s"transaction(${transactionWithName.name}) added : ${transactionHash}")
  //  }
  public void addTransaction(TransactionOutputSet outputSet, TransactionWithName transactionWithName) {
    Hash txHash = getTxHash(transactionWithName);
    for (int i = 0; i < transactionWithName.transaction().outputs().size(); i++) {
      outputSet.addTransactionOutput(OutPoint.apply(txHash, i), transactionWithName.transaction().outputs().apply(i));
    }
    chainView.blockIndex.addTransaction(txHash, transactionWithName.transaction());
    if (chainEventListener.isDefined()) {
      chainEventListener.get().onNewTransaction(txHash, transactionWithName.transaction(), Option.empty(), Option.empty(), db);
    }
  }

  // BlockBuildingTestTrait.sala
  //  def getTxHash(transactionWithName : TransactionWithName) = transactionWithName.transaction.hash
  //  def getBlockHash(block : Block) = block.header.hash
  public Hash getTxHash(TransactionWithName transactionWithName) {
    return HashSupported.toHashSupportedTransaction(transactionWithName.transaction()).hash();
  }

  public Hash getBlockHash(Block block) {
    return HashSupported.toHashSupportedBlockHeader(block.header()).hash();
  }

  //  def addBlock(block: Block) : Unit = {
  //    val blockHeight = blockIndex.bestBlockHeight+1
  //    blockIndex.addBlock(block, blockHeight)
  //    var transactionIndex = -1;
  //    block.transactions foreach { transaction =>
  //      transactionIndex += 1
  //      chainEventListener.map(_.onNewTransaction(
  //        transaction.hash,
  //        transaction,
  //        Some( ChainBlock(blockHeight, block) ),
  //        Some( transactionIndex )
  //      ))
  //    }
  //  }
  public void addBlock(Block block) {
    int blockHeight = chainView.blockIndex.bestBlockHeight() + 1;
    chainView.blockIndex.addBlock(block, blockHeight);
    if (chainEventListener.isDefined()) {
      for (int i = 0; i < block.transactions().size(); i++) {
        Transaction tx = block.transactions().apply(i);
        Hash txHash = HashSupported.toHashSupportedTransaction(tx).hash();
        chainEventListener.get().onNewTransaction(
          txHash,
          tx,
          Option.apply(ChainBlock.apply(blockHeight, block)),
          Option.apply(i),
          db
        );
      }
    }
  }

  // ChainSampleData.scala
  //  def newBlock(transactionsWithName : List[TransactionWithName]) : Block = {
  //    val block = newBlock(blockIndex.bestBlockHash, transactionsWithName)
  //    addBlock(block)
  //    block
  //  }
  public Block newBlock(List<TransactionWithName> transactionsWithName) {
    if (transactionsWithName.size() == 0) {
      System.err.println("WARNING: No transactions to add");
      return null;
    }
    Block block = newBlock(chainView.blockIndex.bestBlockHash(), transactionsWithName);
    addBlock(block);
    return block;
  }

  public Block newBlock(TransactionWithName transactionWithName) {
    ListBuffer<TransactionWithName> buffer = new ListBuffer<TransactionWithName>();
    buffer.$plus$eq(transactionWithName);
    return newBlock(buffer.toList());
  }

  // BlockBuildingTestTrait.sala
  //  def newBlock(prevBlockHash : Hash, transactionsWithName : List[TransactionWithName]) : Block = {
  //    val builder = BlockBuilder.newBuilder()
  //
  //    transactionsWithName.map(_.transaction) foreach { transaction =>
  //      builder.addTransaction(transaction)
  //    }
  //
  //    val block = builder.build(prevBlockHash, System.currentTimeMillis() / 1000)
  //
  //    block
  //  }
  public Block newBlock(Hash prevBlockHash, List<TransactionWithName> transationsWithName) {
    BlockBuilder builder = BlockBuilder.newBuilder();
    for (int i = 0; i < transationsWithName.size(); i++) {
      builder.addTransaction(transationsWithName.apply(i).transaction());
    }

    return builder.build(
      prevBlockHash,
      System.currentTimeMillis() / 1000,
      ChainEnvironment$.MODULE$.get().DefaultBlockVersion(),
      0,
      0);
  }

  //BlockBUildingTestTrait.scala
  //  /** Create a new normal transaction
  //   *
  //   * @param spendingOutputs The list of spending outputs. These are spent by inputs.
  //   * @param newOutputs The list of newly created outputs.
  //   * @return
  //   */
  //  def normalTransaction( name : String, spendingOutputs : List[OutputWithOutPoint], newOutputs : List[NewOutput]) : TransactionWithName = {
  //    val builder = TransactionBuilder.newBuilder()
  //
  //    spendingOutputs foreach { output =>
  //      builder.addInput(availableOutputs, output.outPoint)
  //    }
  //
  //    newOutputs foreach { output =>
  //      builder.addOutput(output.amount, output.outputOwnership)
  //    }
  //
  //    val transaction = builder.build()
  //
  //    val transactionWithName = TransactionWithName(name, transaction)
  //
  //    addTransaction( availableOutputs, transactionWithName)
  //
  //    transactionWithName
  //  }
  public TransactionWithName normalTransaction(String name, List<OutputWithOutPoint> spendingOutputs, List<NewOutput> newOutputs) {
    TransactionBuilder builder = TransactionBuilder.newBuilder();

    for (int i = 0; i < spendingOutputs.size(); i++) {
      builder.addInput(
        availableOutputs(),
        spendingOutputs.apply(i).outPoint(),
        builder.addInput$default$3(),
        builder.addInput$default$4(),
        db
      );
    }
    for (int i = 0; i < newOutputs.size(); i++) {
      builder.addOutput(
        newOutputs.apply(i).amount(),
        newOutputs.apply(i).outputOwnership()
      );
    }

    TransactionWithName transactionWithName = TransactionWithName.apply(name, builder.build(builder.build$default$1(), builder.build$default$2()));
    addTransaction(availableOutputs(), transactionWithName);
    return transactionWithName;
  }

  //BlockBUildingTestTrait.scala
  //  /** Create a generation transaction
  //   *
  //   * @param amount The amount of coins to generate
  //   * @param generatedBy The OutputOwnership that owns the newly generated coin. Ex> a coin address.
  //   * @return The newly generated transaction
  //   */
  //  def generationTransaction( name : String,
  //                             amount : CoinAmount,
  //                             generatedBy : OutputOwnership
  //  ) : TransactionWithName = {
  //    val transaction = TransactionBuilder.newBuilder()
  //      // Need to put a random number so that we have different transaction id for the generation transaction.
  //      .addGenerationInput(CoinbaseData(s"Random:${Random.nextLong}.The scalable crypto-currency, ScaleChain by Kwanho, Chanwoo, Kangmo.".getBytes))
  //      .addOutput(CoinAmount(50), generatedBy)
  //      .build()
  //    val transactionWithName = TransactionWithName(name, transaction)
  //    addTransaction( availableOutputs, transactionWithName)
  //    transactionWithName
  //  }
  public TransactionWithName generationTransaction(String name, CoinAmount amount, OutputOwnership generatedBy) {
    String data = "Random:" + (random.nextLong()) + ".The scalable crypto-currency, ScaleChain by Kwanho, Chanwoo, Kangmo.";
    TransactionBuilder builder = TransactionBuilder.newBuilder();
    Transaction transaction = builder
      .addGenerationInput(CoinbaseData.apply(ByteArray.apply(data.getBytes())), builder.addGenerationInput$default$2())
      .addOutput(CoinAmount.apply(scala.math.BigDecimal.valueOf(50)), generatedBy)
      .build(builder.build$default$1(), builder.build$default$2());
    TransactionWithName transactionWithName = TransactionWithName.apply(name, transaction);

    addTransaction(availableOutputs(), transactionWithName);

    return transactionWithName;
  }



  public void onAddressGeneration(String account, CoinAddress address, PrivateKey privateKey) {
//    System.out.println("Address generated: account=" + account + ", address=" + address.base58());
  }
//
//  public AddressData[] getnerateAccount(String account, int size) throws OapException {
//    AddressData[] result = new AddressData[size];
//    for (int i = 0; i < size; i++) {
//      result[i] = generateAddress();
//      onAddressGeneration(account, result[i].address,  (i == size - 1) ? result[i].privateKey : null);
//    }
//    return result;
//  }
//
//  public AddressData generateAddress() throws OapException {
//    PrivateKey privateKey = PrivateKey.generate();
//    CoinAddress address = CoinAddress.from(privateKey);
//    return new AddressData(privateKey, PublicKey.from(privateKey), ParsedPubKeyScript.from(privateKey), address, AssetAddress.fromCoinAddress(address), AssetId.from(address));
//  }
//
//  private String internalAccount = "_FOR_TEST_ONLY";
//  private AddressData internalAddressData;
//  public String internalAccount() {
//    return internalAccount;
//  }
//  public AddressData internalAddressData() {
//    return internalAddressData;
//  }
//
//  public String ACCOUNT_IMPORTER = "IMPORTER";
//  public String ACCOUNT_SENDER   = "SENDER";
//  public String ACCOUNT_RECEIVER = "RECEIVER";
//
//  private String[] accounts = {
//    ACCOUNT_IMPORTER,
//    ACCOUNT_SENDER,
//    ACCOUNT_RECEIVER
//  };
//
//  public String[] accounts() {
//    return accounts;
//  }
//
//  private Map<String, AddressData[]> accountAddressData = new HashMap<String, AddressData[]>();
//  private HashMap<String, AddressData> addressAndaddressData = new HashMap<String, AddressData>();
//  public AddressData[] accountAddresses(String account) {
//    if (account.equals(internalAccount)) return new AddressData[] { internalAddressData };
//    return accountAddressData.get(account);
//  }
//
//  public AddressData addressData(String address) {
//    return addressAndaddressData.get(address);
//  }
//
//  public void generateAccountsAndAddresses() throws OapException {
//    internalAddressData = getnerateAccount(internalAccount, 1)[0];
//    for (String account : accounts) {
//      AddressData[] data = getnerateAccount(account, 2);
//      accountAddressData.put(account, data);
//      for(AddressData d : data) {
//        addressAndaddressData.put(d.address.base58(), d);
//      }
//    }
//  }
//
}
