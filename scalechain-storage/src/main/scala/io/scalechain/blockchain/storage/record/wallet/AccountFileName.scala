package io.scalechain.blockchain.storage.record.wallet

/**
  * Created by mijeong on 2016. 3. 22..
  */
object AccountFileName {
  val POSTFIX = ".dat"
  val PREFIX_LENGTH = 7
  val FILE_NUMBER_LENGTH = 5

  def apply(prefix: String, account: String, fileNumber: Int) = {
    assert(fileNumber >= 0)
    s"${prefix}${account}${"%05d".format(fileNumber)}.dat"
  }

  def unapply(fileName : String) : Option[(String, String, Int)] = {
    if (fileName.endsWith(POSTFIX)) {
      val prefix = fileName.substring(0, PREFIX_LENGTH)
      val account = fileName.substring(PREFIX_LENGTH, fileName.length-POSTFIX.length-FILE_NUMBER_LENGTH)
      val fileNumberPart =
        fileName.substring(
          prefix.length+account.length,
          fileName.length - POSTFIX.length)
      try {
        println("prefix: " + prefix + ", account: " + account + ", fileNumberPart: " + fileNumberPart.toInt)
        Some(prefix, account, fileNumberPart.toInt)
      } catch {
        case e : NumberFormatException => {
          None
        }
      }
    } else {
      None
    }
  }

}
