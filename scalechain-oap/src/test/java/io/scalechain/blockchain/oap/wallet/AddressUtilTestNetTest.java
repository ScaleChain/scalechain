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
public class AddressUtilTestNetTest {
    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println(AddressUtilTestNetTest.class.getName() + ".setupForClass()");

        ChainEnvironment$.MODULE$.create("mainnet");
    }

    @Test
    public void coinAddressFromLockingScriptP2SHTest() throws OapException {
        // String txIdHex = "9c08a4d78931342b37fd5f72900fb9983087e6f46c4a097d8a1f52c74e28eaf6";
        // String txHex = "010000000168781ca236d8e70e4af8285852defabeff61c73db259cbbbebdf7bdac918c234010000004847304402206d186984373e0b781b85f49334cc9a249ffd13a448a5d1732096b011a10063e102206d280df8f4b5e6805f48eb04601ca6d97edd78f2b2d1104dc577e08fd788c78001ffffffff02cce80900000000001976a914fe58bbf690824bdaffb0431a709c27d7bdb6105e88ac801a06000000000017a91419a7d869032368fd1f1e26e5e73a4ad0e474960e8700000000";
        String lockingScriptHex  = "a91419a7d869032368fd1f1e26e5e73a4ad0e474960e87";
        String p2shAddressBase58 = "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey";

        LockingScript lockingScript = new LockingScript(new ByteArray(HexUtil.bytes(lockingScriptHex)));
        CoinAddress p2shAddress = AddressUtil.coinAddressFromLockingScript(lockingScript);
        assertEquals("P2SH Script", p2shAddressBase58, p2shAddress.base58());

    }

    @Test
    public void coinAddressFromLockingScriptP2PKHTest() throws OapException {
        // String txIdHex = "9c08a4d78931342b37fd5f72900fb9983087e6f46c4a097d8a1f52c74e28eaf6";
        // String txHex = "010000000168781ca236d8e70e4af8285852defabeff61c73db259cbbbebdf7bdac918c234010000004847304402206d186984373e0b781b85f49334cc9a249ffd13a448a5d1732096b011a10063e102206d280df8f4b5e6805f48eb04601ca6d97edd78f2b2d1104dc577e08fd788c78001ffffffff02cce80900000000001976a914fe58bbf690824bdaffb0431a709c27d7bdb6105e88ac801a06000000000017a91419a7d869032368fd1f1e26e5e73a4ad0e474960e8700000000";
        String lockingScriptHex  = "76a914fe58bbf690824bdaffb0431a709c27d7bdb6105e88ac";
        String p2shAddressBase58 = "1QBrw64xj8p9RNLsjhzQtuz2NBdMqRuKzJ";

        LockingScript lockingScript = new LockingScript(new ByteArray(HexUtil.bytes(lockingScriptHex)));
        CoinAddress p2shAddress = AddressUtil.coinAddressFromLockingScript(lockingScript);
        assertEquals("P2SH Script", p2shAddressBase58, p2shAddress.base58());
    }


}
