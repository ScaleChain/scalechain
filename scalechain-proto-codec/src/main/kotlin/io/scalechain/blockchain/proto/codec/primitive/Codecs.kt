package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import java.nio.charset.Charset

/**
 * Codec factory for creating different kinds of codec.
 */
object Codecs {

    val Boolean = BooleanCodec()
    val Byte = ByteCodec()

    val Int16 = Int16Codec()
    val Int16L = Int16LCodec()
    val UInt16 = UInt16Codec()
    val UInt16L = UInt16LCodec()

    val Int32 = Int32Codec()
    val Int32L = Int32LCodec()
    val UInt32 = UInt32Codec()
    val UInt32L = UInt32LCodec()

    val Int64 = Int64Codec()
    val Int64L = Int64LCodec()
    val UInt64L = UInt64LCodec()

    val VariableInt = VariableIntCodec()

    val CString = CStringCodec(Charset.forName("UTF-8"))
    val CByteArray = CByteArrayCodec()
    fun<T> cstringPrefixed(valueCodec : Codec<T>) = CStringPrefixedCodec<T>(valueCodec)

    fun fixedByteBuf(length : Int )
        = FixedByteBufCodec(length)

    fun fixedByteArray(length : Int )
        = FixedByteArrayCodec(length)

    fun variableByteBuf( lengthCodec : Codec<Long> )
        = VariableByteBufCodec(lengthCodec)

    val VariableByteBuf = variableByteBuf( VariableInt )

    fun variableString( lengthCodec : Codec<Long> )
        = VariableStringCodec(lengthCodec)

    val VariableString = variableString( VariableInt )

    fun<T> variableList(lengthCodec : Codec<Long>, valueCodec : Codec<T>)
        = VariableListCodec<T>(lengthCodec, valueCodec)

    fun<T> variableList(valueCodec : Codec<T>)
        = variableList(VariableInt, valueCodec)

    fun<T> optional( flagCodec : Codec<Boolean>, valueCodec : Codec<T>)
        = OptionalCodec<T>(flagCodec, valueCodec)

    fun<T> optional( valueCodec : Codec<T> )
        = optional(Boolean, valueCodec)


    fun <valueT, enumT> mappedEnum(valueCodec: Codec<valueT>, enumMap: Map<enumT, valueT>)
        = MappedEnumCodec(valueCodec, enumMap)

    fun<T> provide( objectSample : T)
        = ProvideCodec<T>(objectSample)
}
