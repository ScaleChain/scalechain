package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.LockingScript;
import io.scalechain.blockchain.script.ops.OpHash160;
import io.scalechain.blockchain.script.ops.OpPush;
import io.scalechain.blockchain.script.ops.ScriptOp;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.util.ByteArray;
import scala.collection.immutable.List;

/**
 * Created by shannon on 16. 11. 14.
 */
public class AddressUtil {
  public static CoinAddress coinAddressFromLockingScript(LockingScript script) throws OapException {
    byte version;
    List<ScriptOp> operations = ParsedPubKeyScript.from(script).scriptOps().operations();
    // FIXME: we should add version byte for TestNet
    // P2SH  : OP_HASH160 <scriptHash> OP_EQUAL
    // P2PKH : OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
    if (operations.apply(0) instanceof OpHash160) { // P2SH
      // MainNet 5, TestNet 0x6f
      version = ChainEnvironment$.MODULE$.get().ScriptAddressVersion();
    } else {    // P2PKSH
      // MainNet 0, TestNet 0xc4
      version = ChainEnvironment$.MODULE$.get().PubkeyAddressVersion();
    }
    // In the P2PKH, P2SH, there is only on HASH160 Public Key Hash.
    for (int i = 1; i < operations.size(); i++) {
      ScriptOp op = operations.apply(i);
      if (op instanceof OpPush) {
        OpPush push = (OpPush) op;
        if (push.byteCount() == 20) {
          return new CoinAddress(version, new ByteArray(push.inputValue().value()));
        }
      }
    }
    throw new OapException(OapException.INVALID_ADDRESS, "LockingScript has no Public Key Hash.");
  }
}
