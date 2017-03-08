package io.scalechain.blockchain.cli.wallet
import io.kotlintest.properties.Gen

object AccountNameSample {
  fun default() = ""
  fun random() = Gen.string().nextPrintableString(20)
}

