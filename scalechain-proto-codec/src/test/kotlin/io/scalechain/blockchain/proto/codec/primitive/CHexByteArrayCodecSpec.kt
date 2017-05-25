package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.HexUtil.hex
import org.junit.runner.RunWith

import java.nio.charset.Charset

@RunWith(KTestJUnitRunner::class)
class CHexByteArrayCodecSpec : MultiplePayloadTestSuite<ByteArray>()  {

  override val codec : Codec<ByteArray> = Codecs.CHexByteArray

  override val payloads =
    table(
      headers("message", "payload"),
      row( bytes(""), bytes( "00" )),
      // Ascii of 0 is 30 in hex.
      row( bytes("00"), bytes( "30 30  00")),
      row( bytes("00 01"), bytes("30 30  30 31  00"))
    )
}
