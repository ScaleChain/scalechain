package io.scalechain.blockchain.storage.record

/** Block file name extractor
  * http://www.tutorialspoint.com/scala/scala_extractors.htm
  */
object BlockFileName {
  val PREFIX_LENGTH = 3
  val POSTFIX = ".dat"
  def apply(prefix : String, fileNumber : Int) = {
    assert(prefix.length == PREFIX_LENGTH)
    s"${prefix}${"%05d".format(fileNumber)}.dat"
  }
  def unapply(fileName : String) : Option[(String, Int)] = {
    if (fileName.endsWith(POSTFIX)) {
      val prefix = fileName.substring(0, PREFIX_LENGTH)
      val fileNumberPart =
        fileName.substring(
          PREFIX_LENGTH, // start offset - inclusive
          fileName.length - POSTFIX.length) // end offset - exclusive
      try {
        Some(prefix, fileNumberPart.toInt)
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
