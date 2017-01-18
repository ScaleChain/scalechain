package io.scalechain.blockchain.oap.wallet;

import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.Hash;
import io.scalechain.blockchain.transaction.ChainEnvironment$;
import io.scalechain.util.ByteArray;
import io.scalechain.util.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Option;
import scala.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by shannon on 17. 1. 8.
 */
public class CaseClassTest {
  @BeforeClass
  public static void setUpForClass() throws Exception {
    System.out.println(AssetAddressTestNetTest.class.getName() + ".setupForClass()");
    ChainEnvironment$.MODULE$.create("testnet");
  }

  @Test
  public void unspentAssetDescrptorTest() throws OapException {
    String assetIdString = "ALn3aK1fSuG27N96UGYB1kUYUpGKRhBuBC";
    UnspentAssetDescriptor desc = new UnspentAssetDescriptor(
      Hash.apply(ByteArray.apply(HexUtil.bytes("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"))),
      0, // vout
      Option.apply("16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM"),
      Option.apply("account"),
      "srciptPubKey",
      Option.apply("redeepScript"),
      BigDecimal.decimal(5),
      1, // confirmations
      true,
      AssetId.from(assetIdString),
      1000 // quantity
    );

    assertEquals("AssetId should be equal to", assetIdString, desc.assetId.base58());
    String s = desc.toString();
    assertTrue("Descriptor toString() contains Asset Id String", s.contains(assetIdString));
  }

  @Test
  public void oapExceptionTest() {
    int errorCode = OapException.INTERNAL_ERROR;
    String message = "Internal Error";
    OapException e = new OapException(errorCode, message);
    assertEquals("getMessage()", message, e.getMessage());
    assertEquals("getErrorCode()", errorCode, e.getErrorCode());
  }
}
