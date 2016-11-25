package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

import io.scalechain.test.TestMethods.S
import io.scalechain.test.TestMethods.A

/**
 * JUnit 4 Test Case
 */
class HexUtilSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    fun byteArrayOf(vararg args : Int) : ByteArray = args.map { it.toByte() }.toByteArray()

    @Test fun testBytes() {
        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("") ),
                byteArrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01") ),
                byteArrayOf(1)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("0a") ),
                byteArrayOf(10)
            )
        )

        val arr = HexUtil.bytes( S("01 02") )
        for ( a in arr) {
            println("=> ${a}")
        }
        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01 02") ),
                byteArrayOf(1,2)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01 02 03") ),
                byteArrayOf(1,2,3)
            )
        )

    }

    @Test fun testHex() {
        assertEquals( HexUtil.hex(A()), "" )
        assertEquals( HexUtil.hex(A(1)), "01" )
        assertEquals( HexUtil.hex(A(10)), "0a" )
        assertEquals( HexUtil.hex(A(1,2)), "0102" )
        assertEquals( HexUtil.hex(A(1,2,3)), "010203" )
        assertEquals( HexUtil.hex(A(1,2,3), scala.Some(",")), "01,02,03" )
    }

    @Test fun testPrettyHex() {
        assertEquals( HexUtil.prettyHex(A()), "" )
        assertEquals( HexUtil.prettyHex(A(1)), "01" )
        assertEquals( HexUtil.prettyHex(A(10)), "0a" )
        assertEquals( HexUtil.prettyHex(A(1,2)), "01 02" )
        assertEquals( HexUtil.prettyHex(A(1,2,3)), "01 02 03" )
    }

    @Test fun testScalaHex() {
        assertEquals( HexUtil.kotlinHex(A()), "\"\"" )
        assertEquals( HexUtil.kotlinHex(A(1)), "\"01\"" )
        assertEquals( HexUtil.kotlinHex(A(10)), "\"0a\"" )
        assertEquals( HexUtil.kotlinHex(A(1,2)), "\"0102\"" )
        assertEquals( HexUtil.kotlinHex(A(1,2,3)), "\"010203\"" )
    }

}