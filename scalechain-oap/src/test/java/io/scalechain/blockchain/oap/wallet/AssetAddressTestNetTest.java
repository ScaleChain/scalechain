package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.transaction.ChainEnvironment$;
import org.junit.BeforeClass;

/**
 * Created by shannon on 16. 12. 6.
 */
public class AssetAddressTestNetTest extends AssetAddressMainNetTest {

    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AssetAddressTestNetTest.class.getName() + ".setupForClass()");
        ChainEnvironment$.MODULE$.create("testnet");
        coinAddresses = new String[] {
                "mzhdcviYb4gyPQLzrSqhC38vnMBUGescmG",
                "n3BTwZ2pP3gB39Nu8Q9eUhgpUYS7c1P3gB",
                "my9TtJmNYfuFPoFBntieEnLMSy8hUYrrzG",   // Coinprism Colored Address of lightvil
                "mfgNtCxPzsYBf5Nj9yfzdXA9U76rSc2ZMh"    // Coinprism Colored Address of shinyvil
        };
        assetAddresses = new String[] {
                "bXAfWs6Xt4YafFeWMu4wsqu3W7xMeKeNbbC",
                "bXD9MBir9rXZruPYGB2Fq8ZbPp9cHZiBvzg",
                "bX97M8Uai29nwG3QYqWppteEvnaJsRw8n4J",
                "bWqeG8NmjUMRsXKY6CbnBHP4ioiH2N9saBz"
        };
    }
}