package io.scalechain.blockchain.transaction

/** The amount of coin.
  *
  * @param value The amount of coin value.
  */
data class CoinAmount(val value : java.math.BigDecimal) {
  constructor( value : Long) : this(java.math.BigDecimal.valueOf(value))
  /** Return the coin amount in coin units.
    *
    * @return The coin units calculated from the CoinAmount. In Bitcoin, the units of coin is satoshi.
    */
  fun coinUnits() : Long {
    // BUGBUG : Change from toLongExact to toLong. is it ok?
    return (value * CoinAmount.ONE_COIN_IN_UNITS).toLong()
  }

  companion object {
    /** How many units does a coin have?
     */
    val ONE_COIN_IN_UNITS = java.math.BigDecimal.valueOf(100000000L)

    /** Get CoinAmount from the units of coin. In Bitcoin, the units of coin is satoshi.
     *
     * @param coinUnits
     * @return
     */
    fun from(coinUnits : Long) : CoinAmount {
      return CoinAmount( java.math.BigDecimal.valueOf(coinUnits) / ONE_COIN_IN_UNITS )
    }
  }
}
