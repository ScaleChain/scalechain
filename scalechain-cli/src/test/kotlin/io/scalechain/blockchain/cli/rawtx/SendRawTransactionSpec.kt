package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.rawtx.SendRawTransaction
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.net.handler.TxMessageHandler
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.DiskBlockStorage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class SendRawTransactionSpec : APITestSuite() {

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }


  init {
    // Implemented in SubmitBlockSpec
  }
}
