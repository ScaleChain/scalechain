package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;

/**
 * Created by shannon on 16. 12. 8.
 */
public class AssetIdMainNetTest {
    static String[] coinAddresses = null;
    static String[] assetIds = null;

    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AssetIdMainNetTest.class.getName() + ".setupForClass()");
        ChainEnvironment$.MODULE$.create("mainnet");
        coinAddresses = new String[] {
                "16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM",
                "1Pwes7rbLb4cjQ8z4tSiS13zVaHKqtJ33U",
                "17KGX72xM71xV99FvYWCekabF5aFWx78US",   // Coinprism Colored Address of lightvil
                "18VZZcini4mcopSAp3iM7xcGJsizcZgnu8"    // Coinprism Colored Address of shinyvil
        };

        assetIds = new String[] {
                "ALn3aK1fSuG27N96UGYB1kUYUpGKRhBuBC",
                "AHnGH2NJQmoV97tix2dtHUAk3ut1Sn4U9p",
                "AahW4fXGAc3h9bhMBajpCibVuYbxzPn3jH",
                "AYiGHmAU91NV6TmBzHFqsGA3QBotUENvyK"
        };
    }

    @Test
    public void versionByteTest() throws OapException {
        byte[] mainNet = { (byte)0x00, (byte)0x05, (byte)0x80 };
        byte[] testNet = { (byte)0x6f, (byte)0xc4, (byte)0xef };
        for(byte version : mainNet) {
            assert AssetId.versionByteFromCoinVersion(version) == IOapConstants.MAINNET_ASSET_ID_VERSION_BYTE;
        }
        for(byte version : testNet) {
            assert AssetId.versionByteFromCoinVersion(version) == IOapConstants.TESTNET_ASSET_ID_VERSION_BYTE;
        }
    }

    @Test
    public void invalidVersionByteTest() {
        HashSet<Byte> supportedVersionByteSet = new HashSet<Byte>();
        byte[] supportedVersionBytes = { (byte)0x00, (byte)0x05, (byte)0x80, (byte)0x6f, (byte)0xc4, (byte)0xef };
        for(byte b : supportedVersionBytes) {
            supportedVersionByteSet.add(b);
        }
        for(int i = Byte.MIN_VALUE;i < Byte.MAX_VALUE;i++) {
            if (!supportedVersionByteSet.contains((byte)i)) {
                try {
                    AssetId.versionByteFromCoinVersion((byte) i);
                    System.out.println(i);
                    assert false;
                }catch(OapException e) {
                }
            }
        }
    }


    @Test
    public void p2pkhScriptHashTest() {
        String coinAddress = "16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM";
        String p2shScriptHash = "36e0ea8e93eaa0285d641305f4c81e563aa570a2";

        String expectedHash = HexUtil.hex(
                AssetId.p2pkhScriptHash(
                        CoinAddress.from(coinAddress).publicKeyHash().array()
                ),
                HexUtil.hex$default$2()
        );
        assert p2shScriptHash.equals(expectedHash);
    }

    @Test
    public void assetIdTest() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            AssetId assetId = new AssetId(
                    AssetId.versionByteFromCoinVersion(coinAddress.version()),
                    AssetId.p2pkhScriptHash(coinAddress.publicKeyHash().array())
            );
            assert !assetId.equals(coinAddress);
            assert assetId.base58().equals(assetIds[i]);
            assert assetId.base58().equals(assetId.toString());

            AssetId assetId2 = new AssetId(
                    AssetId.versionByteFromCoinVersion(coinAddress.version()),
                    AssetId.p2pkhScriptHash(coinAddress.publicKeyHash().array())
            );

            assert assetId.base58().equals(assetId.base58());
            assert assetId.hashCode() == assetId2.hashCode();
            assert assetId.equals(assetId2);

            CoinAddress coinAddress2 = CoinAddress.from(coinAddresses[coinAddresses.length - i -1]);
            AssetId assetId3 = new AssetId(
                    AssetId.versionByteFromCoinVersion(coinAddress2.version()),
                    AssetId.p2pkhScriptHash(coinAddress2.publicKeyHash().array())
            );
            assert !assetId.equals(assetId3);
        }
    }

    @Test
    public void fromBase58Test() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            assert AssetId.from(assetIds[i]).equals(AssetId.fromCoinAddress(coinAddresses[i]));
        }
    }
    @Test(expected = OapException.class)
    public void fromInvalidBase58Test() throws OapException {
        AssetId.from("INVALID_ASSET_ID");
    }

    @Test
    public void fromCoinAddressTest() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            CoinAddress coinAddress = CoinAddress.from(coinAddresses[i]);
            assert assetIds[i].equals(AssetId.from(coinAddress).base58());
        }
    }

    @Test
    public void fromCoinAddressBase58Test() throws Exception {
        for (int i = 0; i < coinAddresses.length; i++) {
            assert assetIds[i].equals(AssetId.fromCoinAddress(coinAddresses[i]).base58());
        }
    }

    @Test(expected = OapException.class)
    public void fromInvalidCoinAddressTest() throws OapException {
        AssetId.fromCoinAddress("INVALID_COIN_ADDRESS");
    }
}
