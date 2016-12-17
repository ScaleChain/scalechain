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
  fun summarize(message : ProtocolMessage) : String {
    when {
      message is Block -> {
        return "Block. Hash : ${message.header.hash()}"
      }
      message is Transaction -> {
        return "Transaction. Hash : ${message.hash()}"
      }
      else -> {
        return StringUtil.getBrief(message.toString(), 256)
      }
    }
  }
}
