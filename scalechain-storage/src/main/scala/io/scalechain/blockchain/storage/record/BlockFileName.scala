package io.scalechain.blockchain.storage.record

/** Block file name extractor, which knows how to extract file number from a block file name.
  *
  * About scala extractor :
  * http://www.tutorialspoint.com/scala/scala_extractors.htm
  */
object BlockFileName {
  val PREFIX_LENGTH = 3
  val POSTFIX = ".dat"

  /** Create a block file name from a prefix and a number.
    * Format of the file name :
    *  {PREFIX}00001.dat
    *
    * @param prefix The prefix of the block file.
    * @param fileNumber The file number that comes after the prefix.
    * @return The file name with the prefix and the file number.
    */
  def apply(prefix : String, fileNumber : Int) = {
    assert(fileNumber >= 0)
    s"${prefix}${"%05d".format(fileNumber)}.dat"
  }

  /** Extract prefix and file number from the given string.
    * This method is required for pattern matching a file name with match statement in Scala.
    *
    * Example> The following code prints "prefix : blk, fileNumber : 1"
    *
    * "blk00001.dat" match {
    *   case BlockFileName(prefix, fileNumber) => {
    *     println(s"prefix : $prefix, fileNumber : $fileNumber")
    *   }
    * }
    *
    * @param fileName The file name, where we extract the prefix and the file number.
    * @return Some of (prefix, file number) pair, if the given file name matches the pattern. None otherwise.
    */
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
