package io.scalechain.util

/* Source copied from https://github.com/ACINQ/bitcoin-lib/blob/master/src/main/scala/fr/acinq/bitcoin/Base58.scala
 * The license was apache v2.
 */

import java.math.BigInteger
import java.util.*


/*
 * see https://en.bitcoin.it/wiki/Base58Check_encoding
 *
 * Also, consider using b0c1's implementation.
 * https://www.livecoding.tv/pastebin/uwZe
 *
 * Why base-58 instead of standard base-64 encoding?
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and could be used to create visually identical
 * looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted as an account number.</li>
 * <li>E-mail usually won't line-break if there's no punctuation to break at.</li>
 * <li>Doubleclicking selects the whole number as one word if it's all alphanumeric.</li>
 */
object Base58Util {

    val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    // char -> value
    val alphabetValue = alphabet.indices.zip(alphabet.toList()).associateBy(
        // pair is ( index, alphabet )
        keySelector={ pair -> pair.second }, // choose alphabet for the key
        valueTransform={ pair -> pair.first.toLong() } // choose index for the value
    )

    /**
     *
     * @param input binary data
     * @return the base-58 representation of input
     */
    // BUGBUG : Interface Change, Seq<Byte> => ByteArray
    @JvmStatic
    fun encode(input: kotlin.ByteArray): String {
        if (input.isEmpty()) return ""
        else {
            val big = BigInteger(1, input)
            val builder = StringBuilder()

            tailrec fun encode1(current: BigInteger) {
                when(current) {
                    BigInteger.ZERO -> return
                    else -> {
                        val result = current.divideAndRemainder(BigInteger.valueOf(58L))
                        val x = result[0]
                        val remainder = result[1]
                        builder.append(alphabet[remainder.toInt()])
                        encode1(x)
                    }
                }
            }

            encode1(big)
            input.takeWhile{it.toInt() == 0}.map{ builder.append(alphabet[it.toInt()]) }
            return builder.toString().reversed()
        }
    }

    /**
     *
     * @param input base-58 encoded data
     * @return the decoded data
     */
    @JvmStatic
    fun decode(input: String) : kotlin.ByteArray {
        val zeroes = input.takeWhile{it == '1'}.map{0.toByte()}.toByteArray()
        val trim  = input.dropWhile{it== '1'}.toList()
        val decoded = trim.fold(BigInteger.ZERO, {a, b -> a.multiply(BigInteger.valueOf(58L)).add(BigInteger.valueOf(alphabetValue[b] ?: throw NoSuchElementException()))})
        val result = if (trim.isEmpty()) zeroes else zeroes + decoded.toByteArray().dropWhile{it.toInt() == 0}.toByteArray() // BigInteger.toByteArray may add a leading 0x00
        return result
    }
}


