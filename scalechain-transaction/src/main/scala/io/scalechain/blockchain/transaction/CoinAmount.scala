package io.scalechain.blockchain.transaction

object CoinAmount {
  val ONE_COIN_SATOSHI = scala.math.BigDecimal(100000000)
  def from(satoshi : Long) = {
    CoinAmount( scala.math.BigDecimal(satoshi) / ONE_COIN_SATOSHI )
  }
}

case class CoinAmount(value : scala.math.BigDecimal)
