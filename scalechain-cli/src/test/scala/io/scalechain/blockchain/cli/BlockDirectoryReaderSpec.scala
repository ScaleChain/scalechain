package io.scalechain.blockchain.cli

import io.scalechain.blockchain.proto.Block
import org.scalatest._

/**
 * Created by kangmo on 11/2/15.
 */
class BlockDirectoryReaderSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
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

  "readFrom" should "read all blocks in a file" in {
    val READ_BLOCKS_UP_TO = 100
    val blocks = new Array[Block](READ_BLOCKS_UP_TO)
    var blocksRead = 0

    class BlockListener extends BlockReadListener {
      def onBlock(block : Block ): Unit = {
        //println("onBlock("+blocksRead+") : " + block.header)

        if (blocksRead < READ_BLOCKS_UP_TO) {
          blocks(blocksRead) = block
          blocksRead +=1;
        }
      }
    }

    val blockListener = new BlockListener()
    val reader = new BlockDirectoryReader(blockListener)

    reader.readFrom("scalechain-script/src/test/resources/blocks")

    for (b : Block <- blocks) {
      println("Block : " + b.toString())
    }
    assert(blocksRead == READ_BLOCKS_UP_TO)
  }
}
