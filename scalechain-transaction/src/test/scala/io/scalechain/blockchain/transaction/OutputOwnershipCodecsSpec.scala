package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{Hash, WalletTransaction}
import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class OutputOwnershipCodecsSpec extends CodecSuite with TransactionTestDataTrait with ProtoTestData {

  if (ChainEnvironmentFactory.getActive().isEmpty)
    ChainEnvironmentFactory.create("testnet")

  "OutputOwnershipCodecsSpec" should {

    "roundtrip" in {
      roundtrip(OutputOwnershipCodec.codec, ADDR1.address)
      roundtrip(OutputOwnershipCodec.codec, ADDR1.pubKeyScript)
    }
  }
}

