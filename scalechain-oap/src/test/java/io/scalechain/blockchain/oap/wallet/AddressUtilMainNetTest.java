package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by shannon on 16. 12. 15.
 */
public class AddressUtilMainNetTest {

    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AddressUtilMainNetTest.class.getName() + ".setupForClass()");

        ChainEnvironment$.MODULE$.create("mainnet");
    }

    // String txIdHex = "9c08a4d78931342b37fd5f72900fb9983087e6f46c4a097d8a1f52c74e28eaf6";
    // String txHex = "010000000168781ca236d8e70e4af8285852defabeff61c73db259cbbbebdf7bdac918c234010000004847304402206d186984373e0b781b85f49334cc9a249ffd13a448a5d1732096b011a10063e102206d280df8f4b5e6805f48eb04601ca6d97edd78f2b2d1104dc577e08fd788c78001ffffffff02cce80900000000001976a914fe58bbf690824bdaffb0431a709c27d7bdb6105e88ac801a06000000000017a91419a7d869032368fd1f1e26e5e73a4ad0e474960e8700000000";
    private String[] _p2shLockingScripts  = {
            "a91419a7d869032368fd1f1e26e5e73a4ad0e474960e87"
    };
    private String[] _p2shAddresses = {
            "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey"
    };

    String[] getP2shLockingScripts() {
        return _p2shLockingScripts;
    }
    String[] getP2shAddresses() {
        return _p2shAddresses;
    }

    private String[] _p2pkhLockingScripts = {
            "76a914fe58bbf690824bdaffb0431a709c27d7bdb6105e88ac"
    };
    private String[] _p2pkhAddresses = {
            "1QBrw64xj8p9RNLsjhzQtuz2NBdMqRuKzJ"
    };

    String[] getP2pkhLockingScripts() {
        return _p2pkhLockingScripts;
    }
    String[] getP2pkhAddresses() {
        return _p2pkhAddresses;
    }


    @Test
    public void coinAddressFromLockingScriptP2SHTest() throws OapException {
        String[] p2shLockingScripts = getP2shLockingScripts();
        String[] p2shAddresses = getP2shAddresses();

        for(int i = 0;i < p2shLockingScripts.length;i++ ) {
            CoinAddress p2shAddress = AddressUtil.coinAddressFromLockingScript(
                    new LockingScript(new ByteArray(HexUtil.bytes(p2shLockingScripts[i])))
            );
            assertEquals("P2SH Script", p2shAddresses[i], p2shAddress.base58());
        }
    }

    @Test
    public void coinAddressFromLockingScriptP2PKHTest() throws OapException{
        String[] p2pkhLockingScripts = getP2pkhLockingScripts();
        String[] p2pkhAddresses = getP2pkhAddresses();

        for(int i = 0;i < p2pkhLockingScripts.length;i++ ) {
            CoinAddress p2pkhAddress = AddressUtil.coinAddressFromLockingScript(
                    new LockingScript(new ByteArray(HexUtil.bytes(p2pkhLockingScripts[i])))
            );
            assertEquals("P2SH Script", p2pkhAddresses[i], p2pkhAddress.base58());
        }
    }

    @Test
    public void coinAddressFromLockingScriptMarkerOutputTest() {
//        String markerOutputLockingScript = "";
//
//
//        CoinAddress p2pkhAddress = AddressUtil.coinAddressFromLockingScript(
//                new LockingScript(new ByteArray(HexUtil.bytes(p2pkhLockingScripts[i])))
//        );
    }

}
