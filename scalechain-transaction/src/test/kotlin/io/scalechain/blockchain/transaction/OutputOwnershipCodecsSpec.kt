package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.WalletTransaction
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.transaction.TransactionTestData.ADDR1
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OutputOwnershipCodecsSpec : FlatSpec(), Matchers, CodecTestUtil, TransactionTestInterface {

  init {
    "OutputOwnershipCodecsSpec" should {
      "roundtrip" {
        roundTrip(OutputOwnershipCodec, ADDR1.address)
      }
    }

    "publicKeyScript" should {
      "roundtrip" {
        val expectedScriptOps = ScriptParser.parse( ADDR1.pubKeyScript.lockingScript() )

        val serialized = ParsedPubKeyScriptCodec.encode(ADDR1.pubKeyScript)

        val actualScriptOps = ParsedPubKeyScriptCodec.decode(serialized)!!

        actualScriptOps.scriptOps shouldBe expectedScriptOps
      }
    }
  }
}

