package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.{Transaction, Block, ProtocolMessage}
import io.scalechain.util.StringUtil
import io.scalechain.blockchain.script.HashSupported._

/**
  * Created by kangmo on 6/20/16.
  */
object MessageSummarizer {
  def summarize(message : ProtocolMessage) = {
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
