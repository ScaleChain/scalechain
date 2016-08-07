package io.scalechain.blockchain.cli

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
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
    val READ_BLOCKS_UP_TO = 500
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

    try {
      reader.readFrom("scalechain-script/src/test/resources/blocks")
    } catch {
      case e : ProtocolCodecException => {
        if (e.code == ErrorCode.RemainingNotEmptyAfterDecoding) {
          // Because the data file in the path is created by cutting blk00000.dat to 128K, this error can happen.
          // Do nothing.
        } else {
          throw e
        }
      }
    }

    var count = 0
    for (b : Block <- blocks) {
      count += 1
    }
    println("Total Blocks : " + count)
    assert(blocksRead == READ_BLOCKS_UP_TO)
  }
}
