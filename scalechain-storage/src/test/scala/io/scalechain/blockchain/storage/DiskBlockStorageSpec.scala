package io.scalechain.blockchain.storage

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class DiskBlockStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "checkBestBlockHash" should "" in {
  }

  "putBlock(block)" should "" in {
  }


  "putBlock(hash,block)" should "" in {
  }


  "putBlockHeader(header)" should "" in {
  }


  "putBlockHeader(hash,header)" should "" in {
  }


  "getTransaction" should "" in {
  }


  "getBestBlockHash" should "" in {
  }


  "hasBlock" should "" in {
  }

  "getBlockHeader" should "" in {
  }

  "hasBlockHeader" should "" in {
  }

  "getBlock" should "" in {
  }

  "getTransaction" should "" in {
  }

}

