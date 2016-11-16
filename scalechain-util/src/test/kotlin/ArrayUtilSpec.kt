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

    @Test fun testPad() {
        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(arrayOf(), 0, 0 ),
                arrayOf()))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(arrayOf(), 1, 0 ),
                arrayOf(0)))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(arrayOf(1), 1, 0 ),
                arrayOf(1)))

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.pad(arrayOf(1), 2, 0 ),
                arrayOf(1,0)))
    }

    @Test fun testUnPad() {
        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(), 0),
                arrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(), 1),
                arrayOf()
            )
        )


        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(0), 0),
                arrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(1), 0),
                arrayOf(1)
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(0, 0), 0),
                arrayOf()
            )
        )

        assertTrue(
            ArrayUtil.isEqual(
                ArrayUtil.unpad(arrayOf(1, 0), 0),
                arrayOf(1)
            )
        )
    }


    @Test fun testIsEqual() {
        assertTrue(ArrayUtil.isEqual(arrayOf(), arrayOf()))

        assertTrue(ArrayUtil.isEqual(arrayOf(1), arrayOf(1)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1), arrayOf(2)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1), arrayOf(1,2)))

        assertTrue(ArrayUtil.isEqual(arrayOf(1,2), arrayOf(1,2)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1,2), arrayOf(1,3)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1,2), arrayOf(1,2,3)))

        assertTrue(ArrayUtil.isEqual(arrayOf(1,2,3), arrayOf(1,2,3)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1,2,3), arrayOf(1,2,4)))
        assertFalse(ArrayUtil.isEqual(arrayOf(1,2,3), arrayOf(1,2,3,4)))
    }
}