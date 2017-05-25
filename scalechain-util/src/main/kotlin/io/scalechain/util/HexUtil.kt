package io.scalechain.util


/**
 * Created by kangmo on 11/3/15.
 */
object HexUtil {
    /** Convert hex string to a byte array.
     *
     * c.f.> The output of DumpChain produces strings such as bytes("cafebebe").
     * Because we have HexUtil.bytes, we can copy the output of DumpChain to Scala source codes, if we imported HexUtil.* .
     *
     * @param hex The hex string such as "cafebebe"
     * @return A byte array, which is converted from the given hex string.
     */
    @JvmStatic
    fun bytes(hex: String): ByteArray {
        //assert(hexString.length % 2 == 0)
        // BUGBUG : this extention function was copied from Internet. Need to make sure that this works.
        fun <T> List<T>.sliding(windowSize: Int): List<List<T>> {
            return this.dropLast(windowSize - 1).mapIndexed { i, s -> Pair(i, this.subList(i, i + windowSize)) }.filter{ it.first % windowSize == 0}.map {it.second}
        }

        // Remove all non-hex chars
        val trimedHex = java.lang.String(hex).replaceAll("[^0-9A-Fa-f]", "")
        // Make the hex string have pairs of bytes. ex> "0" is converted to "00", "201" is converted to "0201"
        val evenHex = if (trimedHex.length %2 != 0) java.lang.String("0" + trimedHex) else java.lang.String(trimedHex)

        return evenHex.toCharArray().toList().sliding(2).map { Integer.parseInt(it.joinToString(""), 16).toByte() }.toByteArray()
    }

    /** Convert a byte array to a hex string with an optional separator between each byte.
     * Ex> In case sep=Some(",") the hex string will look like ca,fe,be,be.
     *
     * @param data The byte array to convert to a hex string.
     * @param sep The separator between each byte.
     * @return The hex string.
     */
    // BUGBUG : Get rid of Scala Option
    @JvmStatic
    fun hex(data: ByteArray, sep: String? ): String {
        val separatorChar = if ( sep != null ) sep else ""

        return data.map{String.format("%02x", it) }.joinToString( separatorChar )
    }

    @JvmStatic
    fun hex(data: ByteArray): String {
        return hex(data, "")
    }

        /** Return a hex string in pretty format.
     * Ex> ca fe be be
     *
     * @param data the byte array.
     * @return the hex string.
     */
    @JvmStatic
    fun prettyHex(data : ByteArray) : String {
        return "${hex(data, " ")}"
    }

    /** Return a string with the hex data in a format that can be copied to Scala source code to produce a byte array from it.
     * toString method of classes that have a byte array can call this method to print hex values.
     * The output can be copied to Scala source code because we have bytes(hex:String) method that converts hex string to a byte array.
     * @param data The byte array.
     * @return A string in a format, hex(hex-data)
     */
    @JvmStatic
    fun kotlinHex(data : ByteArray) : String {
        return "\"${HexUtil.hex(data)}\""
    }
}
