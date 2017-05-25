package io.scalechain.blockchain.oap.blockchain.mockup;

import io.scalechain.blockchain.chain.TestBlockIndex;
import io.scalechain.blockchain.transaction.TransactionOutputSet;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.blockchain.transaction.BlockchainView;
import io.scalechain.blockchain.transaction.ChainBlock;

import java.util.Iterator;

/**
 * Created by shannon on 17. 1. 5.
 */

// ChainSampleData.scala
//  object TestBlockchainView extends BlockchainView {
//    def getTransactionOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : TransactionOutput = {
//      availableOutputs.getTransactionOutput(outPoint)
//    }
//    def getIterator(height : Long)(implicit db : KeyValueDatabase) : Iterator[ChainBlock] = {
//      // unused.
//      assert(false)
//      null
//    }
//    def getBestBlockHeight() : Long = {
//      blockIndex.bestBlockHeight
//    }
//
//    def getTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option[Transaction] = {
//      blockIndex.getTransaction( transactionHash )
//    }
//  }

public class TestBlockChainView implements BlockchainView {
  public static TestBlockChainView create(KeyValueDatabase db) {
    return new TestBlockChainView(db);
  }

  KeyValueDatabase db;
  private TestBlockChainView(KeyValueDatabase db) {
    this.db = db;
  }

  // val availableOutputs = new TransactionOutputSet();
  public TransactionOutputSet availableOutputs = new TransactionOutputSet();

  // ChainSampleData.sacala
  //private val blockIndex = new TestBlockIndex()
  public TestBlockIndex blockIndex = new TestBlockIndex();

  @Override
  public TransactionOutput getTransactionOutput(KeyValueDatabase db, OutPoint outPoint) {
    return availableOutputs.getTransactionOutput(db, outPoint);
  }

  @Override

  public Iterator<ChainBlock> getIterator(KeyValueDatabase db, long height) {
    assert(false);
    return null;
  }

  @Override
  public long getBestBlockHeight() {
    return blockIndex.getBestBlockHeight();
  }

  @Override
  public Transaction getTransaction(KeyValueDatabase db, Hash transactionHash) {
    return blockIndex.getTransaction(db, transactionHash);
  }

  public KeyValueDatabase db() {
    return db;
  }

//  // Methods defined newly declared in IBlockchain
//  @Override
//  public Transaction getTransation(Hash hash) {
//    Option<Transaction> txOption = getTransaction(hash, db());
//    if (txOption.isDefined()) return txOption.get();
//    else return null;
//  }
//
//  @Override
//  public TransactionOutput getTransactionOutput(OutPoint outPoint) {
//    return getTransactionOutput(outPoint, db());
//  }
}
