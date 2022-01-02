package io.scalechain.util

import java.io.ByteArrayInputStream

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.HttpRequestException
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
            val istream = ByteArrayInputStream(INPUT.toByteArray())

            HttpRequester.inputStreamAsString(istream) shouldBe INPUT
        }

        "post" should "request POST to a HTTP server and get a response from it" {
            val POST_DATA = "Hello World!"
            val response = HttpRequester.post("http://httpbin.org/post", POST_DATA, "unused", "unused")
            assertTrue( response.contains(POST_DATA) )
        }
// Not sure how to get a response other than HTTP OK(200)
/*
        "post" should "throw an exception if the response was not HTTP OK(200)" {
            val thrown = shouldThrow<HttpRequestException> {
                // Post to an non-existent URL.
                val POST_DATA = ""
                val response = HttpRequester.post("http://httpbin.org/post", POST_DATA, "unused", "unused")
                assertTrue( response.contains(POST_DATA) )
            }
            thrown.code shouldBe ErrorCode.HttpRequestFailure
        }
*/
    }
}