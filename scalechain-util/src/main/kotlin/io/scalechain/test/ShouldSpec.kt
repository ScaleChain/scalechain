package io.scalechain.test

/**
 * Created by kangmo on 15/12/2016.
 */
interface ShouldSpec {
  operator fun String.invoke(test: () -> Unit): Pair<String, () -> Unit>
  infix fun String.should(pair: Pair<String, () -> Unit>): Unit
}
