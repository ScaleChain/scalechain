package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.codec.FileRecordLocatorCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FileRecordLocatorSpec : PayloadTestSuite<FileRecordLocator>()  {

  override val codec = FileRecordLocatorCodec

  override val payload = bytes(
    """
      01 00 00 00 0a 00 00 00  00 00 00 00 c8 00 00 00
    """)

  override val message = FileRecordLocator(
    fileIndex = 1,
    recordLocator = RecordLocator(
      offset = 10,
      size = 200
    )
  )
}
