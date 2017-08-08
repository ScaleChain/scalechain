package io.scalechain.blockchain.proto

import io.scalechain.util.Bytes
import io.scalechain.util.ListExt

/**
 * Created by kangmo on 07/12/2016.
 */
object TestData {
    val ALL_ONE_HASH = Hash( Bytes(ListExt.fill(32, 1.toByte()).toByteArray() ) )
}

