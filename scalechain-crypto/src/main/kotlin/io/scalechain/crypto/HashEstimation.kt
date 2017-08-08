package io.scalechain.crypto

import io.scalechain.util.Utils


object HashEstimation {
  /** Calculate the estimated number of hash calculations to get a specific hash.
    *
    * @param hashValue The hash to calculate the estimated number of hash calculations to get the given hash.
    *                  ex> How many times should we calculate the block header hash value to get the given hash?
    *                      The returned value is used to get the estimated chain-work.
    * @return The estimated number of hash calculations for the given block.
    */
  fun getHashCalculations(hashValue: ByteArray): Long {
    // Step 2 : Calculate the (estimated) number of hash calculations based on the hash value.
    val hashValueBigInt = Utils.bytesToBigInteger(hashValue)
    val totalBits = 8 * 32

    return Math.pow(
      2.toDouble(),
      (totalBits - hashValueBigInt.bitLength()).toDouble()
    ).toLong()
  }
}