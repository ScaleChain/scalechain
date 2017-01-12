package io.scalechain.util

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
class StackUtilSpec : FlatSpec(), Matchers {
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
        "getCurrentStack" should "return a string that contains java.lang.Thread.getStackTrace" {
            try {
                throw IllegalArgumentException()
            } catch ( t : Throwable ) {
                assertTrue( StackUtil.getCurrentStack().contains("java.lang.Thread.getStackTrace") )
            }
        }

        "getStackTrace" should "return a string that contains the name of the thrown exception" {
            try {
                throw IllegalArgumentException()
            } catch ( t : Throwable ) {
                assertTrue( StackUtil.getStackTrace(t).contains("IllegalArgumentException") )
            }
        }

    }

}