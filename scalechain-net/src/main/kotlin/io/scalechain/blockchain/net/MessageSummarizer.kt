package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.StringUtil
import io.scalechain.blockchain.script.hash

/**
  * Created by kangmo on 6/20/16.
  */
object MessageSummarizer {
  fun summarize(message : ProtocolMessage) {
    message match {
      case m : Block => {
        s"Block. Hash : ${m.header.hash}"
      }
      case m : Transaction => {
        s"Transaction. Hash : ${m.hash}"
      }
      case m => {
        StringUtil.getBrief(m.toString, 256)
      }
    }
  }
}
