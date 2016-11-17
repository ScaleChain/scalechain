package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

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

    fun S(str : String) = str

    @Test fun testBytes() {
        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("") ),
                arrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01") ),
                arrayOf(1)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("0a") ),
                arrayOf(10)
            )
        )

        val arr = HexUtil.bytes( S("01 02") )
        for ( a in arr) {
            println("=> ${a}")
        }
        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01 02") ),
                arrayOf(1,2)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                HexUtil.bytes( S("01 02 03") ),
                arrayOf(1,2,3)
            )
        )

    }

    @Test fun testHex() {
        assertEquals( HexUtil.hex(arrayOf()), "" )
        assertEquals( HexUtil.hex(arrayOf(1)), "01" )
        assertEquals( HexUtil.hex(arrayOf(10)), "0a" )
        assertEquals( HexUtil.hex(arrayOf(1,2)), "0102" )
        assertEquals( HexUtil.hex(arrayOf(1,2,3)), "010203" )
        assertEquals( HexUtil.hex(arrayOf(1,2,3), scala.Some(",")), "01,02,03" )
    }

    @Test fun testPrettyHex() {
        assertEquals( HexUtil.prettyHex(arrayOf()), "" )
        assertEquals( HexUtil.prettyHex(arrayOf(1)), "01" )
        assertEquals( HexUtil.prettyHex(arrayOf(10)), "0a" )
        assertEquals( HexUtil.prettyHex(arrayOf(1,2)), "01 02" )
        assertEquals( HexUtil.prettyHex(arrayOf(1,2,3)), "01 02 03" )
    }

    @Test fun testScalaHex() {
        assertEquals( HexUtil.scalaHex(arrayOf()), """bytes(\"\")""" )
        assertEquals( HexUtil.scalaHex(arrayOf(1)), """bytes(\"01\")""" )
        assertEquals( HexUtil.scalaHex(arrayOf(10)), """bytes(\"0a\")""" )
        assertEquals( HexUtil.scalaHex(arrayOf(1,2)), """bytes(\"0102\")""" )
        assertEquals( HexUtil.scalaHex(arrayOf(1,2,3)), """bytes(\"010203\")""" )
    }

}