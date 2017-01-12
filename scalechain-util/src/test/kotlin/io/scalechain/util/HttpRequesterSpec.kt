package io.scalechain.util

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith


@RunWith(KTestJUnitRunner::class)
class HttpRequesterSpec : FlatSpec(), Matchers {
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

    init {
        "inputStreamAsString" should "return the string representation of the stream in the HttpRequester" {
            val INPUT = "HelloWord"
            val istream = ByteInputStream(INPUT.toByteArray(), INPUT.length)

            HttpRequester.inputStreamAsString(istream) shouldBe INPUT
        }

        "post" should "request POST to a HTTP server and get a response from it" {
            val POST_DATA = "Hello World!"
            val response = HttpRequester.post("http://httpbin.org/post", POST_DATA, "unused", "unused")
            assertTrue( response.contains(POST_DATA) )
        }
    }
}