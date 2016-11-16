package io.scalechain.util

import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

/**
 * JUnit 4 Test Case
 */
class StringUtilSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testGetBrief() {
        assertEquals(
            StringUtil.getBrief("",0),
            ""
        )

        assertEquals(
            StringUtil.getBrief("1",0),
            "..."
        )

        assertEquals(
            StringUtil.getBrief("1",1),
            "1"
        )

        assertEquals(
            StringUtil.getBrief("1",2),
            "1"
        )

        assertEquals(
            StringUtil.getBrief("12",2),
            "12"
        )

        assertEquals(
            StringUtil.getBrief("12",1),
            "1..."
        )

        assertEquals(
            StringUtil.getBrief("123",2),
            "12..."
        )

        assertEquals(
            StringUtil.getBrief("123",3),
            "123"
        )

    }
}