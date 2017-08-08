package io.scalechain.util


open class StopWatchEx : StopWatch() {
    fun <T> measure (subject : String, block : () -> T): T  {
        val watchSubject : StopWatchSubject = start(subject)
        val returnValue = block()
        watchSubject.stop()
        return returnValue
    }
}

/**
 * Created by kangmo on 7/10/16.
 */
object GlobalStopWatch : StopWatchEx()



