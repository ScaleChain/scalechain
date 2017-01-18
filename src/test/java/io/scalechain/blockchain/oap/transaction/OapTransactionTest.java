package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.ByteArray;
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
        ChainEnvironment$.MODULE$.create("mainnet");
    }

    @Test
    public void getMarkerOutputTest() throws OapException {
        int[] assetQuantities = new int[] { 1000 };
        byte[] metadata = "METADATA".getBytes();

        CoinAddress address = CoinAddress.from("17KGX72xM71xV99FvYWCekabF5aFWx78US");
        LockingScript lockingScript = address.lockingScript();
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        outputs.add(new OapTransactionOutput(
                AssetId.from(address),
                1000,
                TransactionOutput.apply(600, lockingScript)
        ));
        outputs.add(new OapMarkerOutput(AssetId.from(address), assetQuantities, metadata));
        inputs.add(NormalTransactionInput.apply(
                    new Hash(new ByteArray(HexUtil.bytes("00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00"))),
                    0,
                    new UnlockingScript(new ByteArray(new byte[] { (byte)0} )),
                    0
            )
        );

        OapTransaction tx = new OapTransaction(
                1,
                JavaConverters.asScalaBuffer(inputs).toList(),
                JavaConverters.asScalaBuffer(outputs).toList(),
                0
        );

        assertEquals("TX OUTPUTS COUNT", 2, tx.outputs().size());
        assertEquals("MARKER OUPUT INDEX SHOULD BE 1", 1, tx.getMarkerOutputIndex());
        assertEquals("MARKER OUTPUT VALUE", 0, tx.getMarkerOutput().value());
        assertArrayEquals("MARKER OUTPUT ASSET QUANTITIES", assetQuantities, tx.getMarkerOutput().getQuantities());
        assertArrayEquals("MARKER OUTPUT ASSET METADATA", metadata, tx.getMarkerOutput().getMetadata());

        assertTrue("TX OUTPUT[0] SOULD BE OapTransactionOutput", tx.outputs().apply(0) instanceof OapTransactionOutput);
        OapTransactionOutput o = (OapTransactionOutput)tx.outputs().apply(0);
        assertEquals("OapTransactionOutput SHOULD HAVE VALUE", 600, o.value());
        assertEquals("ASSET ID", AssetId.from(address), o.getAssetId());
        assertEquals("ASSET QUANTITY", 1000, o.getQuantity());
    }

    @Test
    public void toStringTest() throws OapException {
        int[] assetQuantities = new int[] { 1000 };
        byte[] metadata = "METADATA".getBytes();

        CoinAddress address = CoinAddress.from("17KGX72xM71xV99FvYWCekabF5aFWx78US");
        LockingScript lockingScript = address.lockingScript();
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        outputs.add(new OapTransactionOutput(
          AssetId.from(address),
          1000,
          TransactionOutput.apply(600, lockingScript)
        ));
        outputs.add(new OapMarkerOutput(AssetId.from(address), assetQuantities, metadata));
        inputs.add(NormalTransactionInput.apply(
          new Hash(new ByteArray(HexUtil.bytes("00a65162187e76037fa6170bcdc558cd8115c12743243142cd9111df67db9d00"))),
          0,
          new UnlockingScript(new ByteArray(new byte[] { (byte)0} )),
          0
          )
        );

        OapTransaction tx = new OapTransaction(
          1,
          JavaConverters.asScalaBuffer(inputs).toList(),
          JavaConverters.asScalaBuffer(outputs).toList(),
          0
        );

        String txString = tx.toString();
        for(TransactionOutput output: outputs) {
            assertTrue("Tx.toSting() should cointain TxOuput.toString()", txString.contains(output.toString()));
        }
    }
}
