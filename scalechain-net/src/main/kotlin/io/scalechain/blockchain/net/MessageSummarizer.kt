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
    when {
      message is Block -> {
        "Block. Hash : ${message.header.hash()}"
      }
      message is Transaction -> {
        "Transaction. Hash : ${message.hash()}"
      }
      else -> {
        StringUtil.getBrief(message.toString(), 256)
      }
    }
  }
}
