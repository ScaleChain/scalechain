package io.scalechain.test

/**
 * Created by kangmo on 20/11/2016.
 */

object TestMethods {
    fun S(str : String) = str
    fun A(vararg elements : Byte) = elements.toList().toByteArray()
}