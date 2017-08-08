package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.blockchain.OapWallet;
import io.scalechain.blockchain.oap.blockchain.mockup.TestBlockchainInterface;
import io.scalechain.blockchain.oap.blockchain.mockup.TestWalletInterface;
import io.scalechain.blockchain.oap.coloring.ColoringEngine;
import io.scalechain.blockchain.oap.sampledata.WalletSampleDataProvider;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Created by shannon on 17. 1. 5.
 */
public class TestWithWalletSampleData {
  public static OapWallet              wallet;

  public static WalletHandler          walletHandler;
  public static IssueAssetHandler      issueHandler;
  public static TransferAssetHandler   transferHandler;
  public static AssetDefinitionHandler definitionHandler;
  public static WalletSampleDataProvider provider;
  public static File storagePath = new File("./build");
  public static ColoringEngine coloringEngine;


  public  WalletSampleDataProvider getDataProvider() {
    return provider;
  }

  @Rule
  public TestName runningTestName = new TestName();


  private static int clearStorage(File path) {
    int count = 0;
    if (path.exists()) {
      File[] files = storagePath.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          if (file.getName().startsWith("oap-") || file.getName().startsWith("blockstorage-"))
            return true;
          else
            return false;
        }
      });
      for(File file : files) {
        if (file.isDirectory()) {
          try {
            FileUtils.deleteDirectory(file);
            count++;
          } catch(IOException ioe) {

          }
        }
      }
    }
    return count;
  }


  private static void reigsterShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          clearStorage(storagePath);
        }catch(Exception e) {
        }
      }
    });
  }


  @BeforeClass
  public static void setUpForClass() throws Exception {
    //
    // Remove storage files
    //
    storagePath = new File("./build");
    if (clearStorage(storagePath) == 0) {
      if (!storagePath.exists()) {
        storagePath.mkdir();
        System.out.println("setUpForClass(): " + storagePath + " created.");
      } else {
        System.out.println("setUpForClass(): " + storagePath + " exists.");
      }
    }
    reigsterShutdownHook();

    WalletSampleDataProvider.init("testnet", storagePath);
  }

  @Before
  public void setUp() throws Exception {
    String testName = getClass().getSimpleName() + "-" + runningTestName.getMethodName();
    provider = WalletSampleDataProvider.create(
      testName
    );
    TestBlockchainInterface chainInterface = new TestBlockchainInterface(provider);
    TestWalletInterface walletInterface = new TestWalletInterface(provider.blockChainView(), provider.wallet());
    OpenAssetsProtocol.create(
      chainInterface,
      walletInterface,
      new File(storagePath, "oap-stroage-"+testName)
    );


    coloringEngine    = OpenAssetsProtocol.get().coloringEngine();
    wallet            = OpenAssetsProtocol.get().wallet();

    walletHandler     = WalletHandler.get();
    issueHandler      = IssueAssetHandler.get();
    transferHandler   = TransferAssetHandler.get();
    definitionHandler = AssetDefinitionHandler.get();
  }

  @After
  public void tearDown() {
    WalletSampleDataProvider.destroy(provider);
    OpenAssetsProtocol.get().storage().close();
  }

  @AfterClass
  public static void tearDownClass() {
    System.out.println("TestWithWalletSampleData.tearDownClass()");
  }
}

