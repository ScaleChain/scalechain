package io.scalechain.blockchain.cli.rawtx

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.rawtx.SignRawTransaction
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.cli.APITestSuite
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class SignRawTransactionSpec : APITestSuite() {

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  init {
    // The test does not pass yet. Will make it pass soon.
    "SignRawTransaction" should "" {
      // TODO : Implement.
    }

    "SignRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(SignRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
