package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.io.BlockDataInputStream
import io.scalechain.io.VarIntValues.ENCODED_VALUE_MAP
import io.scalechain.io.VarIntValues.B
import java.io.ByteArrayInputStream
import java.util.Arrays

class BlockDataInputStreamSpec : FlatSpec(), Matchers {

    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
    }

    fun istream( encoded : Array<Byte> ) = BlockDataInputStream(ByteArrayInputStream(encoded.toByteArray()))

    init {
        "readLittleEndianInt" should "read 0" {
            istream( arrayOf<Byte>(0, 0, 0, 0) ).readLittleEndianInt() shouldBe 0
        }
        "readLittleEndianInt" should "read -1" {
            istream( arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF) ) ).readLittleEndianInt() shouldBe -1
        }
        "readLittleEndianInt" should "read 0x7FFFFFFF" {
            istream( arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0x7F) ) ).readLittleEndianInt() shouldBe 0x7FFFFFFF
        }
        "readLittleEndianInt" should "read 0x11223344" {
            istream( arrayOf<Byte>(B(0x44), B(0x33), B(0x22), B(0x11) ) ).readLittleEndianInt() shouldBe 0x11223344
        }

        "readLittleEndianLong" should "read 0L" {
            istream( arrayOf<Byte>(0, 0, 0, 0, 0, 0, 0, 0) ).readLittleEndianLong() shouldBe 0L
        }
        "readLittleEndianLong" should "read -1L" {
            istream( arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF) ) ).readLittleEndianLong() shouldBe -1L
        }
        "readLittleEndianLong" should "read 0x1122334455667788L" {
            istream( arrayOf<Byte>(B(0x88), B(0x77), B(0x66), B(0x55), B(0x44), B(0x33), B(0x22), B(0x11) ) ).readLittleEndianLong() shouldBe 0x1122334455667788L
        }
        "readLittleEndianLong" should "read 0x7FFFFFFFFFFFFFFFL" {
            istream( arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0x7F) ) ).readLittleEndianInt() shouldBe 0x7FFFFFFFFFFFFFFFL
        }


        "readVarInt" should "read encoded values" {
            for ( (decoded, encoded) in ENCODED_VALUE_MAP ) {
                println("testing readVarInt : $decoded")
                val istream = BlockDataInputStream(ByteArrayInputStream(encoded))
                istream.readVarInt() shouldBe decoded
            }
        }

        "readBytes(size)" should "read bytes" {
            Arrays.equals( istream( arrayOf<Byte>(0, 1) ).readBytes(1), arrayOf<Byte>(0).toByteArray()) shouldBe true
            Arrays.equals( istream( arrayOf<Byte>(0, 1) ).readBytes(2), arrayOf<Byte>(0,1).toByteArray()) shouldBe true
        }

        "readBytes(bytes)" should "read bytes" {
            val arraySize1 = ByteArray(1)
            istream( arrayOf<Byte>(0, 1) ).readBytes(arraySize1)
            Arrays.equals( arraySize1, arrayOf<Byte>(0).toByteArray()) shouldBe true

            val arraySize2 = ByteArray(2)
            istream( arrayOf<Byte>(0, 1) ).readBytes(arraySize2)
            Arrays.equals( arraySize2, arrayOf<Byte>(0,1).toByteArray()) shouldBe true
        }
    }
}