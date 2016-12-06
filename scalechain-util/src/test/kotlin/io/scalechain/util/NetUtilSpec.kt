package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class NetUtilSpec : FlatSpec(), Matchers {

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
        "getLocalAddresses" should "list addresses" {
            NetUtil.getLocalAddresses() should contain("127.0.0.1")
        }
    }
}