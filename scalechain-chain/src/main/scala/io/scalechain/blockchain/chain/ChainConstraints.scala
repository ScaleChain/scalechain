package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.CoinAmount

trait ChainConstraints {
  /** Get the current difficulty of block hash.
    *
    * @return
    */
  def getDifficulty(): Long

  /** Get the amount of reward that a minder gets from the generation input.
    *
    * @return
    */
  def getCoinbaseAmount(): CoinAmount
}