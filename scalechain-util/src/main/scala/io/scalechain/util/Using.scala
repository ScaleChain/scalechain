package io.scalechain.util

import java.io._

class Using[A <: AutoCloseable](resource: A) {
  def in[B](block: A => B) = {
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
  def using[A <: AutoCloseable](resource: A) = new Using(resource)
}