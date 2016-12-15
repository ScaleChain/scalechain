package io.scalechain.test

/**
 * Created by kangmo on 15/12/2016.
 */
interface ShouldSpec {
  operator fun String.invoke(test: () -> Unit): Pair<String, () -> Unit>
  infix fun String.should(pair: Pair<String, () -> Unit>): Unit
  companion object {
    inline fun <reified T> shouldThrow(thunk: () -> Any?): T {
      val e = try {
        thunk()
        null
      } catch (e: Throwable) {
        e
      }

      val exceptionClassName = T::class.qualifiedName

      if (e == null)
        throw AssertionError("Expected exception ${T::class.qualifiedName} but no exception was thrown")
      else if (e.javaClass.canonicalName != exceptionClassName)
        throw AssertionError("Expected exception ${T::class.qualifiedName} but ${e.javaClass.name} was thrown", e)
      else
        return e as T
    }
  }
}

