package io.scalechain.util

/**
  * Created by kangmo on 7/10/16.
  */
object GlobalStopWatch extends StopWatchEx {}


class StopWatchEx extends StopWatch {
  def measure[T] (subject : String)(block : => T): T = {
    val watchSubject : StopWatchSubject = start(subject)
    val returnValue = block
    watchSubject.stop()
    returnValue
  }
}
