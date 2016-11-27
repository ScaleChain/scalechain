package io.scalechain.util

/**
 * Created by kangmo on 27/11/2016.
 */
object ListExt {
    inline fun<reified T> fill(count : Int, value : T) : List<T> {
        val values : Array<T> = (1..count).map{value}.toTypedArray<T>()
        return listOf(*values)
    }
}
