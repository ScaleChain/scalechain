package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.util.writeUnsignedInt
import io.scalechain.util.writeUnsignedIntLE
import io.scalechain.util.writeUnsignedShort
import io.scalechain.util.writeUnsignedShortLE

class Int16Codec() : Codec<Short> {
    override fun transcode(io : CodecInputOutputStream, obj : Short? ) : Short? {
        if (io.isInput) {
            return io.byteBuf.readShort()
        } else {
            io.byteBuf.writeShort(obj!!.toInt())
            return null
        }
    }
}


class Int16LCodec() : Codec<Short> {
    override fun transcode(io : CodecInputOutputStream, obj : Short? ) : Short? {
        if (io.isInput) {
            return io.byteBuf.readShortLE()
        } else {
            io.byteBuf.writeShortLE(obj!!.toInt())
            return null
        }
    }
}


class UInt16Codec() : Codec<Int> {
    override fun transcode(io : CodecInputOutputStream, obj : Int? ) : Int? {
        if (io.isInput) {
            return io.byteBuf.readUnsignedShort()
        } else {
            io.byteBuf.writeUnsignedShort(obj!!)
            return null
        }
    }
}

class UInt16LCodec() : Codec<Int> {
    override fun transcode(io : CodecInputOutputStream, obj : Int? ) : Int? {
        if (io.isInput) {
            return io.byteBuf.readUnsignedShortLE()
        } else {
            io.byteBuf.writeUnsignedShortLE(obj!!)
            return null
        }
    }
}
