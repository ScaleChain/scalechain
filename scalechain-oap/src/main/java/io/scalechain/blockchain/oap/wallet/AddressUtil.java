package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.script.ops.OpHash160;
import io.scalechain.blockchain.script.ops.OpPush;
import io.scalechain.blockchain.script.ops.ScriptOp;
import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.util.Bytes;

import java.util.List;

/**
 * Created by shannon on 16. 11. 14.
 */
public class AddressUtil {
  public static CoinAddress coinAddressFromLockingScript(LockingScript script) throws OapException {
    byte version;
    List<ScriptOp> operations = ParsedPubKeyScript.from(script).getScriptOps().getOperations();
    // FIXME: we should add version byte for TestNet
    // P2SH  : OP_HASH160 <scriptHash> OP_EQUAL
    // P2PKH : OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
    if (operations.get(0) instanceof OpHash160) { // P2SH
      // MainNet 5, TestNet 0x6f
      version = ChainEnvironment.get().getScriptAddressVersion();
    } else {    // P2PKSH
      // MainNet 0, TestNet 0xc4
      version = ChainEnvironment .get().getPubkeyAddressVersion();
    }
    // In the P2PKH, P2SH, there is only on HASH160 Public Key Hash.
    for (int i = 1; i < operations.size(); i++) {
      ScriptOp op = operations.get(i);
      if (op instanceof OpPush) {
        OpPush push = (OpPush) op;
        if (push.getByteCount() == 20) {
          return new CoinAddress(version, new Bytes(push.getInputValue().getValue()));
        }
      }
    }
    throw new OapException(OapException.INVALID_ADDRESS, "LockingScript has no Public Key Hash.");
  }
}
