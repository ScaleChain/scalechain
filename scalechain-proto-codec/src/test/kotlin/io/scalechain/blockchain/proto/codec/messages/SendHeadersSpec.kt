package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.SendHeaders
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.blockchain.proto.codec.SendHeadersCodec
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
 * Created by kangmo on 15/01/2017.
 */
@RunWith(KTestJUnitRunner::class)
class FilterClearSpec : PayloadTestSuite<SendHeaders>() {

  override val codec = SendHeadersCodec

  override val payload = bytes("") // No Payload.

  override val message = SendHeaders()
}
