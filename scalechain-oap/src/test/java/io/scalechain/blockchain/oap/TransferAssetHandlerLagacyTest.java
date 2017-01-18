package io.scalechain.blockchain.oap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.*;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.UnspentCoinDescriptor;
import org.junit.*;
import org.junit.rules.ExpectedException;
import scala.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by shannon on 17. 1. 7.
 */
public class TransferAssetHandlerLagacyTest {
  @BeforeClass
  public static void setUpForClass() throws Exception {
    System.out.println(AddressUtilMainNetTest.class.getName() + ".setupForClass()");

    ChainEnvironment$.MODULE$.create("mainnet");
  }


  String[] coinAddresses = {
    "17KGX72xM71xV99FvYWCekabF5aFWx78US",
    "18VZZcini4mcopSAp3iM7xcGJsizcZgnu8"
  };

  String[] assetAddresses = {
    "akHH9mGrHpaueMPJcyAcPJcVAagkRTUFvZJ",
    "akJTSonY8BYfJg4bXrfpXmpWqeUuAdquP2j"
  };

  String[] assetIds = {
    "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH",
    "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"
  };

  String[] metadata = {
    "u=https://cpr.sm/YyKRtaYoZP",
    "u=https://cpr.sm/P_QS_x9uD8"
  };

  int[] coinAmounts = {
    68200,
    8200
  };

  int[][] assetQuantities = {
    {8744, 3000},
    {1256, 7000}
  };

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Test
  public void toAssetQuantitiesTest() throws OapException {
    TransferAssetHandler hanlder = TransferAssetHandler.get();

    List<AssetTransfer> tos = new ArrayList<AssetTransfer>();
    for (int i = 0; i < assetQuantities[0].length; i++) {
      tos.add(AssetTransfer.create(assetAddresses[1], assetIds[i], (i + 1) * 1000));
    }
    // DO TEST
    int[] quantities = hanlder.toAssetQuanties(tos);
    assertNotNull("Quantities should not be null", quantities);
    assertEquals("Size of quantities should be that of tos", quantities.length, tos.size());
    for (int i = 0; i < tos.size(); i++) {
      assertEquals("Each quantiy should be equal to that of each to", quantities[i], tos.get(i).getQuantity());
    }
  }

  @Test
  public void groupAssetTransfersByAssetIdWithNegativeQuantityTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Invalid quantity"));

    TransferAssetHandler hanlder = TransferAssetHandler.get();

    CoinAddress fromAddress = CoinAddress.from(coinAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[0]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    for (int i = 0; i < assetQuantities[0].length; i++) {
      tos.add(AssetTransferTo.apply(assetAddresses[1], assetIds[i], -1));
    }

    // DO TEST
    HashMap<AssetId, List<AssetTransfer>> assetTransfers = hanlder.groupAssetTransfersByAssetId(
      tos
    );
  }

  @Test
  public void groupAssetTransfersByAssetIdWithInvalidAssetIdTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid AssetId");

    TransferAssetHandler hanlder = TransferAssetHandler.get();

    CoinAddress fromAddress = CoinAddress.from(coinAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[0]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    for (int i = 0; i < assetQuantities[0].length; i++) {
      tos.add(AssetTransferTo.apply(assetAddresses[1], coinAddresses[0], 1000));
    }
    HashMap<AssetId, List<AssetTransfer>> assetTransfers = hanlder.groupAssetTransfersByAssetId(
      tos
    );
  }

  @Test
  public void groupAssetTransfersByAssetIdWithInvalidAssetAddressTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Invalid AssetAddress:"));

    CoinAddress fromAddress = CoinAddress.from(coinAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[0]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    for (int i = 0; i < assetQuantities[0].length; i++) {
      tos.add(AssetTransferTo.apply(coinAddresses[1], assetIds[i], 1000));
    }
    HashMap<AssetId, List<AssetTransfer>> assetTransfers = TransferAssetHandler.get().groupAssetTransfersByAssetId(
      tos
    );
  }

  @Test
  public void groupAssetTransfersByAssetIdTest() throws OapException {
    CoinAddress fromAddress = CoinAddress.from(coinAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[0]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    for (int i = 0; i < assetQuantities[0].length; i++) {
      tos.add(AssetTransferTo.apply(assetAddresses[1], assetIds[i], 1000));
    }

    // DO TEST
    HashMap<AssetId, List<AssetTransfer>> assetTransfers = TransferAssetHandler.get().groupAssetTransfersByAssetId(
      tos
    );

    assertEquals("Asset Transfers Map should have 2 entry", 2, assetTransfers.size());
    for (int i = 0; i < assetIds.length; i++) {
      List<AssetTransfer> list = assetTransfers.get(AssetId.from(assetIds[i]));
      assertEquals("Asset Transfer List should have 1 element", 1, list.size());
      AssetTransfer actual = list.get(0);
      AssetTransferTo exp = tos.get(i);
      assertTrue(
        "AssetTransfer should match given AssetTransferTo",
        (
          exp.to_address().equals(actual.getToAddress().base58()) &&
            exp.asset_id().equals(actual.getAssetId().base58()) &&
            exp.quantity() == actual.getQuantity()
        )
      );
    }
  }


  static String DESC = "[" +
    "  {" +
    "    \"transaction_hash\": \"b69e437d25db3ad4abe8e5e6d404a9fa8950ff79fba4f96fc37e1635791bc31e\"," +
    "    \"output_index\": 1," +
    "    \"value\": 600," +
    "    \"asset_id\": \"AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK\"," +
    "    \"asset_quantity\": 2000," +
    "    \"addresses\": [ \"17KGX72xM71xV99FvYWCekabF5aFWx78US\" ]," +
    "    \"script_hex\": \"76a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac\"," +
    "    \"spent\": false," +
    "    \"confirmations\": 12" +
    "  }," +
    "  {" +
    "    \"transaction_hash\": \"1bd3781629165b4c163b58be91961102bc344b917f581bd939ba16c8d2dcd231\"," +
    "    \"output_index\": 1," +
    "    \"value\": 600," +
    "    \"asset_id\": \"AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK\"," +
    "    \"asset_quantity\": 1000," +
    "    \"addresses\": [ \"17KGX72xM71xV99FvYWCekabF5aFWx78US\" ]," +
    "    \"script_hex\": \"76a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac\"," +
    "    \"spent\": false," +
    "    \"confirmations\": 930" +
    "  }" +
    "]";

  private UnspentAssetDescriptor createUnspentAssetDescriptor(String account, JsonObject json) throws Exception {
    java.math.BigDecimal amount = java.math.BigDecimal.valueOf(json.get("value").getAsLong());
    amount = amount.divide(IOapConstants.ONE_BTC_IN_SATOSHI);
    String base58 = json.get("addresses").getAsJsonArray().get(0).getAsString();
    Option<String> addressOption = Option.apply(base58);
    Option<String> accountOption = Option.apply(account);
    Option<String> redeemScriptOption = Option.empty();
    UnspentCoinDescriptor ucd = new UnspentCoinDescriptor(
      new Hash(new ByteArray(HexUtil.bytes(json.get("transaction_hash").toString()))),
      json.get("output_index").getAsInt(),
      addressOption,
      accountOption,
      json.get("script_hex").getAsString(),
      redeemScriptOption,
      scala.math.BigDecimal.javaBigDecimal2bigDecimal(amount),
      json.get("confirmations").getAsInt(),
      !json.get("spent").getAsBoolean()
    );
    return new UnspentAssetDescriptor(
      ucd, AssetId.from(json.get("asset_id").getAsString()), json.get("asset_quantity").getAsInt()
    );
  }

  @Test
  public void caculateAssetChangeTest() throws OapException, Exception {
    JsonArray j = new JsonParser().parse(DESC).getAsJsonArray();
    List<UnspentAssetDescriptor> inputs = new ArrayList<UnspentAssetDescriptor>();
    for (JsonElement e : j) {
      inputs.add(createUnspentAssetDescriptor("LV", e.getAsJsonObject()));
    }

    AssetId assetId = AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK");
    List<AssetTransfer> transfers = new ArrayList<AssetTransfer>();
    for (int i = 0; i < assetAddresses.length; i++) {
      transfers.add(AssetTransfer.create(
        assetAddresses[i],
        assetId.base58(),
        1200
      ));
    }

    // DO TEST
    Pair<List<UnspentAssetDescriptor>, Integer> assetChange = TransferAssetHandler.get().caculateAssetChange(assetId, inputs, transfers);

    assertEquals("Asset Change should be", 600, assetChange.getSecond().intValue());
    assertEquals("Size of inputs should be", 2, assetChange.getFirst().size());
  }

  @Test
  public void caculateAssetChangeAllInputNoChangeTest() throws OapException, Exception {
    // 2 Descriptors: 2000, 1000 for each
    JsonArray j = new JsonParser().parse(DESC).getAsJsonArray();
    List<UnspentAssetDescriptor> inputs = new ArrayList<UnspentAssetDescriptor>();
    for (JsonElement e : j) {
      inputs.add(createUnspentAssetDescriptor("LV", e.getAsJsonObject()));
    }

    // Spend 3000.
    AssetId assetId = AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK");
    List<AssetTransfer> transfers = new ArrayList<AssetTransfer>();
    for (int i = 0; i < assetAddresses.length; i++) {
      transfers.add(AssetTransfer.create(
        assetAddresses[i],
        assetId.base58(),
        1500
      ));
    }

    // DO TEST
    Pair<List<UnspentAssetDescriptor>, Integer> assetChange = TransferAssetHandler.get().caculateAssetChange(assetId, inputs, transfers);

    assertEquals("Asset Change should be", 0, assetChange.getSecond().intValue());
    assertEquals("Size of inputs should be", 2, assetChange.getFirst().size());
  }

  @Test
  public void caculateAssetChangeSpenddingSingleInputTest() throws OapException, Exception {
    JsonArray j = new JsonParser().parse(DESC).getAsJsonArray();
    List<UnspentAssetDescriptor> inputs = new ArrayList<UnspentAssetDescriptor>();
    for (JsonElement e : j) {
      inputs.add(createUnspentAssetDescriptor("LV", e.getAsJsonObject()));
    }
    // Spend 1800.
    //   2000 => 1800 + 200
    AssetId assetId = AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK");
    List<AssetTransfer> transfers = new ArrayList<AssetTransfer>();
    transfers.add(AssetTransfer.create(
      assetAddresses[1],
      assetId.base58(),
      1800
    ));

    // DO TEST
    Pair<List<UnspentAssetDescriptor>, Integer> assetChange = TransferAssetHandler.get().caculateAssetChange(assetId, inputs, transfers);
    assertEquals("Size of spending input should be =", 1, assetChange.getFirst().size());
    assertEquals("Asset Change should be", 200, assetChange.getSecond().intValue());
  }

  @Test
  public void caculateAssetChangeNotEnoughAssetTest() throws OapException, Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Not enoough asset"));

    JsonArray j = new JsonParser().parse(DESC).getAsJsonArray();
    List<UnspentAssetDescriptor> inputs = new ArrayList<UnspentAssetDescriptor>();
    for (JsonElement e : j) {
      inputs.add(createUnspentAssetDescriptor("LV", e.getAsJsonObject()));
    }

    // Try to spend 4000. Not enough asset Exception should be thrown
    AssetId assetId = AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK");
    List<AssetTransfer> transfers = new ArrayList<AssetTransfer>();
    for (int i = 0; i < assetAddresses.length; i++) {
      transfers.add(AssetTransfer.create(
        assetAddresses[i],
        assetId.base58(),
        2000
      ));
    }

    // DO TEST
    Pair<List<UnspentAssetDescriptor>, Integer> assetChange = TransferAssetHandler.get().caculateAssetChange(assetId, inputs, transfers);
  }

  @Test
  public void caculateAssetChangeNoAssetTest() throws OapException, Exception {
    thrown.expect(OapException.class);
    thrown.expectMessage(startsWith("Address has no asset"));

    // GIVE NO UNSPENT ASSETS
    List<UnspentAssetDescriptor> inputs = new ArrayList<UnspentAssetDescriptor>();
    AssetId assetId = AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK");
    List<AssetTransfer> transfers = new ArrayList<AssetTransfer>();
    for (int i = 0; i < assetAddresses.length; i++) {
      transfers.add(AssetTransfer.create(
        assetAddresses[i],
        assetId.base58(),
        1500
      ));
    }

    // DO TEST
    Pair<List<UnspentAssetDescriptor>, Integer> assetChange = TransferAssetHandler.get().caculateAssetChange(assetId, inputs, transfers);
  }

  @Test
  public void invalidQuantityTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Invalid quantity");
    int quantity = -1;
    long fees = IOapConstants.DEFAULT_FEES_IN_SATOSHI;
    CoinAddress fromAddress = CoinAddress.from(coinAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[1]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(AssetTransferTo.apply(toAddress.base58(), assetIds[0], -1));
    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      AssetAddress.fromCoinAddress(coinAddresses[0]),
      tos,
      AssetAddress.fromCoinAddress(coinAddresses[0]),
      fees
    );
  }

  @Test
  public void feesToSmallTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Fees are too small");
    int quantity = 1000;
    long fees = IOapConstants.MIN_FEES_IN_SATOSHI - 1;
    AssetAddress fromAddress = AssetAddress.from(assetAddresses[0]);
    AssetAddress toAddress = AssetAddress.from(assetAddresses[1]);
    List<AssetTransferTo> tos = new ArrayList<AssetTransferTo>();
    tos.add(AssetTransferTo.apply(toAddress.base58(), assetIds[0], quantity));
    Transaction tx = TransferAssetHandler.get().createTransferTransaction(
      fromAddress,
      tos,
      fromAddress,
      fees
    );
  }
}
