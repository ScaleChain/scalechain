package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.WalletOutputCodec
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.util.HexUtil

class OutputDescriptorCodecSpec extends CodecSuite with ProtoTestData {

  implicit val outputDescriptorCodec = WalletOutputCodec.codec

  val outputDescriptor1 =
    WalletOutput(
      spent         = false,
      outPoint      = OutPoint(Hash("d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a"), 1),
      address       = Some("mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"),
      account       = Some("test label"),
      lockingScript = LockingScript( HexUtil.bytes("76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac") ),
      redeemScript  = None,
      amount        = scala.math.BigDecimal(0.0001),
      confirmations = 6210,
      involvesWatchonly     = true
    )

  "WalletTransactionCodec" should {

    "roundtrip" in {
      roundtrip(outputDescriptor1)
    }
  }
}



