package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.{RecordLocator, FileRecordLocator}
import io.scalechain.blockchain.proto.codec.{FileRecordLocatorCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class FileRecordLocatorSpec : PayloadTestSuite<FileRecordLocator>  {

  val codec = FileRecordLocatorCodec.codec

  val payload = bytes(
    """
      01 00 00 00 0a 00 00 00  00 00 00 00 c8 00 00 00
    """)

  val message = FileRecordLocator(
    fileIndex = 1,
    RecordLocator(
      offset = 10,
      size = 200
    )
  )
}
