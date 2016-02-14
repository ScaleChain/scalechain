package io.scalechain.util

/**
  * Created by kangmo on 2/14/16.
  */
object CollectionUtil {
  def random[T](i: scala.collection.Iterable[T]) : T = {
    val n = util.Random.nextInt(i.size)
    val it = i.iterator.drop(n)
    it.next
  }
}


