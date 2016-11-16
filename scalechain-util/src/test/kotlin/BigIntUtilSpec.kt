package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before
import scala.math.BigInt

/**
 * JUnit 4 Test Case
 */
class BigIntUtilSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testBint() {
        val value : BigInt = BigInt.apply(1)
        assertEquals(
            "BigInt(\"$value\")",
            BigIntUtil.bint(value)
        )
    }
}