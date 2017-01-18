package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.oap.transaction.OapMarkerOutput;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by shannon on 16. 12. 6.
 */
public class AssetAddressMainNetTest {
    static String[] coinAddresses = null;
    static String[] assetAddresses = null;

    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AssetAddressMainNetTest.class.getName() + ".setupForClass()");

        ChainEnvironment$.MODULE$.create("mainnet");
        coinAddresses = new String[] {
                "16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM",
                "1Pwes7rbLb4cjQ8z4tSiS13zVaHKqtJ33U",
                "17KGX72xM71xV99FvYWCekabF5aFWx78US",   // Coinprism Colored Address of lightvil
                "18VZZcini4mcopSAp3iM7xcGJsizcZgnu8"    // Coinprism Colored Address of shinyvil
        };
        assetAddresses = new String[] {
                "akB4NBW9UuCmHuepksob6yfZs6naHtRCPNy",
                "akZuY7Hfvp4xJbeJM7WYu5rxZqBTVqYWs2A",
                "akHH9mGrHpaueMPJcyAcPJcVAagkRTUFvZJ",
                "akJTSonY8BYfJg4bXrfpXmpWqeUuAdquP2j"
        };
    }

    @Test
    public void fromCoinAddressBase58Test() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            assert assetAddresses[i].equals(AssetAddress.fromCoinAddress(coinAddresses[i]).base58());
        }
    }

    @Test
    public void stringKeyTest() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetAddress assetAddress = AssetAddress.fromCoinAddress(coinAddress);
            assert assetAddress.base58().equals(assetAddress.stringKey());
            assert assetAddress.base58().equals(assetAddress.toString());
            // If successfully created, the Asset Address should be vaild
            assert assetAddress.isValid();
        }
    }
    @Test
    public void isValidTest() {
        ChainEnvironment env = ChainEnvironment$.MODULE$.get();
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetAddress assetAddress = AssetAddress.fromCoinAddress(coinAddress);
            // If successfully created, the Asset Address should be vaild
            assert assetAddress.isValid();
        }
        // Address    : 16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM
        // PubKeyHash : 010966776006953d5567439e5e39f86a0d273bee
        // AssetId with a wrong PubKeyHash should be invalid.
        AssetAddress assetAddress = new AssetAddress(env.PubkeyAddressVersion(), HexUtil.bytes("010966776006953d5567439e5e39f86a0d273bee00"));
        assert !assetAddress.isValid();
        // AssetId with a wrong version should be invalid.
        assetAddress = new AssetAddress((byte)-1, HexUtil.bytes("010966776006953d5567439e5e39f86a0d273bee"));
        assert !assetAddress.isValid();
    }

    @Test
    public void fromCoinAddressTest() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            assert assetAddresses[i].equals(AssetAddress.fromCoinAddress(coinAddress).base58());
        }
    }

    @Test
    public void toCoinAddressTest() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            assert coinAddresses[i].equals(AssetAddress.fromCoinAddress(coinAddresses[i]).coinAddress().base58());
        }
    }

    @Test
    public void publicKeyHashTest() {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetAddress assetAddress = AssetAddress.fromCoinAddress(coinAddress);
            assert coinAddress.publicKeyHash().equals(new ByteArray(assetAddress.getPublicKeyHash()));
        }
    }

    @Test
    public void lockingScriptTest() {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetAddress assetAddress = AssetAddress.fromCoinAddress(coinAddress);
            assert coinAddress.lockingScript().equals(assetAddress.lockingScript());
        }
    }
    @Test
    public void versionByteTest() {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetAddress assetAddress = AssetAddress.fromCoinAddress(coinAddress);
            assert coinAddress.version() == assetAddress.getVersion();
        }
    }


    @Test(expected = Exception.class)
    public void fromInvalidCoinAddressTest() throws Exception {
        AssetAddress.fromCoinAddress("INVALID_COIN_ADDRESS");
    }

    @Test
    public void coinAddressFromLockingScriptTest() throws OapException {
        for(int i = 0;i < coinAddresses.length;i++) {
            CoinAddress expected = CoinAddress.from(coinAddresses[i]);
            CoinAddress coinAddress = AddressUtil.coinAddressFromLockingScript(expected.lockingScript());
            assert expected.lockingScript().equals(coinAddress.lockingScript());
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void coinAddressFromMarkerOutputLockingScriptTest() throws OapException {
        thrown.expect(OapException.class);
        thrown.expectMessage(startsWith("LockingScript has no Public Key Hash"));
        // MARKER OUTPUT HAS no PublicKeyHash, AddressUtil.coinAddressFromLockingScript() sould return null
        OapMarkerOutput markerOutput = new OapMarkerOutput(null, new int[] { 100 }, new byte[0]);
        CoinAddress coinAddress = AddressUtil.coinAddressFromLockingScript(markerOutput.lockingScript());
    }
}