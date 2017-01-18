package io.scalechain.blockchain.oap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.OapStorage;
import io.scalechain.blockchain.oap.helper.UnitTestHelper;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.storage.Storage$;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Option;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by shannon on 16. 12. 27.
 */
public class OapStorageTest {
  static OapStorage storage = null;
  @BeforeClass
  public static void setUpForClass() throws OapException {
    ChainEnvironment$.MODULE$.create("mainnet");
    Storage$.MODULE$.initialize();
    File targetPath = new File("./target");
    if (!targetPath.exists()) {
        targetPath.mkdir();
    }

    File path = new File(targetPath, "oap-storage-test");
    try {
      FileUtils.deleteDirectory(path);
    }catch(IOException e) {
      e.printStackTrace();
    }
    storage = new OapStorage(path);

    coinAddresses = new CoinAddress[] {
      CoinAddress.from("17KGX72xM71xV99FvYWCekabF5aFWx78US"),
      CoinAddress.from("18VZZcini4mcopSAp3iM7xcGJsizcZgnu8")
    };
    assetId = AssetId.from(coinAddresses[0]);
    UnitTestHelper.init();

  }

  String zeroHashHex = "0000000000000000000000000000000000000000000000000000000000000000";
  static String txHashHex = "00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00";
  static CoinAddress[] coinAddresses;
  static AssetId assetId;


  @Test
  public void outputTest() throws OapException {
    Hash txHash = new Hash(ByteArray.apply(HexUtil.bytes(txHashHex)));
    Hash zeroHash = new Hash(ByteArray.apply(HexUtil.bytes(zeroHashHex)));
    // TEST GET OUTPUT RETURN None
    Option<OapTransactionOutput> souldBeNone = storage.getOutput(new OutPoint(zeroHash, 0));
    assertTrue("1: Output " + zeroHash + ":0 should not exist", souldBeNone.isEmpty());

    // DELETE ALL OUTPUTS of txHash
    storage.delOutputs(txHash);
    assertTrue("2: Outputs of txHash: " + txHashHex + " should not exist", storage.getOutputs(txHash).size() == 0);

    // PUT OUTPUTS AND GET BACK
    OapTransactionOutput[] cacheItems = new OapTransactionOutput[coinAddresses.length];
    for(int i = 0;i < coinAddresses.length;i++) {
      OutPoint key = OutPoint.apply(txHash, i + 1);
      TransactionOutput output = TransactionOutput.apply(IOapConstants.DUST_IN_SATOSHI, coinAddresses[i].lockingScript());
      OapTransactionOutput value = new OapTransactionOutput(assetId, 1000 * (i + 1), output);
      storage.putOutput(key, value);
      cacheItems[i] = value;
      Option<OapTransactionOutput> valueOption = storage.getOutput(key);
      assertTrue("3: getOutput() should return a value", valueOption.isDefined());
      assertEquals("3: getOutput() value should be equal", value, valueOption.get());
    }

    // TEST GET OUTPUTS
    List<Pair<OutPoint,OapTransactionOutput>> outputs = storage.getOutputs(txHash);
    assertEquals("4: Number of elements of getOutput() value should be equal", coinAddresses.length, outputs.size());
    for(int i = 0;i < cacheItems.length;i++) {
      OapTransactionOutput txOutput = outputs.get(i).getSecond();
      assertTrue("4: Output should be equal", (
        txOutput.value() == IOapConstants.DUST_IN_SATOSHI &&
        txOutput.lockingScript().equals(cacheItems[i].lockingScript()) &&
        txOutput.getAssetId().base58().equals(cacheItems[i].getAssetId().base58()) &&
        txOutput.getQuantity() == cacheItems[i].getQuantity()
      ));
    }
  }

  @Test
  public void delOutputTest() throws OapException {
    Hash txHash = new Hash(ByteArray.apply(HexUtil.bytes(txHashHex)));
    Hash zeroHash = new Hash(ByteArray.apply(HexUtil.bytes(zeroHashHex)));

    // DELETE ALL OUTPUTS of txHash
    storage.delOutputs(txHash);
    // PUT OUTPUTS AND GET BACK
    OapTransactionOutput[] cacheItems = new OapTransactionOutput[coinAddresses.length];
    for(int i = 0;i < coinAddresses.length;i++) {
      OutPoint key = OutPoint.apply(txHash, i + 1);
      TransactionOutput output = TransactionOutput.apply(IOapConstants.DUST_IN_SATOSHI, coinAddresses[i].lockingScript());
      OapTransactionOutput value = new OapTransactionOutput(assetId, 1000 * (i + 1), output);
      storage.putOutput(key, value);
      cacheItems[i] = value;
      Option<OapTransactionOutput> valueOption = storage.getOutput(key);
    }

    List<Pair<OutPoint,OapTransactionOutput>> outputs = storage.getOutputs(txHash);
    for(int i = 0;i < cacheItems.length;i++) {
      OapTransactionOutput txOutput = outputs.get(i).getSecond();
      assertTrue("4: Output should be equal", (
        txOutput.value() == IOapConstants.DUST_IN_SATOSHI &&
          txOutput.lockingScript().equals(cacheItems[i].lockingScript()) &&
          txOutput.getAssetId().base58().equals(cacheItems[i].getAssetId().base58()) &&
          txOutput.getQuantity() == cacheItems[i].getQuantity()
      ));
    }

    // TEST DELETE OUTPUTS
    for(Pair<OutPoint, OapTransactionOutput> p : outputs) {
      storage.delOutput(p.getFirst());
    }
    assertTrue("Outputs should not exist", storage.getOutputs(txHash).size() == 0);
  }

  @Test
  public void delOutputsTest() throws OapException {
    Hash txHash = new Hash(ByteArray.apply(HexUtil.bytes(txHashHex)));
    Hash zeroHash = new Hash(ByteArray.apply(HexUtil.bytes(zeroHashHex)));
    // TEST GET OUTPUT RETURN None
    Option<OapTransactionOutput> souldBeNone = storage.getOutput(new OutPoint(zeroHash, 0));
    assertTrue("1: Output " + zeroHash + ":0 should not exist", souldBeNone.isEmpty());

    // DELETE ALL OUTPUTS of txHash
    storage.delOutputs(txHash);
    assertTrue("2: Outputs of txHash: " + txHashHex + " should not exist", storage.getOutputs(txHash).size() == 0);

    // PUT OUTPUTS AND GET BACK
    OapTransactionOutput[] cacheItems = new OapTransactionOutput[coinAddresses.length];
    for(int i = 0;i < coinAddresses.length;i++) {
      OutPoint key = OutPoint.apply(txHash, i + 1);
      TransactionOutput output = TransactionOutput.apply(IOapConstants.DUST_IN_SATOSHI, coinAddresses[i].lockingScript());
      OapTransactionOutput value = new OapTransactionOutput(assetId, 1000 * (i + 1), output);
      storage.putOutput(key, value);
      cacheItems[i] = value;
      Option<OapTransactionOutput> valueOption = storage.getOutput(key);
    }

    // TEST DELETE OUTPUTS
    int count = storage.delOutputs(txHash);
    assertEquals("Number of deleted outputs should be equal", count, coinAddresses.length);
    for(int i = 0;i < coinAddresses.length;i++) {
      OutPoint key = OutPoint.apply(txHash, 1);
      Option<OapTransactionOutput> valueOption = storage.getOutput(key);
      assertTrue("getOutput() should return None", valueOption.isEmpty());
    }
    assertTrue("Outputs should not exist", storage.getOutputs(txHash).size() == 0);
  }

  @Test
  public void assetDefinitionTest() throws OapException {
    String coinAddress = UnitTestHelper.getBitcoinAddressAt(UnitTestHelper.SEND_MANY_SEND_ADDRESS_INDEX);
    JsonObject expectedAssetDefinition = UnitTestHelper.getAssetDefinition(coinAddress);

    JsonObject json = new JsonObject();
    for(Map.Entry<String, JsonElement> e : expectedAssetDefinition.entrySet()) {
      json.add(e.getKey(), e.getValue());
    }
    json.remove(AssetDefinition.ASSET_IDS);

    AssetDefinition definition = AssetDefinition.from(UnitTestHelper.getAssetId(coinAddress), json.toString());
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());

    storage.delAssetDefintion(pointer);
    storage.delAssetDefinitionPointer(assetId.base58());

    Option<String> definitionOption = storage.getAssetDefinition(pointer);
    assertTrue("The deleted defition should not exist", definitionOption.isEmpty());
    Option<AssetDefinitionPointer> pointerOption = storage.getAssetDefinitionPointer(assetId.base58());
    assertTrue("The deleted defition pointer should not exist", pointerOption.isEmpty());

    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);

    pointerOption = storage.getAssetDefinitionPointer(assetId.base58());
    definitionOption = storage.getAssetDefinition(pointerOption.get());
    assertTrue("The defition newly put should exist", definitionOption.isDefined());
    assertTrue("The defition pointer newly put should exist", pointerOption.isDefined());

    assertEquals("Inserted defintion sould be equal", definitionOption.get().toString(), definition.toString());
    assertEquals("Inserted defintion sould be equal", pointerOption.get(), pointer);

    pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    pointerOption = storage.getAssetDefinitionPointer(assetId.base58());
    assertEquals("Inserted defintion sould be equal", pointerOption.get(), pointer);
    definitionOption = storage.getAssetDefinition(pointer);
    assertEquals("Inserted defintion sould be equal", definitionOption.get().toString(), definition.toString());
    definitionOption = storage.getAssetDefinition(pointerOption.get());
    assertEquals("Inserted defintion sould be equal", definitionOption.get().toString(), definition.toString());
  }
}
