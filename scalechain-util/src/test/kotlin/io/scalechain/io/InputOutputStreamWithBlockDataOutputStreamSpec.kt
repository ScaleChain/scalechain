package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.io.BlockDataOutputStream
import io.scalechain.io.InputOutputStream
import io.scalechain.io.VarIntValues
import java.io.ByteArrayOutputStream
import java.util.*

class InputOutputStreamWithBlockDataOutputStreamSpec : FlatSpec(), Matchers {

    var bout    : ByteArrayOutputStream? = null
    var stream  : InputOutputStream? = null
    override fun beforeEach() {
        // set-up code
        //
        bout = ByteArrayOutputStream()
        stream = InputOutputStream( null, BlockDataOutputStream(bout!!) )
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
        stream?.close()
        stream = null
        bout = null
    }

    fun testLittleEndianInt(value : Int, encoded : Array<Byte>) : Boolean {
        stream?.littleEndianInt(value)
        stream?.flush()

        return Arrays.equals(encoded.toByteArray(), bout?.toByteArray())
    }

    fun testLittleEndianLong(value : Long, encoded : Array<Byte>) : Boolean {
        stream?.littleEndianLong(value)
        stream?.flush()

        return Arrays.equals(encoded.toByteArray(), bout?.toByteArray())
    }

    init {
        "writeLittleEndianInt" should "write 0" {
            testLittleEndianInt(0, arrayOf<Byte>(0, 0, 0, 0)) shouldBe true
        }

        "writeLittleEndianLong" should "write 0L" {
            testLittleEndianLong(0L, arrayOf<Byte>(0, 0, 0, 0, 0, 0, 0, 0)) shouldBe true
        }


        "writeVarInt" should "write encoded values" {
            for ( (decoded, encoded) in VarIntValues.ENCODED_VALUE_MAP) {
                println("testing readVarInt : $decoded")
                val bout = ByteArrayOutputStream()
                val stream = InputOutputStream( null, BlockDataOutputStream(bout) )
                stream.variableInt(decoded)
                stream.flush()
                java.util.Arrays.equals( encoded, bout.toByteArray() ) shouldBe true
            }
        }

        "writeBytes" should "write bytes" {
            val inputBytes = arrayOf<Byte>(0,1).toByteArray()
            stream?.bytes(inputBytes)
            stream?.flush()
            Arrays.equals( inputBytes, bout?.toByteArray() ) shouldBe true
        }
    }
}