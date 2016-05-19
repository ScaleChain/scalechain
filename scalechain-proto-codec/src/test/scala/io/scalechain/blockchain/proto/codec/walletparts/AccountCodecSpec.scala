package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.{Account, Hash, OutPoint}
import io.scalechain.blockchain.proto.codec.{AccountCodec, OutPointCodec}
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

/**
  * Created by kangmo on 5/19/16.
  */
class AccountCodecSpec extends CodecSuite with ProtoTestData {

  implicit val accountCodec = AccountCodec.codec

  val account1 = Account(
    name = "kangmo"
  )

  "AccountCodec" should {

    "roundtrip" in {
      roundtrip(account1)
    }
  }
}

