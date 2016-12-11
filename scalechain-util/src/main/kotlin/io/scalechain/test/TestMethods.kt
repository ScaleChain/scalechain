package io.scalechain.test

import io.scalechain.util.ListExt

/**
 * Created by kangmo on 20/11/2016.
 */

object TestMethods {
    fun S(str : String) = str
    fun A(vararg elements : Byte) = elements.toList().toByteArray()
    fun filledString(count : Int, byte : Byte) = String(ListExt.fill(count, byte).toByteArray())
}