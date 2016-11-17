package io.scalechain.util

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.util.concurrent.TimeUnit

class GlobalStopWatchSpec : FlatSpec(), Matchers {

    var watch : StopWatchEx? = null
    override fun beforeEach() {
        // set-up code
        //
        watch = StopWatchEx()
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
        watch = null
    }

    init {
        "GlobalStopWatch" should "measure elapsed time in a block" {
            GlobalStopWatch.measure("test1") {
                TimeUnit.NANOSECONDS.sleep(100)
            }

            GlobalStopWatch.measure("test2") {
                TimeUnit.NANOSECONDS.sleep(200)
            }

            assert( GlobalStopWatch.toString()!!.contains("test1") )
            assert( GlobalStopWatch.toString()!!.contains("test2") )
        }

        "StopWatchEx" should "measure elapsed time in a block" {
            watch?.measure("test1") {
                TimeUnit.NANOSECONDS.sleep(100)
            }

            watch?.measure("test2") {
                TimeUnit.NANOSECONDS.sleep(200)
            }

            assert( watch?.toString()!!.contains("test1") )
            assert( watch?.toString()!!.contains("test2") )
        }

    }
}