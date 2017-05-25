package io.scalechain.blockchain.oap.transaction;

import io.scalechain.blockchain.oap.IOapConstants;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.proto.TransactionOutput;
import io.scalechain.blockchain.oap.util.LEB128Codec;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;

import java.nio.ByteBuffer;

/**
 * Created by shannon on 16. 11. 23.
 */
public class OapMarkerOutput extends OapTransactionOutput {
    byte[] metadata;
    int[]  quantities;

    public OapMarkerOutput(TransactionOutput output, AssetId assetId) throws OapException {
        super(assetId, 0, IOapConstants.MARKER_OUTPUT_AMOUNT, output.getLockingScript());
        // Need to check output.value() == 0 ?
        parse(output.getLockingScript().getData().getArray());
    }

    public OapMarkerOutput(AssetId assetId, int[] quantities, byte[] metadata) throws OapException {
        super(assetId, 0, IOapConstants.MARKER_OUTPUT_AMOUNT, lockingScriptFrom(quantities, metadata));
        this.metadata = metadata;
        this.quantities = quantities;
    }

    public int[] getQuantities() {
        return quantities;
    }

    private void parse(byte[] buffer) throws OapException {
        int index = 0;
        // Check OP_RETURN
        if (buffer[index++] != (byte)0x6a) throw new OapException(OapException.COLORING_ERROR, "Invalid data: No OP_RETURN");
        // Check length
        if (buffer[index++] + 2 != (byte)(buffer.length)) throw new OapException(OapException.COLORING_ERROR, "Invalid data: MarkerOutput Length");
        // Check OpenAssetsProtocolImpl Marker
        if (!(buffer[index++] == 0x4f && buffer[index++]  == 0x41)) throw new OapException(OapException.COLORING_ERROR, "Invalid data: OpenAssetsProtocol MARKER");
        // Check OpenAssetsProtocolImpl Version
        if (!(buffer[index++] == 0x01 && buffer[index++]  == 0x00)) throw new OapException(OapException.COLORING_ERROR, "Invalid data: OpenAssetsProtocol VERSION");
        // Asset Quantaty Count : VINT,
        // BUG : Max Length of MarkerOuput is 80 in bytes. Asset Qunatity Count should be less than 80 - 8 bytes.
        int quantityCount = buffer[index++];
        this.quantities = new int[quantityCount];
        // Read Asset Quantities
        for(int i = 0;i < quantityCount;i++) {
            int[] decoded = LEB128Codec.decode(buffer, index);
            quantities[i] = decoded[0];
            index = decoded[1];
        }

        // Get Meatadata length
        int length = buffer[index];
        if (index + length + 1 != buffer.length) throw new OapException(OapException.COLORING_ERROR, "Invalid data: Length does not match");
        metadata = new byte[length];
        System.arraycopy(buffer, index+ 1, metadata, 0, length);
    }

    public static LockingScript lockingScriptFrom(int[] quantities, byte[] metadata) throws OapException {
        byte[] result = null;
        ByteBuffer bufer = ByteBuffer.allocate(80);
        bufer.put((byte)0x6a);
        bufer.put((byte)0);    // DUMMY MARKER OUTPUT LENGTH, WE WILL SET THIS LATER
        bufer.put((byte)0x4f).put((byte)0x41).put((byte)0x01).put((byte)0x00);
        bufer.put((byte)(quantities.length)); // Asset Quantity Count
        for(int quanity : quantities) {
            for(byte b : LEB128Codec.encode(quanity)) {
                bufer.put(b);
            }
        }
        // CHECK metadata length
        if (bufer.position() + 1 + metadata.length > 80) throw new OapException(OapException.COLORING_ERROR, "Invalid metadata length");
        bufer.put((byte)metadata.length);
        for(byte b : metadata) {
            bufer.put(b);
        }
        int length = bufer.position();
        // MARKER OUTPUT LENGTH
        bufer.position(1);
        bufer.put((byte)(length - 2));
        bufer.position(length);
        bufer.flip();
        result = new byte[length];
        bufer.get(result);
        return new LockingScript(new Bytes(result));
    }

    //  /**
    //    * Add an output whose locking script only contains the given bytes prefixed with OP_RETURN.
    //    *
    //    * Used by the block signer to create a transaction that contains the block hash to sign.
    //    *
    //    * @param data
    //    * @return
    //    */
    //   def addOutput(data : Array[Byte]) : TransactionBuilder = {
    //      val lockingScriptOps = List( OpReturn(), OpPush.from(data) )
    //      val lockingScriptData = ScriptSerializer.serialize(lockingScriptOps)
    //      val output = TransactionOutput( 0L, LockingScript(lockingScriptData))
    //      newOutputs.append(output)
    //      this
    //   }
    // Marker OUTPUT : def addOutput(data : Array[Byte]) : TransactionBuilder
    public static byte[] stripOpReturnFromLockScript(LockingScript lockingScript) {
        byte[] scriptBytes = lockingScript.getData().getArray();
        // If script start with OP_RETURN, strip 2 bytes from locking  script
        if (scriptBytes[0] == 0x6a) {
            // STRIP 2 bytes 0x6a + PUSHDATA(n)
            byte[] result = new byte[scriptBytes.length - 2];
            System.arraycopy(scriptBytes, 2, result, 0, result.length);
            return result;
        }
        return scriptBytes;
    }


    public byte[] getMetadata() {
        return metadata;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(OapMarkerOutput.class.getSimpleName()).append('(');
            if (quantities != null) {
                sb.append("quantities=(");
                for (int i = 0; i < quantities.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(quantities[i]);
                }
                sb.append(')');
            }
            if (metadata != null) {
                sb.append(", metadata=(").append(HexUtil.hex(metadata)).append(')');
            }
        return sb.append(')').toString();
    }
}
