package io.scalechain.blockchain.oap;


import java.math.BigDecimal;

/**
 * Created by shannon on 16. 11. 17.
 */
public interface IOapConstants {
    public static final int MARKER_OUTPUT_AMOUNT          = 0;

    // DEFAULT FEES AND MIN FEES comes from api_server
    public static final int DEFAULT_FEES_IN_SATOSHI        = 20000;
    public static final int MIN_FEES_IN_SATOSHI            =  1000;
    public static final int DUST_IN_SATOSHI                =   600;

    public static final BigDecimal DUST_IN_BITCOIN         = new BigDecimal("0.000006");
    public static final BigDecimal DEFAULT_FEES_IN_BITCOIN = new BigDecimal("0.0002");
    public static final BigDecimal ONE_BTC_IN_SATOSHI      = new BigDecimal("100000000");

    public static final long DEFAULT_MIN_CONFIRMATIONS     = 0;
    // BUGBUG Can this code make an issue when there is an old transaction holding the output to spend? (Below the block depth 999999)
    public static final long DEFAULT_MAX_CONFIRMATIONS     = 999999;

    public static final byte NAMESPACE_BYTE                = (byte)0x13;

    public static final byte TESTNET_ASSET_ID_VERSION_BYTE = (byte)0x73;
    public static final byte MAINNET_ASSET_ID_VERSION_BYTE = (byte)0x17;

    public static final int MODE_SCALECHAIN    = 0;
    public static final int MODE_BITCOIND      = 1;
    public static final int MODE_MOCKUP_TEST   = 2;
}
