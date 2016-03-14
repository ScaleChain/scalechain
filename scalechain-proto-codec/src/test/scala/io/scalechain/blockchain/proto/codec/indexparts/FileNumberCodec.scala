package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.{FileNumber}
import io.scalechain.blockchain.proto.codec.{FileNumberCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class FileNumberCodecSpec extends PayloadTestSuite[FileNumber]  {

  val codec = FileNumberCodec.codec

  val payload = bytes(
    """
      01 00 00 00
    """)

  val message = FileNumber( 1 )
}
