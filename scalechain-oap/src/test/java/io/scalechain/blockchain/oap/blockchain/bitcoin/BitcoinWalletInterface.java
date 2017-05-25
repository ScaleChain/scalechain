package io.scalechain.blockchain.oap.blockchain.bitcoin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.rpc.RpcInvoker;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.oap.env.OpenAssetsProtocolEnv;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.UnspentCoinDescriptor;
import io.scalechain.wallet.WalletTransactionDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface class to bitcoin wallet api used in OAP.
 * Created by shannon on 16. 11. 28.
 */
public class BitcoinWalletInterface implements IWalletInterface {

  private static BitcoinWalletInterface instance;

  public static BitcoinWalletInterface get() {
    if (instance == null) {
      instance = new BitcoinWalletInterface();
    }
    return instance;
  }

  @Override
  public List<UnspentCoinDescriptor> listUnspent(
    long minConfirmations,
    long maxConfirmations,
    List<CoinAddress> addresses
  ) {
    JsonArray params = new JsonArray();
    params.add(minConfirmations);
    params.add(maxConfirmations);
    if (addresses != null && addresses.size() > 0) {
      JsonArray jsonAddresses = new JsonArray();
      for (CoinAddress address : addresses) {
        jsonAddresses.add(address.base58());
      }
      params.add(jsonAddresses);
    }

    JsonObject response = RpcInvoker.invoke(
      OpenAssetsProtocolEnv.getHost(),
      OpenAssetsProtocolEnv.getPort(),
      "listunspent",
      params,
      OpenAssetsProtocolEnv.getUser(),
      OpenAssetsProtocolEnv.getPassword()
    );

    JsonArray jsonResult = response.getAsJsonArray("result");
    java.util.List<UnspentCoinDescriptor> descriptors = new java.util.ArrayList<UnspentCoinDescriptor>();


    for (JsonElement e : jsonResult) {
      JsonObject jo = e.getAsJsonObject();
      String addressOption = jo.get("address").getAsString();
      String accountOption = jo.get("account").getAsString();
      UnspentCoinDescriptor ucd = new UnspentCoinDescriptor(
        new Hash(new Bytes(HexUtil.bytes(jo.get("txid").getAsString()))),
        jo.get("vout").getAsInt(),  //((JsNumber)fields.get("vout").get()).value().intValue(),
        addressOption,   //Option.apply(fields.get("address").get().toString()),
        accountOption,   //Option.apply(fields.get("account").get().toString()),
        jo.get("scriptPubKey").getAsString(),   //fields.get("scriptPubKey").get().toString(),
        null,
        jo.get("amount").getAsBigDecimal(), // ((JsNumber)fields.get("amount").get()).value(),
        jo.get("confirmations").getAsLong(), //((JsNumber)fields.get("confirmations").get()).value().longValue(),
        jo.get("spendable").getAsBoolean() //((JsBoolean)fields.get("spendable").get()).value()
      );
      descriptors.add(ucd);
    }

    return descriptors;
  }

  @Override
  public List<WalletTransactionDescriptor> listTransactions(String accountOption, int count, long skip, boolean includeWatchOly) {
    List<WalletTransactionDescriptor> result = new ArrayList<WalletTransactionDescriptor>();
    // TODO implement
    return result;
  }

  @Override
  public List<CoinAddress> getAddressesByAccount(String accountOption, boolean includeWatchOnly) throws OapException {
    return null;
  }

  @Override
  public CoinAddress getReceivingAddress(String account) throws OapException {
    return null;
  }
}
