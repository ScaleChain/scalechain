package io.scalechain.blockchain.block

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import io.scalechain.io.{BlockDataOutputStream, BlockDataInputStream}

/**
 * Created by kangmo on 11/23/15.
 */
object Util {
  def serialize(block : Block) : Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val dout = new BlockDataOutputStream(bout)
    try {
      val serializer = new BlockSerializer(dout)
      serializer.serialize(block)
    } finally {
      dout.close()
    }
    bout.toByteArray
  }

  def parse(data: Array[Byte]) : Block = {
    val din = new BlockDataInputStream( new ByteArrayInputStream(data))
    val parser = new BlockParser(din)
    parser.parse().get
  }
}
