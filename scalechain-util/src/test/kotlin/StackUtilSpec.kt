package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

/**
 * JUnit 4 Test Case
 */
class StackUtilSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testGetStackTrace() {
        try {
            throw IllegalArgumentException()
        } catch ( t : Throwable ) {
            assertTrue( StackUtil.getCurrentStack().contains("IllegalArgumentException") )
        }
    }


    @Test fun getCurrentStack() {
        try {
            throw IllegalArgumentException()
        } catch ( t : Throwable ) {
            assertTrue( StackUtil.getStackTrace(t).contains("IllegalArgumentException") )
        }
    }
}