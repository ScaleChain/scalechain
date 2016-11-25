package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

/**
 * JUnit 4 Test Case
 */
class ArrayUtilSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    fun byteArrayOf(vararg args : Int) : ByteArray = args.map { it.toByte() }.toByteArray()

    @Test fun testPad() {
        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(byteArrayOf(), 0, 0 ),
                byteArrayOf()))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(byteArrayOf(), 1, 0 ),
                byteArrayOf(0)))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(byteArrayOf(1), 1, 0 ),
                byteArrayOf(1)))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(byteArrayOf(1), 2, 0 ),
                byteArrayOf(1,0)))
    }

    @Test fun testUnPad() {
        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(), 0),
                byteArrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(), 1),
                byteArrayOf()
            )
        )


        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(0), 0),
                byteArrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(1), 0),
                byteArrayOf(1)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(0, 0), 0),
                byteArrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(byteArrayOf(1, 0), 0),
                byteArrayOf(1)
            )
        )
    }


    @Test fun testIsEqual() {
        assertTrue(ArrayUtil.isEqual(byteArrayOf(), byteArrayOf()))

        assertTrue(ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(1)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(2)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(1,2)))

        assertTrue(ArrayUtil.isEqual(byteArrayOf(1,2), byteArrayOf(1,2)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1,2), byteArrayOf(1,3)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1,2), byteArrayOf(1,2,3)))

        assertTrue(ArrayUtil.isEqual(byteArrayOf(1,2,3), byteArrayOf(1,2,3)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1,2,3), byteArrayOf(1,2,4)))
        assertFalse(ArrayUtil.isEqual(byteArrayOf(1,2,3), byteArrayOf(1,2,3,4)))
    }
}