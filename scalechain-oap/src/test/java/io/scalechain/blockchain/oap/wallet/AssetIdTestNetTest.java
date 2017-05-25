package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by shannon on 16. 12. 8.
 */
public class AssetIdTestNetTest extends AssetIdMainNetTest {
    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AssetIdTestNetTest.class.getName() + ".setupForClass()");
        ChainEnvironment.create("testnet");
        coinAddresses = new String[] {
                "mz3fAEjRE3EWgz5NQRgLyYRbUxQqMU7CQY",
                "n46wToj3QY9T7df5sV8Znd4oASK3msdab7",
                "mjBwpAMDVF6JkxPQA3m3Uhfm2ZFW5w4U7A",   // Coinprism Colored Address of lightvil
                "mjRjnT4R9Rrpn6xrnBXzbfYC4YSyWeXjCT"    // Coinprism Colored Address of shinyvil
        };

        assetIds = new String[] {
                "oUueFnqWfLnfQXsFELjGGb3SawkjSxcX1L",
                "oMC6bsBGJSGLhyn66MQh9r2VPQ29t8ceri",
                "odMhVqbtCUMnHijpZ1BqjurmWYerQ6M5ub",
                "oHKBeY5HQhHFMAnNcuYV2BpAo42cnyqktB"
        };
    }
//    @Test
//    public void generateTest() throws OapException {
//        for(int i = 0;i < 4;i++) {
//            PrivateKey privateKey = PrivateKey.generate();
//            CoinAddress coinAddress = CoinAddress.from(privateKey);
//            AssetId assetId = AssetId.fromCoinAddress(coinAddress);
//            System.out.println(coinAddress.base58());
//            System.out.println(assetId.base58());
//        }
//    }

    @Test
    public void p2pkhScriptHashTest() {
        // Hash160 of Pay to Pub Key Hash Script of "mfcSEPR8EkJrpX91YkTJ9iscdAzppJrG9j"
        //   should be equal to that of 16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM.
        // 16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM  ==> 0,   010966776006953d5567439e5e39f86a0d273bee
        // mfcSEPR8EkJrpX91YkTJ9iscdAzppJrG9j ==> 111, 010966776006953d5567439e5e39f86a0d273bee
        String coinAddress = "mfcSEPR8EkJrpX91YkTJ9iscdAzppJrG9j";
        String p2shScriptHash = "36e0ea8e93eaa0285d641305f4c81e563aa570a2";
        String expectedHash = HexUtil.hex(
                AssetId.p2pkhScriptHash(
                        CoinAddress.from(coinAddress).getPublicKeyHash().getArray()
                )
        );
        assert p2shScriptHash.equals(expectedHash);
    }

}
