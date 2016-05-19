package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{Hash, WalletTransaction}
import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.script.ScriptParser

class OutputOwnershipCodecsSpec extends CodecSuite with TransactionTestDataTrait with ProtoTestData {

  if (ChainEnvironmentFactory.getActive().isEmpty)
    ChainEnvironmentFactory.create("testnet")

  "OutputOwnershipCodecsSpec" should {

    "roundtrip" in {
      roundtrip(OutputOwnershipCodec.codec, ADDR1.address)
    }
  }

  "publicKeyScript" should {
    "roundtrip" in {
      val expectedScriptOps = ScriptParser.parse( ADDR1.pubKeyScript.lockingScript() )

      val serialized = ParsedPubKeyScriptCodec.serialize(ADDR1.pubKeyScript)

      val actualScriptOps = ParsedPubKeyScriptCodec.parse(serialized)

      actualScriptOps.scriptOps shouldBe expectedScriptOps
    }
  }
}

