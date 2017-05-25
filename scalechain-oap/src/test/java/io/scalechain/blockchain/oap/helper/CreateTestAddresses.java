package io.scalechain.blockchain.oap.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.PrivateKey;
import io.scalechain.blockchain.transaction.PublicKey;
import io.scalechain.crypto.HashFunctions;
import io.scalechain.util.HexUtil;

/**
 * Create 8 test address and asset definition file.
 * Created by shannon on 16. 12. 25.
 */
public class CreateTestAddresses {
  public static void main(String[] args) throws Exception {
    ChainEnvironment.create("testnet");
    JsonArray array = new JsonArray();
    for (int i = 0; i < 8; i++) {
      PrivateKey privateKey = PrivateKey.generate();
      PublicKey publicKey = PublicKey.from(privateKey);
      CoinAddress coinAddress = CoinAddress.from(privateKey);
      String coinAddressBase58 = coinAddress.base58();
      LockingScript lockingScript = coinAddress.lockingScript();


      JsonObject addressObject = new JsonObject();
      String assetId = AssetId.from(coinAddress).base58();
      addressObject.addProperty("privateKey", privateKey.base58());
      addressObject.addProperty("privateKeyHex", HexUtil.hex(privateKey.getValue().toByteArray()));
      addressObject.addProperty("publicKeyEncodedHex", HexUtil.hex(publicKey.encode()));
      addressObject.addProperty("publicKeyHashHex", HexUtil.hex(publicKey.getHash().getValue().getArray()));
      addressObject.addProperty("bitcoinAddress", coinAddressBase58);
      addressObject.addProperty("lockingScriptHex", HexUtil.hex(lockingScript.getData().getArray()));
      addressObject.addProperty("assetAddress", AssetAddress.fromCoinAddress(coinAddress).base58());
      addressObject.addProperty("assetId", assetId);
      JsonObject assetDefinition = new JsonObject();
      JsonArray ids = new JsonArray();
      ids.add(AssetId.from(coinAddress).base58());
      assetDefinition.add("asset_ids", ids);
      assetDefinition.addProperty("contact_url", "https://api.scalechain.io/assets/" + assetId);
      assetDefinition.addProperty("name", "OAP Test Asset " + i);
      assetDefinition.addProperty("name_short", "TestAsset" + i);
      assetDefinition.addProperty("issuer", "ScaleChain");
      assetDefinition.addProperty("description", "Test Asset " + i + ", for test purpose only.");
      assetDefinition.addProperty("descripton_mime", "text/plain");
      assetDefinition.addProperty("type", "other");
      assetDefinition.addProperty("divisibility", 0);
      assetDefinition.addProperty("link_to_website", false);
      assetDefinition.addProperty("icon_url", "https://api.scalechain.io/profile/icon/" + assetId + ".jpg");
      assetDefinition.addProperty("image_url", "https://api.scalechain.io/profile/image/" + assetId + ".jpg");
      assetDefinition.addProperty("version", "1.0");
      addressObject.add("asset_definition", assetDefinition);
      addressObject.addProperty("asset_definition_hash", HexUtil.hex(
        HashFunctions.hash160(assetDefinition.toString().getBytes()).getValue().getArray()
      ));
      array.add(addressObject);
//            System.out.println("privateKey=" + privateKey);
//            System.out.println("privateKey.base58()=" + privateKey.base58());
//            System.out.println("privateKeyHex=" + HexUtil.hex(privateKey.value().toByteArray()));
//            System.out.println("publicKey=" + publicKey);
//            System.out.println("publicKey.encode().hex()=" + HexUtil.hex(publicKey.encode()));
//            System.out.println("publicKey.hash().hex()=" + HexUtil.hex(publicKey.getHash().bytes()));
//            System.out.println("BitcoinAddress.publicKeyHash().hex()=" + HexUtil.hex(coinAddress.publicKeyHash().array()));
//            System.out.println("BitcoinAddress=" + coinAddressBase58);
//            System.out.println("lockingScript=" + lockingScript);
//            System.out.println("locakingScript.hex()=" +  HexUtil.hex(lockingScript.data().array()));
//            System.out.println("AssetAddress=" + AssetAddress.from(coinAddress));
//            System.out.println("AssetId=" + AssetId.from(coinAddress));
//
//            //       094ca10729ece97a1aec4fcf0f9c97377ecc4e1d
//            // 76a914094ca10729ece97a1aec4fcf0f9c97377ecc4e1d88ac
//            ByteBuffer buffer = ByteBuffer.allocate(25);
//
//            buffer.put((byte)0x76);
//            buffer.put((byte)0xa9);
//            buffer.put((byte)0x14);
//
//            buffer.put(coinAddress.publicKeyHash().array());
//
//            buffer.put((byte)0x88);
//            buffer.put((byte)0xac);
//            buffer.flip();
//
//            byte[] bytes = new byte[25];
//
//            buffer.get(bytes);
//            System.out.println("LockingScript(ManuallyBuilt)=" + HexUtil.hex(bytes));

    }
    System.out.println(array.toString());

  }

}
