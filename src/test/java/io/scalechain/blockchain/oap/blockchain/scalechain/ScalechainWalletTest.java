package io.scalechain.blockchain.oap.blockchain.scalechain;

import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.blockchain.OapWallet;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by shannon on 16. 12. 1.
 */
public class ScalechainWalletTest {

    @Test
    public void getWalletTest() {
        OapWallet wallet = OpenAssetsProtocol.get().wallet();
        assertNotNull("WALLET SHOULD BE CREATED", wallet);
    }
}