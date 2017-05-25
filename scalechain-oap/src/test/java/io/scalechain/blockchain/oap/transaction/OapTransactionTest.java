package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by shannon on 16. 12. 19.
 */
public class OapTransactionTest {
    @BeforeClass
    public static void setUpForClass() {
        ChainEnvironment.create("mainnet");
    }

    @Test
    public void getMarkerOutputTest() throws OapException {
        int[] assetQuantities = new int[] { 1000 };
        byte[] metadata = "METADATA".getBytes();

        CoinAddress address = CoinAddress.from("17KGX72xM71xV99FvYWCekabF5aFWx78US");
        LockingScript lockingScript = address.lockingScript();
        List<OapTransactionOutput> outputs = new ArrayList<OapTransactionOutput>();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        outputs.add(new OapTransactionOutput(
                AssetId.from(address),
                1000,
                new TransactionOutput(600, lockingScript)
        ));
        outputs.add(new OapMarkerOutput(AssetId.from(address), assetQuantities, metadata));
        inputs.add(new NormalTransactionInput(
                    new Hash(new Bytes(HexUtil.bytes("00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00"))),
                    0,
                    new UnlockingScript(new Bytes(new byte[] { (byte)0} )),
                    0
            )
        );

        OapTransaction tx = new OapTransaction(
                1,
                inputs,
                outputs,
                0
        );

        assertEquals("TX OUTPUTS COUNT", 2, tx.getOapOutputs().size());
        assertEquals("MARKER OUPUT INDEX SHOULD BE 1", 1, tx.getMarkerOutputIndex());
        assertEquals("MARKER OUTPUT VALUE", 0, tx.getMarkerOutput().getTransactionOutput().getValue());
        assertArrayEquals("MARKER OUTPUT ASSET QUANTITIES", assetQuantities, tx.getMarkerOutput().getQuantities());
        assertArrayEquals("MARKER OUTPUT ASSET METADATA", metadata, tx.getMarkerOutput().getMetadata());

        assertTrue("TX OUTPUT[0] SOULD BE colored", tx.getOapOutputs().get(0).isColored());
        assertTrue("TX OUTPUT[0] SOULD BE OapTransactionOutput", tx.getOapOutputs().get(0) instanceof OapTransactionOutput);
        OapTransactionOutput o = (OapTransactionOutput)tx.getOapOutputs().get(0);
        assertEquals("OapTransactionOutput SHOULD HAVE VALUE", 600, o.getTransactionOutput().getValue());
        assertEquals("ASSET ID", AssetId.from(address), o.getAssetId());
        assertEquals("ASSET QUANTITY", 1000, o.getQuantity());
    }

    @Test
    public void toStringTest() throws OapException {
        int[] assetQuantities = new int[] { 1000 };
        byte[] metadata = "METADATA".getBytes();

        CoinAddress address = CoinAddress.from("17KGX72xM71xV99FvYWCekabF5aFWx78US");
        LockingScript lockingScript = address.lockingScript();
        List<OapTransactionOutput> outputs = new ArrayList<OapTransactionOutput>();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        outputs.add(new OapTransactionOutput(
          AssetId.from(address),
          1000,
          new TransactionOutput(600, lockingScript)
        ));
        outputs.add(new OapMarkerOutput(AssetId.from(address), assetQuantities, metadata));
        inputs.add(new NormalTransactionInput(
          new Hash(new Bytes(HexUtil.bytes("00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00"))),
          0,
          new UnlockingScript(new Bytes(new byte[] { (byte)0} )),
          0
          )
        );

        OapTransaction tx = new OapTransaction(
          1,
          inputs,
          outputs,
          0
        );

        String txString = tx.toString();
        System.out.println(txString);
        for(OapTransactionOutput output: outputs) {
            assertTrue("OapTransaction should contain Output", txString.contains(output.getTransactionOutput().toString()));
        }
    }
}
