package io.scalechain.crypto

import java.math.BigInteger

import io.scalechain.util.{HexUtil, ByteArray}

object PrivateKey {
  def from(privateKeyString : String) : PrivateKey = {
    PrivateKey(new BigInteger(1, HexUtil.bytes(privateKeyString)))
  }
}

/**
  * Created by kangmo on 5/13/16.
  */
case class PrivateKey(value:BigInteger)
