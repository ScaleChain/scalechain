package io.scalechain.blockchain.cli

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ProtocolCodecException
import io.scalechain.blockchain.proto.Block

/**
 * Created by kangmo on 11/2/15.
 */
class BlockDirectoryReaderSpec : FlatSpec(), Matchers {

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
    "readFrom" should "read all blocks in a file" {
      val READ_BLOCKS_UP_TO = 500
      val blocks = Array<Block?>(READ_BLOCKS_UP_TO, {null})
      var blocksRead = 0

      class BlockListener : BlockReadListener {
        override fun onBlock(block: Block): Unit {
          //println("onBlock("+blocksRead+") : " + block.header)

          if (blocksRead < READ_BLOCKS_UP_TO) {
            blocks[blocksRead] = block
            blocksRead += 1;
          }
        }
      }

      val blockListener = BlockListener()
      val reader = BlockDirectoryReader(blockListener)

      try {
        reader.readFrom("scalechain-script/src/test/resources/blocks")
      } catch(e : ProtocolCodecException) {
        if (e.code == ErrorCode.RemainingNotEmptyAfterDecoding) {
          // Because the data file in the path is created by cutting blk00000.dat to 128K, this error can happen.
          // Do nothing.
        } else {
          throw e
        }
      }

      var count = 0
      for (b: Block? in blocks) {
        if (b!= null) {
          count += 1
        }
      }
      println("Total Blocks : " + count)
      assert(blocksRead == READ_BLOCKS_UP_TO)
    }
  }
}
