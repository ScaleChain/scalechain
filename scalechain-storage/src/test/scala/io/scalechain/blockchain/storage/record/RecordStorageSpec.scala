package io.scalechain.blockchain.storage.record

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "files" should "" in {
  }

  "lastFile" should "" in {
  }

  "lastFileIndex" should "" in {
  }

  "newFile(blockFile)" should "" in {
  }

  "newFile" should "" in {
  }

  "addNewFile" should "" in {
  }

  "appendRecord" should "" in {
  }

  "readRecord" should "" in {
  }

}
