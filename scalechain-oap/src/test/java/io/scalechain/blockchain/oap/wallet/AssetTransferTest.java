package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by shannon on 16. 12. 6.
 */
public class AssetTransferTest {
  static String env = "mainnet";

  @BeforeClass
  public static void setUpForClass() throws Exception {
    System.out.println(AssetTransferTest.class.getName() + ".setupForClass()");
    ChainEnvironment.create(env);
  }

  String[] coinAddresses = {
    "16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM",
    "1Pwes7rbLb4cjQ8z4tSiS13zVaHKqtJ33U"
  };
  String[] assetAddresses = {
    "akB4NBW9UuCmHuepksob6yfZs6naHtRCPNy",
    "akZuY7Hfvp4xJbeJM7WYu5rxZqBTVqYWs2A"
  };

  String[] assetIds = {
    "ALn3aK1fSuG27N96UGYB1kUYUpGKRhBuBC",
    "AHnGH2NJQmoV97tix2dtHUAk3ut1Sn4U9p"
  };

  @Test
  public void createAssetTransferTest() throws Exception {
    for (int i = 0; i < coinAddresses.length; i++) {
      AssetTransfer transfer = AssetTransfer.create(
        assetAddresses[i],
        assetIds[i],
        10000
      );
      assertEquals("AssetId.base58() should to equal to", assetIds[i], transfer.getAssetId().base58());
      assertEquals("AssetId.base58() should to equal to", assetAddresses[i], transfer.getToAddress().base58());
      assertEquals("AssetId.base58() should to equal to", coinAddresses[i], transfer.getToAddress().coinAddress().base58());
    }
  }


  @Test(expected = Exception.class)
  public void invalidToAddressTest() throws Exception {
    AssetTransfer transfer = AssetTransfer.create("INVALID_ASSET_ADDRESS", "AcUKTMqh7bpC8LttrKpMbSQBAdVejuC8VN", 10000);
  }

  @Test(expected = Exception.class)
  public void invalidAssetIdTest() throws Exception {
    AssetTransfer transfer = AssetTransfer.create(AssetAddress.fromCoinAddress("mu35zU5h6pinJtzvXbx79bNMcmX7Fv2d5X").base58(), "INVALID_ASSET_ID", 10000);
  }

  @Test
  public void AssetTransferToTest() {
    int quantity = 10000;
    AssetTransferTo trnasferTo1 = new AssetTransferTo(assetAddresses[0], assetIds[0], quantity);
    assertEquals("asset_id should to equal to", assetIds[0], trnasferTo1.getAsset_id());
    assertEquals("to_address should to equal to", assetAddresses[0], trnasferTo1.getTo_address());
    assertEquals("quantity should to equal to", quantity, trnasferTo1.getQuantity());
    AssetTransferTo trnasferTo2 = new AssetTransferTo(assetAddresses[0], assetIds[0], quantity);

    assertEquals("2 AssetTranferTo instance created from same values should be equals", trnasferTo1, trnasferTo2);
  }
}
