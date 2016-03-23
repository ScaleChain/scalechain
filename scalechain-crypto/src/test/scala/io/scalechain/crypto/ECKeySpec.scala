package io.scalechain.crypto

import io.scalechain.util.HexUtil
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class ECKeySpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  "method" should "validate a correct signature format" in {
    val signature : Array[Byte] = HexUtil.bytes("304402203f16c6f40162ab686621ef3000b04e75418a0c0cb2d8aebeac894ae360ac1e780220ddc15ecdfc3507ac48e1681a33eb60996631bf6bf5bc0a0682c4db743ce7ca2b01")

    ECKey.ECDSASignature.isEncodingCanonical(signature) shouldBe true
  }
}
