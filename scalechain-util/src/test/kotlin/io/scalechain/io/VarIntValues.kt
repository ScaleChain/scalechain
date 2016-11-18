package io.scalechain.io

/**
 * Created by kagnmo on 16. 11. 18.
 */
object VarIntValues {
    fun B(value : Int) = value.toByte()

    val EncodedValueOf_0L                   = arrayOf<Byte>(0).toByteArray()
    val EncodedValueOf_1L                   = arrayOf<Byte>(0).toByteArray()
    val EncodedValueOf_0x80L                = arrayOf<Byte>( B(128) ).toByteArray()
    val EncodedValueOf_252L                 = arrayOf<Byte>( B(252) ).toByteArray()
    val EncodedValueOf_253L                 = arrayOf<Byte>( B(253), B(253), 0 ).toByteArray()
    val EncodedValueOf_254L                 = arrayOf<Byte>( B(253), B(254), 0 ).toByteArray()
    val EncodedValueOf_0x1122L              = arrayOf<Byte>( B(253), B(0x22), B(0x11) ).toByteArray()
    val EncodedValueOf_0xFFFFL              = arrayOf<Byte>( B(253), B(0xFF), B(0xFF) ).toByteArray()
    val EncodedValueOf_0x10000L             = arrayOf<Byte>( B(254), 0, 0, 1, 0 ).toByteArray()
    val EncodedValueOf_0x11223344L          = arrayOf<Byte>( B(254), B(0x44), B(0x33), B(0x22), B(0x11)).toByteArray()
    val EncodedValueOf_0xFFFFFFFFL          = arrayOf<Byte>( B(254), B(0xFF), B(0xFF), B(0xFF), B(0xFF)).toByteArray()
    val EncodedValueOf_0x100000000L         = arrayOf<Byte>( B(255), 0, 0, 0, 0, 1, 0, 0, 0 ).toByteArray()
    val EncodedValueOf_0x1122334455667788L  = arrayOf<Byte>( B(255), B(0x88), B(0x77), B(0x66), B(0x55), B(0x44), B(0x33), B(0x22), B(0x11) ).toByteArray()
    val EncodedValueOf_0xFFFFFFFFFFFFFFFFL  = arrayOf<Byte>( B(255), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF) ).toByteArray()

    val ENCODED_VALUE_MAP = mapOf(
        0L to EncodedValueOf_0L,
        1L to EncodedValueOf_1L,
        0x80L to EncodedValueOf_0x80L,
        252L to EncodedValueOf_252L,
        253L to EncodedValueOf_253L,
        254L to EncodedValueOf_254L,
        0x1122L to EncodedValueOf_0x1122L,
        0xFFFFL to EncodedValueOf_0xFFFFL,
        0x10000L to EncodedValueOf_0x10000L,
        0x11223344L to EncodedValueOf_0x11223344L,
        0xFFFFFFFFL to EncodedValueOf_0xFFFFFFFFL,
        0x100000000L to EncodedValueOf_0x100000000L,
        0x1122334455667788L to EncodedValueOf_0x1122334455667788L,
        // 0xFFFFFFFFFFFFFFFFL is -1L
        -1L to EncodedValueOf_0xFFFFFFFFFFFFFFFFL
    )
}
