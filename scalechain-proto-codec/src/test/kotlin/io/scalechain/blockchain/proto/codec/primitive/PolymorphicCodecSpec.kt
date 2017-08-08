package io.scalechain.blockchain.proto.codec.primitive

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Row2
import io.kotlintest.properties.Table2
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.blockchain.proto.codec.MultiplePayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

interface Animal

data class Dog(val isWild: Boolean) : Animal
data class Human(val iq : Int) : Animal

internal object DogCodec : Codec<Dog> {
  override fun transcode(io: CodecInputOutputStream, obj: Dog?): Dog? {
    val isWild = BooleanCodec().transcode(io, obj?.isWild)
    if (io.isInput) {
      return Dog(isWild!!)
    } else {
      return null
    }
  }
}

internal object HumanCodec : Codec<Human> {
  override fun transcode(io: CodecInputOutputStream, obj: Human?): Human? {
    val iq = Int32Codec().transcode(io, obj?.iq)
    if (io.isInput) {
      return Human(iq!!)
    } else {
      return null
    }
  }
}


@RunWith(KTestJUnitRunner::class)
class PolymorphicCodecSpec : MultiplePayloadTestSuite<Animal>()  {

  override val codec = Codecs.polymorphicCodec<Long, Animal>(
    typeIndicatorCodec = VariableIntCodec(),
    typeClassNameToTypeIndicatorMap = mapOf(
      "Dog" to 1L,
      "Human" to 2L
    ),
    typeIndicatorToCodecMap = mapOf(
      1L to (DogCodec as Codec<Animal>),
      2L to (HumanCodec as Codec<Animal>)
    )
  )

  override val payloads =
    Table2<Animal, ByteArray>(
      headers("message", "payload"),
      listOf(
        Row2<Animal, ByteArray>( Dog(false), bytes("01  00") ),
        Row2<Animal, ByteArray>( Dog(true),  bytes("01  01") ),
        Row2<Animal, ByteArray>( Human(0),   bytes("02  00 00 00 00") ),
        Row2<Animal, ByteArray>( Human(256), bytes("02  00 00 01 00") )
      )
    )
}
