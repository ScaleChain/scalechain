package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by shannon on 16. 12. 8.
 */
public class OapMarkerOutputTest {
    static {
        System.out.println(OapMarkerOutputTest.class.getName()+"."+" LOADED.");
    }

    @Test
    public void markerForIssueanceTest() throws OapException {
        String[] hex ={
                // TX : 6be3c9fc44bdd9a325e54fafd83280c9a3a58dc34f0e5780636de347361dce24
                // AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36를 1000000주 발행
                "6a244f41010001c0843d1b753d68747470733a2f2f6370722e736d2f5969364b6647347a386f"
        };
        String[] assetId = {
                "AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36"
        };

        int[][] quantities = {
                { 1000000 }
        };
        String[] metadata = {
                //u=https://cpr.sm/Yi6KfG4z8o
                "753d68747470733a2f2f6370722e736d2f5969364b6647347a386f"
        };
        for(int i = 0;i < hex.length;i++) {
            OapMarkerOutput markerOutput = new OapMarkerOutput(
                    null,
                    quantities[i],
                    HexUtil.bytes(metadata[i])
            );
            assert hex[i].equals(
                    HexUtil.hex(
                            markerOutput.getTransactionOutput().getLockingScript().getData().getArray()
                    )
            );
            assertEquals("Value of Marker Ouput should be 0", 0, markerOutput.getTransactionOutput().getValue());
            assertArrayEquals("Metadata should be eqaul", HexUtil.bytes(metadata[i]), markerOutput.getMetadata());
            assertArrayEquals("Asset Quantities should be equal", quantities[i], markerOutput.getQuantities());
        }
    }

    //OapMarkerOutput markerOutput = new OapMarkerOutput(null, qts, new byte[0]);
    @Test
    public void markerForTransferTest() throws OapException {
        String[] hex = {
                // TX : ee58df504a5d2df48359285322b7c8de6893a7f417eac6fe4d6f30302530a52e
                // 150개의 AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36(Banana Stand Stock)를 15개 이체하고 135를 CHANGE로 받는 TX
                "6a094f410100020f870100",
                "6a0a4f41010002f4039c4a00"
        };
        int[][] quantities = {
                { 15, 135 },
                { 500, 9500 }
        };
        byte[] emptyMetadata = new byte[0];
        for (int i = 0; i < hex.length; i++) {
            OapMarkerOutput markerOutput = new OapMarkerOutput(
                    null,
                    quantities[i],
                    emptyMetadata
            );
            assert hex[i].equals(
                    HexUtil.hex(
                            markerOutput.getTransactionOutput().getLockingScript().getData().getArray()
                    )
            );
            String s = markerOutput.toString();
            assertEquals("Value of Marker Ouput should be 0", 0, markerOutput.getTransactionOutput().getValue());
            assertEquals("Metadata should be eqaul", 0, markerOutput.getMetadata().length);
            assertArrayEquals("Asset Quantities should be equal", quantities[i], markerOutput.getQuantities() );
        }
    }

    @Test
    public void markerForColoringIssuanceTest() {
    }

    @Test
    public void stripOpReturnTest() throws OapException {
        String[] hex ={
                // TX : 6be3c9fc44bdd9a325e54fafd83280c9a3a58dc34f0e5780636de347361dce24
                // AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36를 1000000주 발행
                "6a244f41010001c0843d1b753d68747470733a2f2f6370722e736d2f5969364b6647347a386f",
                // TX : ee58df504a5d2df48359285322b7c8de6893a7f417eac6fe4d6f30302530a52e
                // 150개의 AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36(Banana Stand Stock)를 15개 이체하고 135를 CHANGE로 받는 TX
                "6a094f410100020f870100",
                "6a0a4f41010002f4039c4a00"
        };
        String[] expectedhex ={
                // TX : 6be3c9fc44bdd9a325e54fafd83280c9a3a58dc34f0e5780636de347361dce24
                // AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36를 1000000주 발행
                "4f41010001c0843d1b753d68747470733a2f2f6370722e736d2f5969364b6647347a386f",
                // TX : ee58df504a5d2df48359285322b7c8de6893a7f417eac6fe4d6f30302530a52e
                // 150개의 AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36(Banana Stand Stock)를 15개 이체하고 135를 CHANGE로 받는 TX
                "4f410100020f870100",
                "4f41010002f4039c4a00"
        };

        AssetId[] assetId = {
                AssetId.from("AHthB6AQHaSS9VffkfMqTKTxVV43Dgst36"),
                null,
                null,
        };

        byte[][] metadata = {
                "u=https://cpr.sm/Yi6KfG4z8o".getBytes(),
                new byte[0],
                new byte[0],
        };
        int[][] quantities = {
                { 1000000 },
                { 15, 135 },
                { 500, 9500 }
        };
        for(int i = 0;i < hex.length;i++) {
            OapMarkerOutput markerOutput = new OapMarkerOutput(
                    assetId[i],
                    quantities[i],
                    metadata[i]
            );
            assertEquals("STRIPED LOCKING SCRIPT", expectedhex[i],
                    HexUtil.hex(
                            OapMarkerOutput.stripOpReturnFromLockScript(markerOutput.getTransactionOutput().getLockingScript())
                    )
            );
        }
        // ADDRESS         : 17KGX72xM71xV99FvYWCekabF5aFWx78US
        // PUBLIC KEY HASH : 45453257d7fb46d6eb3683d0dd062dfa793bc5a8
        // LOCKING SCRIPT  : 76a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac
        LockingScript lockingScript = new LockingScript(new Bytes(HexUtil.bytes("76a91445453257d7fb46d6eb3683d0dd062dfa793bc5a888ac")));
        byte[] expected = lockingScript.getData().getArray();
        byte[] actual = OapMarkerOutput.stripOpReturnFromLockScript(lockingScript);
        assertArrayEquals("NON MARKER OUTPUT LockingScipt", expected, actual);
    }
}
