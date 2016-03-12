package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RocksDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "putObject(rawKey)/getObject(rawKey)" should "" in {
  }

  "putObject(objectKey)/getObject(objectKey)" should "" in {
  }

  "delObject(objectKey)" should "" in {
  }

  "get" should "" in {
  }

  "put" should "" in {
  }

  "del" should "" in {
  }
}
