package io.scalechain.blockchain.oap.coloring;

import io.scalechain.blockchain.chain.NewOutput;
import io.scalechain.blockchain.chain.TransactionBuilder;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.TestWithWalletSampleData;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.sampledata.AddressData;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.blockchain.oap.transaction.OapTransaction;
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput;
import io.scalechain.blockchain.oap.transaction.TransactionCodec;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.proto.OutPoint;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.transaction.CoinAmount;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.Option;
import scala.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by shannon on 16. 12. 8.
 */
public class ColoringEngineTest extends TestWithWalletSampleData {

  String[] issuanceTxHash = {
    "cf0908285fa053b343644391b511e6de855ec1418d734763272142396961fd0a", // SVTest
    "1079025f2463836b8c35cf839d7e844e7572940a91f4412e80ea95dfe2c4e858", // LVTest
  };

  String[] issuanceTxHex = {
    "01000000019963de63ca664ec83e1bfb9c1f0409d817c28269e9503b5a4b39ea5a016a9239000000006a473044022071aee7c741b8beb83d5565ad3105ec02b8c4dce2403c0056c3c2b9d952be3e6102202d25d43dd4271235e131fa5de5b58908924f34a21ecf2c4504f846a8d014683b0121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff0358020000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac0000000000000000256a234f41010001904e1b753d68747470733a2f2f6370722e736d2f505f51535f7839754438385d0100000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac00000000",
    "010000000152d958646a1d3e823a4fe51f44fdda71bafb024d6a7cd5f11f51dcef6ec1434a000000006b48304502210094241eb098b4015bd778c12e1f86c5af0c14048e28346656e6b42fd66270c98f022036a8e85e8bdb62f2501687fb3ba09a06d366ad705ace1b5c6f55988a169624c2012102049b7ccd6808dea43c4fb9c7eee1089df60a1ce77631f2b5b7204ce7b3e75506ffffffff0358020000000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac0000000000000000256a234f41010001904e1b753d68747470733a2f2f6370722e736d2f59794b527461596f5a50385d0100000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac00000000"
  };

  String[] transferTxHash = {
    "6bd33249e286bbb21e2b833ff1b32cb061d8013e3de4009b4a72b93a8f3a8351",
    "1bd3781629165b4c163b58be91961102bc344b917f581bd939ba16c8d2dcd231",
    "00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00",
    "b69e437d25db3ad4abe8e5e6d404a9fa8950ff79fba4f96fc37e1635791bc31e",
  };

  String[] transferTxHex = {
    "010000000258e8c4e2df95ea802e41f4910a9472754e847e9d83cf358c6b8363245f027910000000006b483045022100a9bb8ce884f99e7d4ebd050e8fb3e5a20986d66936769466f0b7cdc5b48a9aef0220425406232fac8fea73b2fa8607c3fa94834622cba19cf1bd55e7cdac487f8128012102049b7ccd6808dea43c4fb9c7eee1089df60a1ce77631f2b5b7204ce7b3e75506ffffffff58e8c4e2df95ea802e41f4910a9472754e847e9d83cf358c6b8363245f027910020000006b483045022100e5319531ccced29a2f30dc2c27321ae349b8d107eac9193465f303cae979504a022074587dc2a95a02c0fdc8e04b122d5df8a0486f0b3e0efe4a9f29d44be30e28c1012102049b7ccd6808dea43c4fb9c7eee1089df60a1ce77631f2b5b7204ce7b3e75506ffffffff0400000000000000000c6a0a4f41010002e807a8460058020000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac58020000000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888acd0330100000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac00000000",
    "01000000020afd6169394221276347738d41c15e85dee611b591436443b353a05f280809cf0000000069463043021f59aac9682c90ea14c8472f7ce30ed3248d1b9c3de500b08a3e45b519e2f11302200ebba6ecfa86ad8afb23dc480a85cb8d8aa60786f2e4086481c3ac15047abd690121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff0afd6169394221276347738d41c15e85dee611b591436443b353a05f280809cf020000006a4730440220363148c8ecc36f8f0fed20814a30e0c1531d513b9e6a37401447e5f2fefcbdaa02207b0a8dc2d35abf4ae1145cf91af4da45d5c806043d6ede0f80c33fbdafecedf90121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff0400000000000000000c6a0a4f41010002e807a8460058020000000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac58020000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488acd0330100000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac00000000",
    "010000000251833a8f3ab9724a9b00e43d3e01d861b02cb3f13f832b1eb2bb86e24932d36b020000006b4830450221009ae744159b8f5eda05235d348924d690177bdfe4f880271ca892767941de04e0022060926f542545ae998239cb645a5a10bff2b2b8f398f7a1df90c20b371790fa79012102049b7ccd6808dea43c4fb9c7eee1089df60a1ce77631f2b5b7204ce7b3e75506ffffffff51833a8f3ab9724a9b00e43d3e01d861b02cb3f13f832b1eb2bb86e24932d36b030000006b483045022100e8e497222d937e678c3eea75aa93bbdd7fff5e3750283ca788faa7f391cf0cd40220100a3c9bd21b375263e72410598df77bc467bb7a265d7a25a3328cc2c552d706012102049b7ccd6808dea43c4fb9c7eee1089df60a1ce77631f2b5b7204ce7b3e75506ffffffff0400000000000000000c6a0a4f410100028002a8440058020000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac58020000000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac680a0100000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac00000000",
    "010000000231d2dcd2c816ba39d91b587f914b34bc02119691be583b164c5b16291678d31b020000006a473044022024412976fb8d7aa7e751ba4af36b36c8845e34d7f90cff88adc05f603daef607022059a167ff53d519457acb557ed3bceb71cbc42ad91c9eea4e10ffbd0b89b1e16b0121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff31d2dcd2c816ba39d91b587f914b34bc02119691be583b164c5b16291678d31b030000006b483045022100b15d3c87513dbc1008d74962f88f5e339e2c1a79b2685963ac203085bfabd54102201140672cb1430bbb7bd9668d7b7892a2796f8c0fb3a467a3dc2b7693ad4700c30121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff0400000000000000000c6a0a4f41010002d00fd8360058020000000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac58020000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac680a0100000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac00000000",
  };

  String[] transferAssetId = {
    "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH", // LVTest
    "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK", // SVTest
    "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH", // SVTest
    "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK", // LVTest
  };

  String[] normalTxHash = {
    "3316f5af78854532362166796b591de5c444ff64cdf2ac93f95778c4a95fb3fd",
    "4a43c16eefdc511ff1d57c6a4d02fbba71dafd441fe54f3a823e1d6a6458d952",
    "321bc9065f927a3c2dbfc34390066aceb4a2c27489ad20473a69ae90fde6b74e",
    "39926a015aea394b5a3b50e96982c217d809041f9cfb1b3ec84e66ca63de6399",
    "e9d4b2c4ab7fff552e01ce97e15ef82431238417c337c97f0a0d2c05df68eb0d"
  };


  String[] normalTxHex = {
    "01000000011ec31b7935167ec36ff9a4fb79ff5089faa904d4e6e5e8abd43adb257d439eb6030000006a473044022029272360bc409bca517e05dba9e734140dab3900f566da1f18acc1fabd5d4d9302205797a56fc8e53e17266e137efa4fa666cfa1884b318f1535a0cb0eb602fa29b20121031b0d4bd536cee78a5fe4d92aaa78759d9788e46cb2909dbefaec6f4eacb2c1e2ffffffff0250c30000000000001976a91453d165c4405d2c4b2e6ecc04d1df381720d559a988ac08200000000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac00000000",
    "01000000014eb7e6fd90ae693a4720ad8974c2a2b4ce6a069043c3bf2d3c7a925f06c91b32010000006b48304502210086c99c857780df3c066e663ea32f8db7ab4feb7601238a53941be169b55b2aa902206f52a19daff21d1e0f264425bc079a67b459124468acedfe171f23a105c038ae0121033692f961e98b83e0ae09a421720be713e8e045021d37a017625970851496d3d7ffffffff02a0860100000000001976a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac30f64501000000001976a91469cec11edc80ceae3f0e4e42fbb4307b47df887788ac00000000",
    "01000000010deb68df052c0d0a7fc937c31784233124f85ee197ce012e55ff7fabc4b2d4e9000000006b483045022100b4898435b0cf7869c780cc37cb50e6fff39460c9ca262bce3a6b0918f3bd1ed0022032733506f162d84d077083223a16c7b80d52f0c830ae0a080c85b6642d9ac0cb0121033692f961e98b83e0ae09a421720be713e8e045021d37a017625970851496d3d7ffffffff0240420f00000000001976a91453d165c4405d2c4b2e6ecc04d1df381720d559a988ace0a34701000000001976a91469cec11edc80ceae3f0e4e42fbb4307b47df887788ac00000000",
    "01000000014eb7e6fd90ae693a4720ad8974c2a2b4ce6a069043c3bf2d3c7a925f06c91b32000000006a473044022050b080a891317ec2008584b92375fcb0993d9b3ae5b3588f59b829f65e53d3c102206218d3e4ed29e27f61fea954dd3c5c8e32cb999b0d533b4771e8dc62b3d3012301210323e193dbba308a1f20fc07f2ef6d725293a0ac21e50b4fc00ade081b8442330fffffffff02a0860100000000001976a914522fb5870192889e85c687b36d6e6070bab0928488ac90940d00000000001976a91453d165c4405d2c4b2e6ecc04d1df381720d559a988ac00000000",
    "0100000001b783d1d1723d53d3528ea249ac30cd3668b76230a155786964de7bf0d9625a93010000006b4830450221008c824360cfbd363ad8ae60ecdc088dfb59686316b351f261c776d8ef56f6edca02207ebea740dd433e2decf016d255a1ef2fa46a8dbe99b037598bf6718970364aef012103d4871d81bc4f61f77e258238ada3454982be4a8fb523c08bba8ba1383f24ca4bfeffffff02300d5701000000001976a91469cec11edc80ceae3f0e4e42fbb4307b47df887788ac3d523276000000001976a914e46705217d172c6e5a0458e3dd08a92b6e5ef00c88acc4ad0600"
  };

  String[] issuanceAssetId = {
    "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK", // SVTest
    "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH", // LVTest
  };


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getMarkerOutputTest() throws OapException {
    ColoringEngine coloringEngine = ColoringEngine.get();
    for (String hex : issuanceTxHex) {
      Transaction tx = TransactionCodec.decode(hex);
      int markerOutIndex = coloringEngine.getMarkerOutputIndex(tx);
      assertEquals("Maker Output Index should be 1", markerOutIndex, 1);
    }
    for (String hex : transferTxHex) {
      Transaction tx = TransactionCodec.decode(hex);
      int markerOutIndex = coloringEngine.getMarkerOutputIndex(tx);
      assertEquals("Maker Output Index should be 0", markerOutIndex, 0);
    }
    for (String hex : normalTxHex) {
      Transaction tx = TransactionCodec.decode(hex);
      int markerOutIndex = coloringEngine.getMarkerOutputIndex(tx);
      assertEquals("Maker Output Index should be -1", markerOutIndex, -1);
    }
  }

  @Test
  public void assetIdForIssuanceTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    String[] accounts = provider.accounts();
    Hash[] hashes = provider.s4IssueTxHashes;
    for (int i = 0; i < hashes.length; i++) {
      AddressData[] addressDatas = provider.addressesOf(accounts[i]);
      AddressData issuerData = addressDatas[addressDatas.length - 1];
      Option<Transaction> txOption = provider.blockChainView().getTransaction(hashes[i], provider.blockChainView().db());
      assertTrue("Transaction should exists", txOption.isDefined());

      AssetId assetId = coloringEngine.assetIdForIssuance(chain, txOption.get());

      assertEquals("AssetId for issuance sholuld be match issuer address", assetId, issuerData.assetId);
    }
  }

  @Test
  public void assetIdForIssuanceForNormalTxTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    String[] accounts = provider.accounts();
    Hash[] hashes = provider.s3NormalTxHashes;
    for (int i = 0; i < hashes.length; i++) {
      AddressData[] addressDatas = provider.addressesOf(accounts[i]);
      AddressData issuerData = addressDatas[0];
      Option<Transaction> txOption = provider.blockChainView().getTransaction(hashes[i], provider.blockChainView().db());
      assertTrue("Transaction should exists", txOption.isDefined());

      try {
        AssetId assetId = coloringEngine.assetIdForIssuance(chain, txOption.get());
      } catch (OapException e) {
        assertThat("OapException Message", e.getMessage(), startsWith("Not OAP Issuance Tx"));
      }
    }
  }

  @Test
  public void orderBasedAssignmentTest() throws OapException {
    // INPUT
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH 3
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH 2
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH 5
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH 3
    //  AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK 9
    // OUTPUT
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH 10 / 10
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH  6 /  6
    //  AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH  7 /  6
    //  AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK  3 /  3
    //
    // REMAINING AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK 6
    int[][] expectedAssetQuntities = {
      {10, 6, 7, 3},
      {10, 6, 6, 3},
    };
    for (int[] assetQuntities : expectedAssetQuntities) {
      AssetId[] expectedAssetIds = {
        AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"),
        AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"),
        AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"),
        AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK")
      };

      List<Integer> listQuantity = new ArrayList<Integer>();
      List<AssetId> listAssetId = new ArrayList<AssetId>();
      HashMap<AssetId, Integer> inputQuantityMap = new HashMap<AssetId, Integer>();
      HashMap<AssetId, Integer> expectedRemaining = new HashMap<AssetId, Integer>();
      inputQuantityMap.put(AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"), 23);
      inputQuantityMap.put(AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"), 9);
      expectedRemaining.put(AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"), 6);

      ColoringEngine coloringEngine = ColoringEngine.get();
      for (int q : assetQuntities) {
        Pair<AssetId, Integer> pair = coloringEngine.getNextAssetIdAndQuantity(inputQuantityMap, q);
        listQuantity.add(pair.getSecond());
        listAssetId.add(pair.getFirst());
      }
      int[] arrayQuantity = new int[listQuantity.size()];
      for (int i = 0; i < arrayQuantity.length; i++) {
        arrayQuantity[i] = listQuantity.get(i);
      }
      assertArrayEquals("Asset Qunatity Count", assetQuntities, arrayQuantity);
      assertArrayEquals("Asset Ids", expectedAssetIds, listAssetId.toArray(new AssetId[0]));
      assertEquals("REMAINING ASSETS AND QUANTITIES", expectedRemaining, inputQuantityMap);
    }
  }

  @Test
  public void orderBasedAssignment2Test() throws OapException {
    int[] assetQuntities = {10, 6, 8, 3};
    int[] expectedAssetQuntities = {10, 6, 8, 0};
    AssetId[] expectedAssetIds = {
      AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"),
      AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"),
      AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"),
      null
    };

    List<Integer> listQuantity = new ArrayList<Integer>();
    List<AssetId> listAssetId = new ArrayList<AssetId>();
    HashMap<AssetId, Integer> inputQuantityMap = new HashMap<AssetId, Integer>();
    HashMap<AssetId, Integer> expectedRemaining = new HashMap<AssetId, Integer>();
    inputQuantityMap.put(AssetId.from("AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"), 23);
    inputQuantityMap.put(AssetId.from("AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"), 9);

    ColoringEngine coloringEngine = ColoringEngine.get();
    for (int q : assetQuntities) {
      Pair<AssetId, Integer> pair = coloringEngine.getNextAssetIdAndQuantity(inputQuantityMap, q);
      listQuantity.add(pair.getSecond());
      listAssetId.add(pair.getFirst());
    }
    int[] arrayQuantity = new int[listQuantity.size()];
    for (int i = 0; i < arrayQuantity.length; i++) {
      arrayQuantity[i] = listQuantity.get(i);
    }
    assertArrayEquals("Asset Qunatity Count", expectedAssetQuntities, arrayQuantity);
    assertArrayEquals("Asset Ids", expectedAssetIds, listAssetId.toArray(new AssetId[0]));
    assertEquals("REMAINING ASSETS AND QUANTITIES", expectedRemaining, inputQuantityMap);
  }

  @Test
  public void colorUntilMarkerOutputIssuanceTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    String[] accounts = provider.accounts();
    Hash[] hashes = provider.s6IssueTxHashes;
    for (int i = 0; i < hashes.length; i++) {
      AddressData[] addressDatas = provider.addressesOf(accounts[i]);
      AddressData issuerData = addressDatas[0];
      Option<Transaction> txOption = provider.blockChainView().getTransaction(hashes[i], provider.blockChainView().db());
      assertTrue("Transaction should exists", txOption.isDefined());
      int markerOutputIndex = coloringEngine.getMarkerOutputIndex(txOption.get());
      assertThat("MARKER OUTPUT shluld exist", markerOutputIndex != -1);

      List<TransactionOutput> outputs = coloringEngine.colorUntilMarkerOutput(chain, txOption.get(), markerOutputIndex);

      assertEquals("OUTPUTS SHOULD BE 2", 2, outputs.size());
      assertTrue("SECOND OUTPUT SHOULD BE MARKER OUTPUT", (outputs.get(1) instanceof OapMarkerOutput));
      assertTrue("FIRST OUTPUT SHOULD BE ISSUANCE OUTPUT", (outputs.get(0) instanceof OapTransactionOutput));
      OapTransactionOutput o = (OapTransactionOutput) outputs.get(0);
      assertEquals("ASSET ID", issuerData.assetId, o.getAssetId());
      assertEquals("ASSET QUANTITY", 10000, o.getQuantity());
    }
  }

  @Test
  public void colorUntilMarkerOutputTransferTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();

    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    String[] accounts = provider.accounts();
    Hash[] hashes = provider.s5TransferTxHashes;
    for (int i = 0; i < hashes.length; i++) {
      AddressData[] addressDatas = provider.addressesOf(accounts[i]);
      AddressData issuerData = addressDatas[0];
      Option<Transaction> txOption = provider.blockChainView().getTransaction(hashes[i], provider.blockChainView().db());
      assertTrue("Transaction should exists", txOption.isDefined());
      int markerOutputIndex = coloringEngine.getMarkerOutputIndex(txOption.get());
      assertThat("MARKER OUTPUT shluld exist", markerOutputIndex != -1);

      List<TransactionOutput> outputs = coloringEngine.colorUntilMarkerOutput(chain, txOption.get(), markerOutputIndex);

      assertEquals("OUTPUTS SHOULD BE 1", 1, outputs.size());
      assertTrue("THE ONLY OUTPUT SHOULD BE MARKER OUTPUT", (outputs.get(0) instanceof OapMarkerOutput));
      assertArrayEquals("ASSET QUANTITIES SHOULD BE EQUAL", new int[]{(i + 1) * 2000, 10000 - (i + 1) * 2000}, ((OapMarkerOutput) outputs.get(0)).getQuantities());
    }
  }

  @Test
  public void colorIssuanceTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();

    int index = 0;
    String[] accounts = provider.accounts();
    for (Hash hash : provider.s4IssueTxHashes) {
      AddressData[] addressData = provider.addressesOf(accounts[index]);
      AddressData fromAddres = addressData[addressData.length - 1];

      Option<Transaction> rawTxOption = provider.blockChainView().getTransaction(hash, provider.blockChainView().db());
      assertTrue("Transaction should exists", rawTxOption.isDefined());
      assertEquals("MarkerOutput Index sholuld be 1", 1, coloringEngine.getMarkerOutputIndex(rawTxOption.get()));
      Transaction tx = ColoringEngine.get().color(rawTxOption.get(), hash);
      assertTrue("Transaction should be an instance of " + OapTransaction.class.getSimpleName(), (tx instanceof OapTransaction));

      OapTransactionOutput issueOutput = (OapTransactionOutput) tx.outputs().apply(0);
      // ASSET ID ISSUE OUTUT
      assertEquals("Asset Id of ISSUE OUTPUT should be " + fromAddres.assetId.base58(), fromAddres.assetId, issueOutput.getAssetId());
      // QUANTITY OF ISSUE OUTUT
      assertEquals("Asset Id of ISSUE OUTPUT should be " + 10000, 10000, issueOutput.getQuantity());
      // ASSET ADDRESS OF ISSUE OUTPUT
      assertEquals("Receiving Address of ISSUE OUTPUT should be ISSUER Address", fromAddres.address.lockingScript(), issueOutput.lockingScript());
      index++;
    }
  }

  @Test
  public void colorIssuanceToDifferentAddressTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();

    int index = 0;
    String[] accounts = provider.accounts();
    for (Hash hash : provider.s6IssueTxHashes) {
      AddressData[] addressData = provider.addressesOf(accounts[index]);
      AddressData fromAddres = addressData[0];
      AddressData toAddres = addressData[addressData.length - 1];

      Option<Transaction> rawTxOption = provider.blockChainView().getTransaction(hash, provider.blockChainView().db());
      assertTrue("Transaction should exists", rawTxOption.isDefined());
      assertEquals("MarkerOutput Index sholuld be 1", 1, coloringEngine.getMarkerOutputIndex(rawTxOption.get()));
      Transaction tx = ColoringEngine.get().color(rawTxOption.get(), hash);
      assertTrue("Transaction should be an instance of " + OapTransaction.class.getSimpleName(), (tx instanceof OapTransaction));

      OapTransactionOutput issueOutput = (OapTransactionOutput) tx.outputs().apply(0);
      // ASSET ID ISSUE OUTUT
      assertEquals("Asset Id of ISSUE OUTPUT should be " + fromAddres.assetId.base58(), fromAddres.assetId, issueOutput.getAssetId());
      // QUANTITY OF ISSUE OUTUT
      assertEquals("Asset Id of ISSUE OUTPUT should be " + 10000, 10000, issueOutput.getQuantity());
      // ASSET ADDRESS OF ISSUE OUTPUT
      assertEquals("Receiving Address of ISSUE OUTPUT should be ISSUER Address", toAddres.address.lockingScript(), issueOutput.lockingScript());

      index++;
    }
  }

  @Test
  public void colorTransferTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();

    int index = 0;
    String[] accounts = provider.accounts();
    for (Hash hash : provider.s5TransferTxHashes) {
      AddressData[] addressData = provider.addressesOf(accounts[index]);
      AddressData fromAddres = addressData[addressData.length - 1];
      AddressData toAddress = addressData[0];

      Option<Transaction> rawTxOption = provider.blockChainView().getTransaction(hash, provider.blockChainView().db());
      assertTrue("Transaction should exists", rawTxOption.isDefined());
      assertEquals("MarkerOutput Index sholuld be 0", coloringEngine.getMarkerOutputIndex(rawTxOption.get()), 0);
      Transaction tx = ColoringEngine.get().color(rawTxOption.get(), hash);
      assertTrue("Transaction should be an instance of " + OapTransaction.class.getSimpleName(), (tx instanceof OapTransaction));

      OapTransactionOutput transferOuput = (OapTransactionOutput) tx.outputs().apply(1);
      OapTransactionOutput changeOuput = (OapTransactionOutput) tx.outputs().apply(2);
      // ASSET ID TRANSFER OUTUT
      assertEquals("Asset Id of TRANSFER OUTPUT should be " + fromAddres.assetId.base58(), fromAddres.assetId, transferOuput.getAssetId());
      // QUANTITY OF TRANSFER OUTUT
      assertEquals("Asset Id of TRANSFER OUTPUT should be " + ((index + 1) * 2000), ((index + 1) * 2000), transferOuput.getQuantity());
      // ADDRESS OF TRANSFER OUTPUT
      assertEquals("Receiving Address of ISSUE OUTPUT should be RECEIVER Address", toAddress.address.lockingScript(), transferOuput.lockingScript());
      // ASSET ID OF CHANGE OUTPUT
      assertEquals("Asset Id of CHANGE OUTPUT should be " + fromAddres.assetId.base58(), fromAddres.assetId, changeOuput.getAssetId());
      // QUANTITY OF CHANGE OUTUT
      assertEquals("Asset Id of CHANGE OUTPUT should be " + (10000 - (index + 1) * 2000), (10000 - (index + 1) * 2000), changeOuput.getQuantity());
      // ADDRESS OF CHANGE OUTPUT
      assertEquals("Receiving Address of ISSUE OUTPUT should be SENDER Address", fromAddres.address.lockingScript(), changeOuput.lockingScript());

      index++;
    }
  }

  @Test
  public void colorNormalTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = ColoringEngine.get();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    for (Hash hash : provider.s3NormalTxHashes) {
      Option<Transaction> rawTxOption = provider.blockChainView().getTransaction(hash, provider.blockChainView().db());
      assertTrue("Transaction should exists", rawTxOption.isDefined());
      Transaction tx = coloringEngine.color(hash);

      assertEquals("TX AFTER color() should be same instance of rawTx", rawTxOption.get(), tx);
      assertEquals("MarkerOutput Index sholuld be -1", coloringEngine.getMarkerOutputIndex(tx), -1);
    }
  }

  //  INPUT
  //      COIN
  //          "00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00" : 3 : 68200
  //      ASSET : "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH" LVTest
  //          "00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00" : 2 :   600 : 8744
  //      ASSET : "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK" SVTest
  //          "b69e437d25db3ad4abe8e5e6d404a9fa8950ff79fba4f96fc37e1635791bc31e" : 1 :   600 : 2000
  //          "1bd3781629165b4c163b58be91961102bc344b917f581bd939ba16c8d2dcd231" : 1 :   600 : 1000
  //  OUTPUT
  //      ASSET ISSEUE   : "akHH9mGrHpaueMPJcyAcPJcVAagkRTUFvZJ" "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"   600 5000
  //      MARKER OUTPUT  : 5000 1000 7744 2400 600
  //      ASSET TRANSFER : "akJTSonY8BYfJg4bXrfpXmpWqeUuAdquP2j" "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"   600 1000
  //      ASSET CHANGE   : "akHH9mGrHpaueMPJcyAcPJcVAagkRTUFvZJ" "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH"   600 7744
  //      ASSET TRANSFER : "akJTSonY8BYfJg4bXrfpXmpWqeUuAdquP2j" "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"   600 2400
  //      ASSET CHANGE   : "akHH9mGrHpaueMPJcyAcPJcVAagkRTUFvZJ" "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"   600  600
  //      COIN +CHANGE    : "17KGX72xM71xV99FvYWCekabF5aFWx78US"                                       50000
  //  FEES : 10000
  private Hash hashFromHex(String hex) {
    return new Hash(new ByteArray(HexUtil.bytes(hex)));
  }

  private Hash hashFromAssetAddressBase58(String base58) throws OapException {
    AssetAddress address = AssetAddress.from(base58);
    return new Hash(new ByteArray(address.getPublicKeyHash()));
  }

  private List<OutPoint> addInputs(OutPoint... points) {
    List<OutPoint> result = new ArrayList<OutPoint>();
    for (OutPoint p : points) {
      result.add(p);
    }
    return result;
  }

  private List<NewOutput> addOuputs(NewOutput... outputs) {
    List<NewOutput> result = new ArrayList<NewOutput>();
    for (NewOutput o : outputs) {
      result.add(o);
    }
    return result;
  }

  private Transaction buildTransaction(WalletSampleDataProvider provider, List<OutPoint> inputs, List<NewOutput> newOutputs) {
    TransactionBuilder builder = TransactionBuilder.newBuilder();
    for (OutPoint p : inputs) {
      builder.addInput(provider.blockChainView(), p, builder.addInput$default$3(), builder.addInput$default$4(), provider.blockChainView().db());
    }
    for (NewOutput o : newOutputs) {
      builder.addOutput(o.amount(), o.outputOwnership());
    }
    return builder.build(builder.build$default$1(), builder.build$default$2());
  }

  @Test
  public void complexOpenAssetsTransferTest() throws OapException {
    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = OpenAssetsProtocol.get().coloringEngine();

    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    ColoringEngine ce = ColoringEngine.get();
    String[] accounts = provider.accounts();
    for (int i = 0; i < accounts.length; i++) {
      AddressData[] accountAddresses = provider.addressesOf(accounts[i]);
      AddressData issuerAddress = accountAddresses[accountAddresses.length - 1];
      AddressData watchOnlyAddress = accountAddresses[0];
      AddressData[] nextAccountAddresses = provider.addressesOf(accounts[(i + 1) % accounts.length]);
      AddressData nextRecvAddress = nextAccountAddresses[nextAccountAddresses.length - 1];

      Option<Transaction> s5TxOption = provider.blockChainView().getTransaction(provider.s5TransferTxHashes[i], provider.blockChainView().db());
      Option<Transaction> s6TxOption = provider.blockChainView().getTransaction(provider.s6IssueTxHashes[i], provider.blockChainView().db());
      Transaction s5Tx = coloringEngine.color(s5TxOption.get());
      Transaction s6Tx = coloringEngine.color(s6TxOption.get());
      OapTransactionOutput s5TxOut2 = (OapTransactionOutput) s5Tx.outputs().apply(2);
      OapTransactionOutput s6TxOut0 = (OapTransactionOutput) s6Tx.outputs().apply(0);

      int[] expectedAssetQuantities = new int[]{
        20000, s5TxOut2.getQuantity() / 2, s5TxOut2.getQuantity() - s5TxOut2.getQuantity() / 2,
        s6TxOut0.getQuantity() / 2, s6TxOut0.getQuantity() - s6TxOut0.getQuantity() / 2
      };

      AssetId[] expAssetIds = new AssetId[]{
        issuerAddress.assetId,
        issuerAddress.assetId, issuerAddress.assetId,
        watchOnlyAddress.assetId, watchOnlyAddress.assetId
      };

      long change =
        s5TxOption.get().outputs().apply(3).value() + IOapConstants.DUST_IN_SATOSHI * 2
          - 5 * IOapConstants.DUST_IN_SATOSHI
          - 2 * IOapConstants.ONE_BTC_IN_SATOSHI.longValue()
          - IOapConstants.DEFAULT_FEES_IN_SATOSHI;

      //  INPUT :
      //    S5 COIN  CHANGE   OUTPUT(3) : UNSPENT COIN OF RECEIVNING ADDRESS
      //    S5 ASSET CHANGE   OUTPUT(2) : ASSET_R : ASSET ISSUED FROM RECEIVNING ADDRESS
      //    S6 ASSET TRANSFER OUTPUT(1) : ASSET_W ASSET ISSUED FROM WATCH-ONLY ADDRESS
      List<OutPoint> inputs = addInputs(
        OutPoint.apply(provider.s5TransferTxHashes[i], 3),
        OutPoint.apply(provider.s5TransferTxHashes[i], 2),
        OutPoint.apply(provider.s6IssueTxHashes[i], 0)
      );
      //  OUTPUT
      //    ISSUE ASSET_R, 10000 TO THE RECEIVING ADDRESS
      //    SEND  COIN, 1.0 TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
      //    MARKER OUTPUT : { 10000, (index + 1) * 2000 / 2, (index + 1) * 2000 / 2, 5000, 5000 }
      //    SEND  ASSET_R, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
      //    SEND  ASSET_R, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
      //    SEND  COIN, 1.0 TO THE WATCH-ONLY ADDRESS
      //    SEND  ASSET_W, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
      //    SEND  ASSET_W, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
      //    SEND  COIN CHANGE TO THE RECEIVING ADDRESS
      List<NewOutput> newOutputs = addOuputs(
        NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
        NewOutput.apply(CoinAmount.apply(BigDecimal.valueOf(1)), nextRecvAddress.address),
        markerOutput(expectedAssetQuantities, issuerAddress.assetDefinitionPointer.getPointer()),
        NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), nextRecvAddress.address),
        NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
        NewOutput.apply(CoinAmount.apply(BigDecimal.valueOf(1)), watchOnlyAddress.address),
        NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), nextRecvAddress.address),
        NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
        NewOutput.apply(CoinAmount.from(change), issuerAddress.address)
      );
      Transaction tx = buildTransaction(provider, inputs, newOutputs);
      Transaction t = coloringEngine.color(tx);
      assertTrue("", (t instanceof OapTransaction));
      OapTransaction ct = (OapTransaction) t;
      int makerOutputIndex = ce.getMarkerOutputIndex(tx);
      OapMarkerOutput marker = (OapMarkerOutput) ct.outputs().apply(makerOutputIndex);
      assertArrayEquals("MARKER OUTPUT QUANTITIES", expectedAssetQuantities, marker.getQuantities());
      assertArrayEquals("MARKER OUTPUT METADATA", issuerAddress.assetDefinitionPointer.getPointer(), marker.getMetadata());
      int qindex = 0;
      for (int oindex = 0; oindex < ct.outputs().size(); oindex++) {
        TransactionOutput out = ct.outputs().apply(oindex);
        if (out.value() == IOapConstants.DUST_IN_SATOSHI) {
          assertEquals("THE QUANTITY OF EACH OUTPUT SHOUL BE EQUAL", expectedAssetQuantities[qindex], ((OapTransactionOutput) out).getQuantity());
          assertEquals("THE ASSET ID OF EACH OUTPUT SHOUL BE EQUAL", expAssetIds[qindex], ((OapTransactionOutput) out).getAssetId());
          qindex++;
        }
      }
    }
  }


  //
  //  USE  STEP 5 ASSET TRANSFER OUTPUTS, STEP 6 ASSET ISSUE OUTPUTS
  //
  //  INPUT :
  //    S5 COIN  CHANGE   OUTPUT(3) : UNSPENT COIN OF RECEIVNING ADDRESS
  //    S5 ASSET CHANGE   OUTPUT(2) : ASSET_R : ASSET ISSUED FROM RECEIVNING ADDRESS
  //    S6 ASSET TRANSFER OUTPUT(1) : ASSET_W ASSET ISSUED FROM WATCH-ONLY ADDRESS
  //  OUTPUT
  //    ISSUE ASSET_R, 10000 TO THE RECEIVING ADDRESS
  //    SEND  COIN, 1.0 TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
  //    MARKER OUTPUT : { 10000, (index + 1) 8 2000 / 2, (index + 1) 8 2000 / 2, 5000, 5000 }
  //    SEND  ASSET_R, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
  //    SEND  ASSET_R, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
  //    SEND  COIN, 1.0 TO THE WATCH-ONLY ADDRESS
  //    SEND  ASSET_W, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
  //    SEND  ASSET_W, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
  //    SEND  COIN CHANGE TO THE RECEIVING ADDRESS

  private NewOutput markerOutput(int[] quantities, byte[] definitionPointer) throws OapException {
    NewOutput markerOutput = NewOutput.apply(
      CoinAmount.from(0L),
      ParsedPubKeyScript.from(OapMarkerOutput.lockingScriptFrom(quantities, definitionPointer))
    );
    return markerOutput;
  }

  @Test
  public void colorCannotAssignIdAndQuantityTest() throws OapException {
    thrown.expect(OapException.class);
    thrown.expectMessage("Cannot assign asset id and asset quantity for index:");

    WalletSampleDataProvider provider = getDataProvider();
    ColoringEngine coloringEngine = OpenAssetsProtocol.get().coloringEngine();
    OapBlockchain chain = OpenAssetsProtocol.get().chain();
    ColoringEngine ce = ColoringEngine.get();

    int SENDER_INDEX = 1;
    String[] accounts = provider.accounts();
    AddressData[] accountAddresses = provider.addressesOf(accounts[SENDER_INDEX]);
    AddressData issuerAddress = accountAddresses[accountAddresses.length - 1];
    AddressData watchOnlyAddress = accountAddresses[0];
    AddressData[] nextAccountAddresses = provider.addressesOf(accounts[(SENDER_INDEX + 1) % accounts.length]);
    AddressData nextRecvAddress = nextAccountAddresses[nextAccountAddresses.length - 1];

    Option<Transaction> s5TxOption = provider.blockChainView().getTransaction(provider.s5TransferTxHashes[SENDER_INDEX], provider.blockChainView().db());
    Option<Transaction> s6TxOption = provider.blockChainView().getTransaction(provider.s6IssueTxHashes[SENDER_INDEX], provider.blockChainView().db());
    Transaction s5Tx = coloringEngine.color(s5TxOption.get());
    Transaction s6Tx = coloringEngine.color(s6TxOption.get());
    OapTransactionOutput s5TxOut2 = (OapTransactionOutput) s5Tx.outputs().apply(2);
    OapTransactionOutput s6TxOut0 = (OapTransactionOutput) s6Tx.outputs().apply(0);

    // INVALID QUANTITY
    int[] expectedAssetQuantities = new int[]{
      20000,
      s5TxOut2.getQuantity(), s5TxOut2.getQuantity(),
      s6TxOut0.getQuantity(), s6TxOut0.getQuantity()
    };
    AssetId[] expAssetIds = new AssetId[]{
      issuerAddress.assetId,
      issuerAddress.assetId, issuerAddress.assetId,
      nextRecvAddress.assetId, issuerAddress.assetId
    };
    long change =
      s5TxOption.get().outputs().apply(3).value() + IOapConstants.DUST_IN_SATOSHI * 2
        - 5 * IOapConstants.DUST_IN_SATOSHI
        - 2 * IOapConstants.ONE_BTC_IN_SATOSHI.longValue()
        - IOapConstants.DEFAULT_FEES_IN_SATOSHI;

    //  INPUT :
    //    S5 COIN  CHANGE   OUTPUT(3) : UNSPENT COIN OF RECEIVNING ADDRESS
    //    S5 ASSET CHANGE   OUTPUT(2) : ASSET_R : ASSET ISSUED FROM RECEIVNING ADDRESS
    //    S6 ASSET TRANSFER OUTPUT(1) : ASSET_W ASSET ISSUED FROM WATCH-ONLY ADDRESS
    List<OutPoint> inputs = addInputs(
      OutPoint.apply(provider.s5TransferTxHashes[SENDER_INDEX], 3),
      OutPoint.apply(provider.s5TransferTxHashes[SENDER_INDEX], 2),
      OutPoint.apply(provider.s6IssueTxHashes[SENDER_INDEX], 0)
    );
    //  OUTPUT
    //    ISSUE ASSET_R, 10000 TO THE RECEIVING ADDRESS
    //    SEND  COIN, 1.0 TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
    //    MARKER OUTPUT : { 10000, (index + 1) * 2000 / 2, (index + 1) * 2000 / 2, 5000, 5000 }
    //    SEND  ASSET_R, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
    //    SEND  ASSET_R, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
    //    SEND  COIN, 1.0 TO THE WATCH-ONLY ADDRESS
    //    SEND  ASSET_W, HALF OF SPENDING QUANTITY TO THE RECEIVING ADDRESS OF THE NEXT ACCOUNT
    //    SEND  ASSET_W, HALF OF SPENDING QUANTITY AS ASSET CHANGE TO THE RECEIVING ADDRESS
    //    SEND  COIN CHANGE TO THE RECEIVING ADDRESS
    List<NewOutput> newOutputs = addOuputs(
      NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
      NewOutput.apply(CoinAmount.apply(BigDecimal.valueOf(1)), nextRecvAddress.address),
      markerOutput(expectedAssetQuantities, issuerAddress.assetDefinitionPointer.getPointer()),
      NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), nextRecvAddress.address),
      NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
      NewOutput.apply(CoinAmount.apply(BigDecimal.valueOf(1)), watchOnlyAddress.address),
      NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), nextRecvAddress.address),
      NewOutput.apply(CoinAmount.from(IOapConstants.DUST_IN_SATOSHI), issuerAddress.address),
      NewOutput.apply(CoinAmount.from(change), issuerAddress.address)
    );
    Transaction tx = buildTransaction(provider, inputs, newOutputs);
    Transaction t = coloringEngine.color(tx);
  }
}
