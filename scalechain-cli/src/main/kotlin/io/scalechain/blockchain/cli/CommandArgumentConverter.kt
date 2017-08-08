package io.scalechain.blockchain.cli

import kotlin.system.exitProcess

/**
 * Created by kangmo on 06/12/2016.
 */
object CommandArgumentConverter {
    fun toInt(optionName : String, value : String?, minValue : Int = Int.MIN_VALUE) : Int? {
        try {
            if (value != null) {
                val intValue = value.toInt()
                if( intValue < minValue) {
                    println("The option, ${optionName} should be greater than or equal to ${minValue}.")
                    exitProcess(-1)
                }
                return intValue
            }
            return null
        } catch ( e : NumberFormatException ) {
            println("The option, ${optionName} should be an integer.")
            exitProcess(-1)
        }
    }
}