package io.scalechain.blockchain.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.scalechain.blockchain.api.sampledata.TransactionDataProvider;
import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.cli.ScaleChainStarter;
import io.scalechain.wallet.Wallet;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shannon on 17. 1. 9.
 */
public class ApiTestWithSampleTransactions {
  static File storagePath = new File("./build");
  public static RpcInvoker rpcInvoker = new RpcInvoker("localhost", 8080, "user", "pleasechangethispassword123@.@");
  public static TransactionDataProvider provider = null;
  @Rule
  public TestName testName = new TestName();

  @BeforeClass
  public static void setUpForClass() throws Exception {
    System.out.println("TestWithSampleTransactions.setUpForClass()");
    // Start ScaleChainPeer
    //
    if (provider == null) {
      if (clearStorage(storagePath) == 0) {
        if (!storagePath.exists()) {
          storagePath.mkdir();
          System.out.println("setUpForClass(): " + storagePath + " created.");
        } else {
          System.out.println("setUpForClass(): " + storagePath + " exists.");
        }
      }

      ArrayList<String> args = new ArrayList<String>(Arrays.asList("--network", "testnet", "--minerInitialDelayMS", "1000","--minerHashDelayMS", "500"));
      ScaleChainStarter.start( args );

      System.out.println("DEBUG:The provider was null. Waiting for blocks to be mined.");
      provider = new TransactionDataProvider(Blockchain.get(), Wallet.get());;

      // Wait at least 3 blocks mined.
      // This will make sure that transfering coins from mining acoount to each test account will succeed.
      waitForChain(3);

      provider.populate();
    } else {
      System.out.println("DEBUG:The provider was not null. No need to wait.");
    }
  }

  @AfterClass
  public static void tearDownClass() {
/*
    System.out.println("TestWithSampleTransactions.tearDownClass()");
    try {
      clearStorage(storagePath);
    }catch(Exception e) {
      // BUGBUG : Is it ok to ignore exception?
    }
*/
    //provider.dispose();
    //OpenAssetsProtocol.get().storage().close();
    //
    // Shutdown ScaleChainPeer : HOW?
    //
  }

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

/*
  private static void reigsterShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          clearStorage(storagePath);
        }catch(Exception e) {
          // BUGBUG : Is it ok to ignore exception?
        }
      }
    });
  }
*/
  protected static void waitForChain(int h) {
    long blockHeight = Blockchain.get().getBestBlockHeight();
    while(true) {
      if (Blockchain.get().getBestBlockHeight() - blockHeight > h)  break;
      try { Thread.sleep(10); } catch(InterruptedException ie) {}
    }
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }



  //
  // RPC APIs
  //
  public JsonArray getAddressesByAccount(String account) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    if (account != null) {
      params.add(account);
    }
    return rpcInvoker.invoke("getaddressesbyaccount", params).get("result").getAsJsonArray();
  }

  public BigDecimal getBalance(String account, int confirmations, boolean includeWatchOnly) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(account);
    params.add(confirmations);
    params.add(includeWatchOnly);
    return rpcInvoker.invoke("getbalance", params).get("result").getAsBigDecimal();
  }

  public JsonArray getAssetBalnce(String account, int confirmations, boolean includeWatchOnly, List<String> assetIds) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(account);
    params.add(confirmations);
    params.add(includeWatchOnly);
    JsonArray array = new JsonArray();
    for(String assetId : assetIds) {
      array.add(assetId);
    }
    params.add(array);
    return rpcInvoker.invoke("getassetbalance", params).get("result").getAsJsonArray();
  }

  public String sendMany(String account, JsonObject outputs, String comment, List<String> substractFeesFromAmount) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(account);
    params.add(outputs);
    if(comment != null) {
      params.add(comment);
      if(substractFeesFromAmount != null) {
        JsonArray array = new JsonArray();
        for(String address : substractFeesFromAmount) {
          array.add(address);
        }
      }
    }
    System.out.println(params);
    return rpcInvoker.invoke("sendmany", params).get("result").getAsString();
  }

  public JsonArray listAssetTransactions(String account, int count, long skip, boolean includeWatchOnly) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    if (account != null) {
      params.add(account);
      params.add(count);
      params.add(skip);
      params.add(includeWatchOnly);
    }
    return rpcInvoker.invoke("listassettransactions", params).get("result").getAsJsonArray();
  }

  public JsonObject getAssetDefinition(String hashOrAssetId) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(hashOrAssetId);
    return rpcInvoker.invoke("getassetdefinition", params).get("result").getAsJsonObject();
  }

  public JsonObject createAssetDefinition(String assetIdOrAssetAddress, JsonObject metadata) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(assetIdOrAssetAddress);
    if (metadata != null) params.add(metadata);
    return rpcInvoker.invoke("createassetdefinition", params).get("result").getAsJsonObject();
  }

  //  val issuerAddress:     String               = request.params.get[String]("issuer_address", 0)
  //  val toAddress:         String               = request.params.get[String]("to_address", 1)
  //  val quantity:          Int                  = request.params.get[Int]("quantity", 2)
  //  val hashOption:        Option[String]       = request.params.getOption[String]("hash", 3)
  //  val privateKeyStrings: Option[List[String]] = request.params.getListOption[String]("private_keys", 4)
  //  val changeAddress:     String               = request.params.getOption[String]("change_address", 5).getOrElse(issuerAddress)
  //  val fees: Long                              = request.params.getOption[Long]("fees", 6).getOrElse(IOapConstants.DEFAULT_FEES_IN_SATOSHI)
  //  val metadataOption:  Option[JsObject]       = request.params.getOption[JsObject]("metadata", 7)
  public JsonObject issueAsset(String issuerAddress, String toAddress, int quantity, String hash, List<String> privateKeys, String changeAddress, long fees, JsonObject metadata) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(issuerAddress);
    params.add(toAddress);
    params.add(quantity);
    if (hash != null) {
      params.add(hash);
      if (privateKeys != null) {
        JsonArray array = new JsonArray();
        for(String key : privateKeys) {
          array.add(key);
        }
        params.add(array);
        if (changeAddress != null) {
          params.add(changeAddress);
          params.add(fees);
          if (metadata!=null) {
            params.add(metadata);
          }
        }
      }
    }
    System.out.println(params);
    return rpcInvoker.invoke("issueasset", params).get("result").getAsJsonObject();
  }


  public JsonObject transferAsset(String fromAddress, JsonArray tos, List<String> privateKeys, String changeAddress, long fees) throws RpcInvoker.RpcCallException {
    JsonArray params = new JsonArray();
    params.add(fromAddress);
    params.add(tos);
    if (privateKeys != null) {
      JsonArray array = new JsonArray();
      for(String key : privateKeys) {
        array.add(key);
      }
      params.add(array);
      if (changeAddress != null) {
        params.add(changeAddress);
        params.add(fees);
      }
    }
    return rpcInvoker.invoke("transferasset", params).get("result").getAsJsonObject();
  }
}
