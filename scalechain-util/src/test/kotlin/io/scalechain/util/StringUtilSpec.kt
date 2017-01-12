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
class StringUtilSpec : FlatSpec(), Matchers {
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
        "getBrief" should "return a brief string when a given string is too long" {

            StringUtil.getBrief("",0) shouldBe  ""

            StringUtil.getBrief("1",0) shouldBe "..."

            StringUtil.getBrief("1",1) shouldBe "1"

            StringUtil.getBrief("1",2) shouldBe "1"

            StringUtil.getBrief("12",2) shouldBe "12"

            StringUtil.getBrief("12",1) shouldBe "1..."

            StringUtil.getBrief("123",2) shouldBe "12..."

            StringUtil.getBrief("123",3) shouldBe "123"
        }
    }
}