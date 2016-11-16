package io.scalechain.util

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before

/**
 * JUnit 4 Test Case
 */
class HttpRequesterSpec {

    @Before fun setUp() {
        // set up the test case
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testInputStreamAsString() {
        val INPUT = "HelloWord"
        val istream = ByteInputStream(INPUT.toByteArray(), INPUT.length)
        assertEquals(
            HttpRequester.inputStreamAsString(istream),
            INPUT)
    }

    @Test fun testPost() {
        val POST_DATA = "Hello World!"
        assertEquals (
            HttpRequester.post("http://httpbin.org/post", POST_DATA, "unused", "unused"),
            POST_DATA )
    }
}