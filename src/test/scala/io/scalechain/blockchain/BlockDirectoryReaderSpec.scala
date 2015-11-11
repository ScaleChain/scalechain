package io.scalechain.blockchain

import io.scalechain.blockchain.block.{BlockDirectoryReader, BlockReadListener, Block}
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
        if (blocksRead < READ_BLOCKS_UP_TO) {
          blocks(blocksRead) = block
          blocksRead +=1;
        }
      }
    }

    val blockListener = new BlockListener()
    val reader = new BlockDirectoryReader(blockListener)

    /**
    import scala.io.Source

    // The string argument given to getResource is a path relative to
    // the resources directory.
    val source = Source.fromURL(getClass.getResource("/data.xml"))     *
    */
    reader.readFrom("/Users/kangmo/crypto/scalachain/src/test/resources/blocks")

    for (b : Block <- blocks) {
      println("Block : " + b.toString())
    }
    assert(blocksRead == READ_BLOCKS_UP_TO)
  }
}