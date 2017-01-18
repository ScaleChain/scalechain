package io.scalechain.blockchain.oap.blockchain.mockup;

import io.scalechain.blockchain.oap.blockchain.IBlockchainInterface;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.blockchain.transaction.BlockchainView;
import io.scalechain.blockchain.transaction.ChainBlock;
import io.scalechain.blockchain.transaction.CoinsView;
import scala.Option;
import scala.collection.Iterator;

/**
 * Created by shannon on 16. 12. 8.
 */
public class TestBlockchainInterface implements IBlockchainInterface, BlockchainView {

  WalletSampleDataProvider dataProvider;
  TestBlockChainView chainView;
  public TestBlockchainInterface(WalletSampleDataProvider provider) {
    this.dataProvider = provider;
    this.chainView = provider.blockChainView();
  }

  public CoinsView getCoinsView() {
    return chainView;
  }
  public BlockchainView getChainView() {
    return chainView;
  }

  @Override
  public Transaction getTransation(Hash hash) {
    Option<Transaction> txOption = chainView.getTransaction(hash, chainView.db());
    if (txOption.isDefined()) return txOption.get();
    else return null;
  }

  @Override
  public TransactionOutput getTransactionOutput(OutPoint outPoint) {
    return chainView.getTransactionOutput(outPoint, chainView.db());
  }

  @Override
  public KeyValueDatabase db() {
    return chainView.db();
  }

  @Override
  public TransactionOutput getTransactionOutput(OutPoint outPoint, KeyValueDatabase db) {
    return chainView.getTransactionOutput(outPoint, db);
  }

  @Override
  public Iterator<ChainBlock> getIterator(long height, KeyValueDatabase db) {
    return chainView.getIterator(height, chainView.db());
  }

  @Override
  public long getBestBlockHeight() {
    return chainView.getBestBlockHeight();
  }

  @Override
  public Option<Transaction> getTransaction(Hash transactionHash, KeyValueDatabase db) {
    return chainView.getTransaction(transactionHash, db);
  }
}
