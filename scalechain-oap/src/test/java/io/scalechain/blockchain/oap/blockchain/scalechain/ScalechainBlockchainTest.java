package io.scalechain.blockchain.oap.blockchain.scalechain;

import io.scalechain.blockchain.oap.OpenAssetsProtocol;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by shannon on 16. 12. 15.
 */
public class ScalechainBlockchainTest {

    @Test
    public void getChainTest() {
        OapBlockchain chain = OpenAssetsProtocol.get().chain();

        assertNotNull("WALLET SHOULD BE CREATED", chain);
    }

    @Test
    public void getTransactionTest() {

    }

    @Test
    public void getTransactionOutputTest() {
    }
}
