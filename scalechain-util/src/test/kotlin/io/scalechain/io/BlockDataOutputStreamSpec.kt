package io.scalechain.util
/*
import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.io.BlockDataInputStream
import io.scalechain.io.BlockDataOutputStream
import io.scalechain.io.VarIntValues
import io.scalechain.io.VarIntValues.B
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class BlockDataOutputStreamSpec : FlatSpec(), Matchers {

    var bout    : ByteArrayOutputStream? = null
    var ostream : BlockDataOutputStream? = null
    override fun beforeEach() {
        // set-up code
        //
        bout = ByteArrayOutputStream()
        ostream = BlockDataOutputStream(bout!!)
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
        ostream?.close()
        ostream = null
        bout = null
    }

    fun testWriteLittleEndianInt(value : Int, encoded : Array<Byte>) : Boolean {
        ostream?.writeLittleEndianInt(value)
        ostream?.flush()

        return Arrays.equals(encoded.toByteArray(), bout?.toByteArray())
    }

    fun testWriteLittleEndianLong(value : Long, encoded : Array<Byte>) : Boolean {
        ostream?.writeLittleEndianLong(value)
        ostream?.flush()

        return Arrays.equals(encoded.toByteArray(), bout?.toByteArray())
    }

    init {
        "writeLittleEndianInt" should "write 0" {
            testWriteLittleEndianInt(0, arrayOf<Byte>(0, 0, 0, 0)) shouldBe true
        }
        "writeLittleEndianInt" should "write -1" {
            testWriteLittleEndianInt(-1, arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF))) shouldBe true
        }
        "writeLittleEndianInt" should "write 0x7FFFFFFF" {
            testWriteLittleEndianInt(0x7FFFFFFF, arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0x7F))) shouldBe true
        }

        "writeLittleEndianInt" should "write 0x11223344" {
            testWriteLittleEndianInt(0x11223344, arrayOf<Byte>(B(0x44), B(0x33), B(0x22), B(0x11))) shouldBe true
        }

        "writeLittleEndianLong" should "write 0L" {
            testWriteLittleEndianLong(0L, arrayOf<Byte>(0, 0, 0, 0, 0, 0, 0, 0)) shouldBe true
        }
        "writeLittleEndianLong" should "write -1L" {
            testWriteLittleEndianLong(-1L, arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF))) shouldBe true
        }
        "writeLittleEndianLong" should "write 0x1122334455667788L" {
            testWriteLittleEndianLong(0x1122334455667788L, arrayOf<Byte>(B(0x88), B(0x77), B(0x66), B(0x55), B(0x44), B(0x33), B(0x22), B(0x11))) shouldBe true
        }
        "writeLittleEndianLong" should "write 0x7FFFFFFFFFFFFFFFL" {
            testWriteLittleEndianLong(0x7FFFFFFFFFFFFFFFL, arrayOf<Byte>(B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0xFF), B(0x7F))) shouldBe true
        }

        "writeVarInt" should "write encoded values" {
            for ( (decoded, encoded) in VarIntValues.ENCODED_VALUE_MAP) {
                println("testing readVarInt : $decoded")
                val bout = ByteArrayOutputStream()
                val ostream = BlockDataOutputStream(bout)
                ostream.writeVarInt(decoded)
                ostream.flush()

                print("encoded:"); println()
                for (b in encoded) print("${b.toInt()},")
                println()
                print("bout:"); println()
                for (b in bout.toByteArray()) print("${b.toInt()},")

                java.util.Arrays.equals( encoded, bout.toByteArray() ) shouldBe true
            }
        }

        "writeBytes" should "write bytes" {
            val inputBytes = arrayOf<Byte>(0,1).toByteArray()
            ostream?.writeBytes(inputBytes)
            ostream?.flush()
            Arrays.equals( inputBytes, bout?.toByteArray() ) shouldBe true
        }

    }
}
*/