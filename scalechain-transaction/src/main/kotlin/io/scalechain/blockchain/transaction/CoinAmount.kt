package io.scalechain.blockchain.transaction

/** The amount of coin.
  *
  */
object CoinAmount {
  /** How many units does a coin have?
    */
  val ONE_COIN_IN_UNITS = scala.math.BigDecimal(100000000)

  /** Get CoinAmount from the units of coin. In Bitcoin, the units of coin is satoshi.
    *
    * @param coinUnits
    * @return
    */
  fun from(coinUnits : Long) {
    CoinAmount( scala.math.BigDecimal(coinUnits) / ONE_COIN_IN_UNITS )
  }
}

/** The amount of coin.
  *
  * @param value The amount of coin value.
  */
data class CoinAmount(value : scala.math.BigDecimal) {
  /** Return the coin amount in coin units.
    *
    * @return The coin units calculated from the CoinAmount. In Bitcoin, the units of coin is satoshi.
    */
  fun coinUnits() : Long {
    (value * CoinAmount.ONE_COIN_IN_UNITS).toLongExact
  }
}
