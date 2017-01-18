package io.scalechain.blockchain.oap.blockchain.bitcoin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.blockchain.IBlockchainInterface;
import io.scalechain.blockchain.oap.env.OpenAssetsProtocolEnv;
import io.scalechain.blockchain.oap.rpc.RpcInvoker;
import io.scalechain.blockchain.oap.transaction.TransactionCodec;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.storage.index.KeyValueDatabase;
import io.scalechain.util.HexUtil;

/**
 * Interface calss to bitcoin block chain api used in OAP.
 *
 * Created by shannon on 16. 11. 29.
 */
public class BitcoinBlockchainInterface implements IBlockchainInterface {
  @Override
  public Transaction getTransation(Hash txId) {
        /*
         * CALL bitcoin rpc getrawtransaction txid 0|1
         *
         */
    JsonArray params = new JsonArray();
    params.add(HexUtil.hex(txId.value().array(), HexUtil.hex$default$2()));

    JsonObject response = RpcInvoker.invoke(
      OpenAssetsProtocolEnv.getHost(),
      OpenAssetsProtocolEnv.getPort(),
      "getrawtransaction",
      params,
      OpenAssetsProtocolEnv.getUser(),
      OpenAssetsProtocolEnv.getPassword()
    );
    JsonElement eResult = response.get("result");
    if (eResult == null || eResult instanceof JsonNull) return null;
    String txHex = eResult.getAsString();
    if (txHex != null && txHex.length() > 0) {
      return TransactionCodec.decode(eResult.getAsString());
    } else return null;
  }

  @Override
  public TransactionOutput getTransactionOutput(OutPoint outPoint) {
    Transaction tx = getTransation(outPoint.transactionHash());
    if (tx == null) return null;
    return tx.outputs().apply(outPoint.outputIndex());
  }

  @Override
  public KeyValueDatabase db() {
    return null;
  }

//
//  @Override
//  public TransactionOutput getTransactionOutput(OutPoint outPoint, KeyValueDatabase db) {
//    return getTransactionOutput(outPoint);
//  }
//
//  @Override
//  public Iterator<ChainBlock> getIterator(long height, KeyValueDatabase db) {
//    return null;
//  }
//
//  @Override
//  public long getBestBlockHeight() {
//    return 0;
//  }
//
//  @Override
//  public Option<Transaction> getTransaction(Hash transactionHash, KeyValueDatabase db) {
//    Transaction tx = getTransation(transactionHash);
//    if (tx == null) return Option.empty();
//    else return Option.apply(tx);
//  }
}

