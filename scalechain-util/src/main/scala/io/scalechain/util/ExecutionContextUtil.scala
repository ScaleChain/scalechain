package io.scalechain.util

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

/**
  * Created by kangmo on 5/24/16.
  */
object ExecutionContextUtil {
  def createContext(threadCount : Int) = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(threadCount);

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }
}
