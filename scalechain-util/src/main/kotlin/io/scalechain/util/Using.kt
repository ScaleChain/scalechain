package io.scalechain.util

// Create one while writing Kotlin code that requires resource close.
/*
class Using<A <: AutoCloseable>(resource: A) {
  fun in<B>(block: A => B) {
    var t: Throwable = null
    try {
      block(resource)
    } catch {
      case x : Throwable => t = x; throw x
    } finally {
      if (resource != null) {
        if (t != null) {
          try {
            resource.close()
          } catch {
            case y : Throwable => t.addSuppressed(y)
          }
        } else {
          resource.close()
        }
      }
    }
  }
}

object Using {
  fun using<A <: AutoCloseable>(resource: A) = Using(resource)
}
*/