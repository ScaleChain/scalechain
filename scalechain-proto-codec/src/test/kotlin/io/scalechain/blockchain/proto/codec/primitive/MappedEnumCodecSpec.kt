package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

enum class Seasons {
  Spring, Summer, Autumn, Winter
}

@RunWith(KTestJUnitRunner::class)
class MappedEnumCodecSpec : MultiplePayloadTestSuite<Seasons>()  {

  override val codec = Codecs.mappedEnum(Int32Codec(), mapOf(
    Seasons.Spring to 1,
    Seasons.Summer to 2,
    Seasons.Autumn to 3,
    Seasons.Winter to 4
  ))

  override val payloads =
    table(
      headers("message", "payload"),
      row( Seasons.Spring, bytes("00 00 00 01")),
      row( Seasons.Summer, bytes("00 00 00 02")),
      row( Seasons.Autumn, bytes("00 00 00 03")),
      row( Seasons.Winter, bytes("00 00 00 04"))
    )
}
