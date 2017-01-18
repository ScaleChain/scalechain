package io.scalechain.blockchain.api;

import io.scalechain.blockchain.api.oap.*;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnitSuite Class for OAP test.
 * This suite won't run due to setup configuration problem.
 *
 * Created by shannon on 17. 1. 3.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
  AssetDefinitionPointerTest.class,
  WalletApiTest.class,
  OapWalletApiTest.class,
  SendManyTest.class,
  SendManyApiTest.class,
  IssueAssetTest.class,
  IssueAssetApiTest.class,
  TransferAssetTest.class,
  TransferAssetApiTest.class
})
public class NetLayerUnitTestSuite {
}
