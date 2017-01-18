package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointerTest;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefintionTest;
import io.scalechain.blockchain.oap.coloring.ColoringEngineTest;
import io.scalechain.blockchain.oap.coloring.OapWalletTest;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutputTest;
import io.scalechain.blockchain.oap.transaction.OapTransactionTest;
import io.scalechain.blockchain.oap.util.LEB128Test;
import io.scalechain.blockchain.oap.util.PairTest;
import io.scalechain.blockchain.oap.wallet.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by shannon on 16. 12. 8.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  LEB128Test.class,
  PairTest.class,
  CaseClassTest.class,
  AddressUtilMainNetTest.class,
  AddressUtilTestNetTest.class,
  AssetAddressMainNetTest.class,
  AssetAddressTestNetTest.class,
  AssetDefinitionPointerTest.class,
  AssetDefintionTest.class,
  AssetIdMainNetTest.class,
  AssetIdTestNetTest.class,
  AssetTransferTest.class,
  OapTransactionTest.class,
  OpenAssetsProtocolTest.class,
  OapMarkerOutputTest.class,
  OapStorageTest.class,
  ColoringEngineTest.class,
  OapWalletTest.class,
  WalletHandlerTest.class,
  AssetDefinitionHandlerTest.class,
  IssueAssetHandlerTest.class,
  TransferAssetHandlerLagacyTest.class,
  TransferAssetHandlerTest.class
})
public class OapJUnitUnitTestSuite {
}
