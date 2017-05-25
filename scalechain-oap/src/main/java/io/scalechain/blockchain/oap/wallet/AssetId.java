package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.crypto.Base58Check;
import io.scalechain.crypto.HashFunctions;
import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import kotlin.Pair;

/**
 * Created by shannon on 16. 11. 23.
 */
public class AssetId {
    private byte version;
    private byte[] p2pkshHash;

    public AssetId(byte version, byte[] p2pkshHash) {
        this.version = version;
        this.p2pkshHash = p2pkshHash;
    }

    public String base58() {
        return Base58Check.encode(version, p2pkshHash);
    }

    @Override
    public String toString() {return base58();}

    /*
     * AssetId is used as key for Map.
     * We should implement hashCode() and equals().
     *
     */
    @Override
    public int hashCode() {
        return base58().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  AssetId))  return false;
        return base58().equals(((AssetId) o).base58());
    }
    //
    // Comment from Open Assets Protocol.
    // Like addresses, asset IDs can be represented in base 58. They must use version byte 23 (115 in TestNet3) when represented in base 58.
    // The base 58 representation of an asset ID therefore starts with the character 'A' in MainNet.
    public static byte versionByteFromCoinVersion(byte coinAddressVersion) throws OapException {
        // MainNet : 23
        // 0, 5, 128
        // 4 + 136 + X + X
        // TestNet : 115
        // 111, 196, 239
        // 4 + 53 + X + X
        switch(coinAddressVersion) {
            // MAINNET VERSION BYTE
            case (byte)0x00:
            case (byte)0x05:
            case (byte)0x80:
                return IOapConstants.MAINNET_ASSET_ID_VERSION_BYTE;
            // TESTNET VERSION BYTE
            case (byte)0x6f:
            case (byte)0xc4:
            case (byte)0xef:
                return IOapConstants.TESTNET_ASSET_ID_VERSION_BYTE;
        }
        throw new OapException(OapException.INVALID_ADDRESS, "Unupported version byte " + coinAddressVersion);
    }

    public static byte[] p2pkhScriptHash(byte[] publickeyHash) {
        return HashFunctions.hash160(
                ParsedPubKeyScript.from(publickeyHash).lockingScript().getData().getArray()
        ).getValue().getArray();
    }
    //      Bitcoin address       : 16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM.
    //        Base58Check decoded : 010966776006953D5567439E5E39F86A0D273BEE
    //      Pay-to-PubKey-Hash script associated to that address:
    //         OP_DUP OP_HASH160 010966776006953D5567439E5E39F86A0D273BEE OP_EQUALVERIFY OP_CHECKSIG
    //         76     a914       010966776006953d5567439e5e39f86a0d273bee 88             ac
    //      The script is hashed  : 36e0ea8e93eaa0285d641305f4c81e563aa570a2
    //       Base 58 string with checksum using version byte 23: ALn3aK1fSuG27N96UGYB1kUYUpGKRhBuBC.

    // mxLUhPY9Sjh6Kpo47vgMPGB7HJeL54NpVE
    //   bX8JMwZMUvDanC4xRAYnY385gcupW62Cepm
    //   APAETT6HGrBp59QHEEeRupsfcS7f2HjApo : REST API 에서 만들어진 AssetId는 이렇게 나오는데 이것은 namespace를 모두 0x17로 한 것. 잘 못된 값이다.
    //   oQFj3SWnbSpRK2EFUqHkXMv3WqpSV4NjaW : namesapce byte를 115로 주었을 때 나오는 값이다.

    public static AssetId from(String base58) throws OapException {
        try {
            CoinAddress.from(base58);
        } catch(Exception e) {
            try {
                Pair<Byte, byte[]> decoded = Base58Check.decode(base58);
                return new AssetId( decoded.getFirst(), decoded.getSecond());
            } catch(Exception ex) {
                throw new OapException(OapException.INVALID_ASSET_ID, "Invalid AssetId: " + base58, ex);
            }
        }
        throw new OapException(OapException.INVALID_ASSET_ID, "Invalid AssetId: " + base58);
    }

    public static AssetId from(CoinAddress coinAddress) throws OapException {
        return new AssetId(
                versionByteFromCoinVersion(coinAddress.getVersion()),
                p2pkhScriptHash(coinAddress.getPublicKeyHash().getArray())
        );
    }

    public static AssetId fromCoinAddress(String coinAddress) throws OapException {
        try {
            Pair<Byte, byte[]> decoded = Base58Check.decode(coinAddress);
            return new AssetId(
                    versionByteFromCoinVersion(decoded.getFirst()),
                    p2pkhScriptHash(decoded.getSecond())
            );
        } catch(Exception e) {
            throw new OapException(OapException.INVALID_ADDRESS, "Cannot create AssetId from CoinAddress: " + coinAddress);
        }
    }
}
