package io.scalechain.util

/*
import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.io.BlockDataInputStream
import io.scalechain.io.BlockDataOutputStream
import io.scalechain.io.InputOutputStream
import io.scalechain.io.VarIntValues
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class InputOutputStreamWithBlockDataInputStreamSpec : FlatSpec(), Matchers {


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

    fun stream( encoded : ByteArray ) = InputOutputStream( BlockDataInputStream(ByteArrayInputStream(encoded.toByteArray())) , null )


    init {
        "readLittleEndianInt" should "read 0" {
            stream( arrayOf<Byte>(0, 0, 0, 0) ).littleEndianInt(-1) shouldBe 0
        }

        "readLittleEndianLong" should "read 0L" {
            stream( arrayOf<Byte>(0, 0, 0, 0, 0, 0, 0, 0) ).littleEndianLong(-1) shouldBe 0L
        }

        "readVarInt" should "read encoded values" {
            for ( (decoded, encoded) in VarIntValues.ENCODED_VALUE_MAP) {
                println("testing readVarInt : $decoded")
                val stream = InputOutputStream( BlockDataInputStream(ByteArrayInputStream(encoded)), null )
                stream.variableInt(-1) shouldBe decoded
            }
        }

        "readBytes(size)" should "read bytes" {
            val arraySize1 = ByteArray(1)
            stream( arrayOf<Byte>(0, 1) ).bytes(arraySize1)
            Arrays.equals( arraySize1, arrayOf<Byte>(0).toByteArray()) shouldBe true
        }

    }
}
*/