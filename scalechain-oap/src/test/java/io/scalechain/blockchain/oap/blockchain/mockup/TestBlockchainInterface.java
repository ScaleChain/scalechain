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
import java.util.Iterator;

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
    Transaction txOption = chainView.getTransaction(chainView.db(), hash);
    return txOption;
  }

  @Override
  public TransactionOutput getTransactionOutput(OutPoint outPoint) {
    return chainView.getTransactionOutput(chainView.db(), outPoint);
  }

  @Override
  public KeyValueDatabase db() {
    return chainView.db();
  }

  @Override
  public TransactionOutput getTransactionOutput(KeyValueDatabase db, OutPoint outPoint) {
    return chainView.getTransactionOutput(db, outPoint);
  }

  @Override
  public Iterator<ChainBlock> getIterator(KeyValueDatabase db, long height) {
    return chainView.getIterator(chainView.db(), height);
  }

  @Override
  public long getBestBlockHeight() {
    return chainView.getBestBlockHeight();
  }

  @Override
  public Transaction getTransaction(KeyValueDatabase db, Hash transactionHash) {
    return chainView.getTransaction(db, transactionHash);
  }
}
