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
class ArrayUtilSpec : FlatSpec(), Matchers {
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


    fun byteArrayOf(vararg args: Int): ByteArray = args.map { it.toByte() }.toByteArray()

    init {
        "pad" should "pad bytes" {
            ArrayUtil.pad(byteArrayOf(), 0, 0).toList() shouldBe
              byteArrayOf().toList()

            ArrayUtil.pad(byteArrayOf(), 1, 0).toList() shouldBe
              byteArrayOf(0).toList()

            ArrayUtil.pad(byteArrayOf(1), 1, 0).toList() shouldBe
              byteArrayOf(1).toList()

            ArrayUtil.pad(byteArrayOf(1), 2, 0).toList() shouldBe
              byteArrayOf(1, 0).toList()
        }

        "unpad" should "unpad padded bytes" {
            ArrayUtil.unpad(byteArrayOf(), 0).toList() shouldBe
              byteArrayOf().toList()

            ArrayUtil.unpad(byteArrayOf(), 1).toList() shouldBe
              byteArrayOf().toList()


            ArrayUtil.unpad(byteArrayOf(0), 0).toList() shouldBe
              byteArrayOf().toList()

            ArrayUtil.unpad(byteArrayOf(1), 0).toList() shouldBe
              byteArrayOf(1).toList()

            ArrayUtil.unpad(byteArrayOf(0, 0), 0).toList() shouldBe
              byteArrayOf().toList()

            ArrayUtil.unpad(byteArrayOf(1, 0), 0).toList() shouldBe
              byteArrayOf(1).toList()
        }

        "isEqual" should "return true only if two bytearrays are equal" {
            ArrayUtil.isEqual(byteArrayOf(), byteArrayOf()) shouldBe true

            ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(1)) shouldBe true
            ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(2)) shouldBe false
            ArrayUtil.isEqual(byteArrayOf(1), byteArrayOf(1, 2)) shouldBe false

            ArrayUtil.isEqual(byteArrayOf(1, 2), byteArrayOf(1, 2)) shouldBe true
            ArrayUtil.isEqual(byteArrayOf(1, 2), byteArrayOf(1, 3)) shouldBe false
            ArrayUtil.isEqual(byteArrayOf(1, 2), byteArrayOf(1, 2, 3)) shouldBe false

            ArrayUtil.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3)) shouldBe true
            ArrayUtil.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 4)) shouldBe false
            ArrayUtil.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3, 4)) shouldBe false
        }

        "compare" should "return 0 if two arrays are equal" {
            ArrayUtil.compare(byteArrayOf(), byteArrayOf()) shouldBe 0
            ArrayUtil.compare(byteArrayOf(1), byteArrayOf(1)) shouldBe 0
            ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(1,1)) shouldBe 0
            ArrayUtil.compare(byteArrayOf(1,2), byteArrayOf(1,2)) shouldBe 0
        }

        "compare" should "return negative value if the left arrays is less than the right array" {
            (ArrayUtil.compare(byteArrayOf(1), byteArrayOf(2)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1), byteArrayOf(1,1)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1), byteArrayOf(2,1)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(1,2)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(1,1,1)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(1,2,1)) < 0) shouldBe true

            (ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(2)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1,1), byteArrayOf(1,2)) < 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1,1), byteArrayOf(2,1)) < 0) shouldBe true

        }


        "compare" should "return positive value if the left arrays is greater than the right array" {
            (ArrayUtil.compare(byteArrayOf(2), byteArrayOf(1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1), byteArrayOf(1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(2,1), byteArrayOf(1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,2), byteArrayOf(1,1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,1,1), byteArrayOf(1,1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,2,1), byteArrayOf(1,1)) > 0) shouldBe true

            (ArrayUtil.compare(byteArrayOf(2), byteArrayOf(1,1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(1,2), byteArrayOf(1,1,1)) > 0) shouldBe true
            (ArrayUtil.compare(byteArrayOf(2,1), byteArrayOf(1,1,1)) > 0) shouldBe true

        }
    }
}