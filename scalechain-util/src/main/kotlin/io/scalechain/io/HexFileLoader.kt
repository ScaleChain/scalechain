package io.scalechain.io

import java.nio.charset.CodingErrorAction

import io.scalechain.util.*
import java.io.File
import java.io.FileInputStream

/**
 * Created by kangmo on 1/30/16.
 */
object HexFileLoader {
    // BUGBUG : Changed interface, ByteArray -> ByteArray
    @JvmStatic
    fun load(path:String) : ByteArray  {
        val fileContent = File(path).readLines().map{ it.substring(10,60) }.joinToString("\n")
        return HexUtil.bytes(fileContent)
    }
}


